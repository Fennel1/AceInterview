package com.fennel.aceinterview.question.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.fennel.aceinterview.question.entity.TypeEntity;
import com.fennel.aceinterview.question.feign.SearchFeignService;
import com.fennel.aceinterview.question.service.TypeService;
import com.fennel.aceinterview.utils.SimpleBloomFilter;
import com.fennel.common.to.es.QuestionEsModel;
import com.fennel.common.utils.R;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.Query;

import com.fennel.aceinterview.question.dao.QuestionDao;
import com.fennel.aceinterview.question.entity.QuestionEntity;
import com.fennel.aceinterview.question.service.QuestionService;

import javax.annotation.PostConstruct;

@Slf4j
@Service("questionService")
public class QuestionServiceImpl extends ServiceImpl<QuestionDao, QuestionEntity> implements QuestionService {

    @Autowired
    TypeService typeService;

    @Autowired
    SearchFeignService searchFeignService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<QuestionEntity> page = this.page(
                new Query<QuestionEntity>().getPage(params),
                new QueryWrapper<QuestionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public boolean saveQuestion(QuestionEntity question) {
        boolean saveResult = save(question);
        if (saveResult && question.getId() != null) {
            // 先尝试同步保存到ES
            try {
//                throw new Exception("刻意发生错误");
                saveEs(question);
                log.info("ES同步保存成功，ID: {}", question.getId());
            } catch (Exception e) {
                log.error("ES同步保存失败，转为MQ异步: {}", e.getMessage());
                // 发送MQ消息进行重试
                sendRocketMQMessage(question);
            }
            addQuestionIdToBloomFilterInternal(question.getId());
            log.info("新问题保存成功，ID {} 已添加到布隆过滤器。", question.getId());
        }
        return saveResult;
    }

    @Override
    public boolean updateQuestion(QuestionEntity question) {
        boolean updateResult = updateById(question);
        if (updateResult && question.getId() != null) {
            saveEs(question);
            String cacheKey = CACHE_QUESTION_KEY_PREFIX + question.getId();
            stringRedisTemplate.delete(cacheKey);
            log.info("问题ID: {} 更新成功，对应缓存已删除。", question.getId());
        }
        return updateResult;
    }

    @Override
    public boolean deleteQuestion(Long[] ids) {
        return true;
    }

    @Override
    public boolean createQuestion(QuestionEntity question) {
        boolean saveResult = save(question);
        createEs(question);
        return saveResult;
    }

    private boolean saveEs(QuestionEntity question) {
        // 1.创建 ES model
        QuestionEsModel esModel = new QuestionEsModel();

        // 2.复制属性
        // 2.1 复制属性
        BeanUtils.copyProperties(question, esModel);
        // 2.2 获取“题目类型”的名称
        log.info("typeEntity:{}", question);
        TypeEntity typeEntity = typeService.getById(question.getType());
        log.info("typeEntity:{}", typeEntity);
        String typeName = typeEntity.getType();
        // 2.3 给 ES model 的“类型”字段赋值
        esModel.setTypeName(typeName);
        log.info("esModel:{}", esModel);

        // 3. 调用 ace-search 服务，将数据发送到 ES 中保存。
        R r = searchFeignService.saveQuestion(esModel);
        log.info("r:{}", r);

        return true;
    }

    private boolean createEs(QuestionEntity question) {
        // 1.创建 ES model
        QuestionEsModel esModel = new QuestionEsModel();

        // 2.复制属性
        // 2.1 复制属性
        BeanUtils.copyProperties(question, esModel);
        log.info("esModel:{}", esModel);

        // 3. 调用 ace-search 服务，将数据发送到 ES 中保存。
        R r = searchFeignService.saveQuestion(esModel);
        log.info("r:{}", r);

        return true;
    }

    // 解决缓存三问题

    // --- 缓存相关常量 ---
    private static final String CACHE_QUESTION_KEY_PREFIX = "question:id:"; // 问题缓存键前缀
    private static final String LOCK_QUESTION_KEY_PREFIX = "lock:question:id:"; // 问题分布式锁键前缀
    private static final String CACHE_NULL_VALUE = "NULL"; // 缓存空对象的特殊值

    // --- TTLs (Time To Live) 单位：秒 ---
    private static final long CACHE_QUESTION_TTL = 1 * 60 * 60; // 1 小时基础TTL
    private static final long CACHE_NULL_TTL = 5 * 60;          // 5 分钟空值TTL
    private static final long RANDOM_TTL_OFFSET_SECONDS = 10 * 60; // 最大10分钟随机偏移量，防雪崩

    private boolean useRedissonBloomFilter = false;
    private static final String REDISSON_BLOOM_FILTER_NAME = "question:bloomfilter:ids:redisson";
    private RBloomFilter<Long> redissonQuestionIdBloomFilter;

    private static final String CUSTOM_BLOOM_FILTER_INFO = "question:bloomfilter:ids:custom"; // 用于日志
    @Value("${app.cache.bloom-filter.custom.expected-insertions:1000000}") // 为自定义过滤器指定配置前缀
    private long customExpectedInsertions;
    @Value("${app.cache.bloom-filter.custom.fpp:0.01}")
    private double customFpp;
    private SimpleBloomFilter customQuestionIdBloomFilter;

    @PostConstruct
    public void initializeFilters() {
        if (useRedissonBloomFilter) {
            initRedissonBloomFilter();
        } else {
            initCustomBloomFilter();
        }
        // 无论使用哪种，都建议在这里预热数据到布隆过滤器
         populateBloomFilterWithExistingData(); // 生产环境中需要此步骤
    }

    private void initRedissonBloomFilter() {
        redissonQuestionIdBloomFilter = redissonClient.getBloomFilter(REDISSON_BLOOM_FILTER_NAME);
        if (!redissonQuestionIdBloomFilter.isExists()) {
            boolean initialized = redissonQuestionIdBloomFilter.tryInit(customExpectedInsertions, customFpp); // 复用配置值
            if (initialized) {
                log.info("Redisson 布隆过滤器 '{}' 初始化成功. 预期数量: {}, 误判率: {}",
                        REDISSON_BLOOM_FILTER_NAME, customExpectedInsertions, customFpp);
            } else {
                log.warn("Redisson 布隆过滤器 '{}' 尝试初始化失败或已存在但参数不匹配. 请检查Redis状态或配置.", REDISSON_BLOOM_FILTER_NAME);
                if (redissonQuestionIdBloomFilter.isExists()) { // 再次检查，如果存在记录其大小
                    log.info("  -> Redisson 布隆过滤器 '{}' 已存在于Redis中. 当前大小 (bit): {}",
                            REDISSON_BLOOM_FILTER_NAME, redissonQuestionIdBloomFilter.getSize());
                }
            }
        } else {
            log.info("Redisson 布隆过滤器 '{}' 已存在于Redis中. 应用配置预期数量: {}, 误判率: {}",
                    REDISSON_BLOOM_FILTER_NAME, customExpectedInsertions, customFpp);
            log.info("  -> Redis中布隆过滤器的当前大小 (bit): {}", redissonQuestionIdBloomFilter.getSize());
        }
    }

    // 初始化自定义的 SimpleBloomFilter
    private void initCustomBloomFilter() {
        customQuestionIdBloomFilter = new SimpleBloomFilter(
                customExpectedInsertions,
                customFpp
        );
        log.info("自定义 SimpleBloomFilter '{}' 初始化成功. 配置预期数量: {}, 配置误判率: {}, 实际BitSet大小: {}, 实际哈希函数数量: {}",
                CUSTOM_BLOOM_FILTER_INFO,
                customQuestionIdBloomFilter.getExpectedInsertions(),
                customQuestionIdBloomFilter.getFpp(),
                customQuestionIdBloomFilter.getBitSetSize(),
                customQuestionIdBloomFilter.getNumHashFunctions());
        // 对于内存中的SimpleBloomFilter，每次应用重启都需要重新填充数据
        // populateBloomFilterWithExistingData(); // 已在 initializeFilters 末尾统一调用
    }

    /**
     * 生产环境中，在应用启动时，应从数据库加载所有现有问题ID到布隆过滤器
     * 注意：对于非常大的数据集，这可能需要一些时间，可以考虑异步或分批处理
     */
    private void populateBloomFilterWithExistingData() {
        log.info("开始从数据库加载数据到布隆过滤器...");
        List<QuestionEntity> list = this.list(); // 获取所有问题，如果数据量大，请分批
        if (list == null || list.isEmpty()) {
            log.info("数据库中没有问题ID需要加载到布隆过滤器.");
            return;
        }
        List<Long> allQuestionIds = list.stream().map(QuestionEntity::getId).filter(java.util.Objects::nonNull).collect(Collectors.toList());

        if (!allQuestionIds.isEmpty()) {
            for (Long id : allQuestionIds) {
                addQuestionIdToBloomFilterInternal(id);
            }
            log.info("成功将 {} 个ID加载到布隆过滤器.", allQuestionIds.size());
        } else {
            log.info("数据库中没有有效的问题ID需要加载到布隆过滤器.");
        }
    }

    // 内部方法，根据配置选择布隆过滤器添加
    private void addQuestionIdToBloomFilterInternal(Long id) {
        if (id == null) return;
        if (useRedissonBloomFilter && redissonQuestionIdBloomFilter != null) {
            redissonQuestionIdBloomFilter.add(id);
        } else if (!useRedissonBloomFilter && customQuestionIdBloomFilter != null) {
            customQuestionIdBloomFilter.add(id); // 使用自定义过滤器
        }
    }

    // 公开给外部调用，例如Controller中创建新问题后
    @Override
    public void addQuestionIdToBloomFilter(Long id) {
        addQuestionIdToBloomFilterInternal(id);
        log.info("问题ID {} 已添加到布隆过滤器.", id);
    }

    private boolean bloomFilterContains(Long id) {
        if (id == null) return false;
        if (useRedissonBloomFilter && redissonQuestionIdBloomFilter != null) {
            return redissonQuestionIdBloomFilter.contains(id);
        } else if (!useRedissonBloomFilter && customQuestionIdBloomFilter != null) {
            return customQuestionIdBloomFilter.mightContain(id); // 使用自定义过滤器
        }
        log.warn("布隆过滤器未正确配置或初始化，ID {} 的检查将跳过 (假设可能存在).", id);
        return true; // 默认行为，如果过滤器不可用，则不拦截
    }

    @Override
    public QuestionEntity getById(Long id) {
        if (id == null) {
            return null;
        }

        // 1. 【缓存穿透保护】检查布隆过滤器
        //    布隆过滤器说“可能存在”，才继续；如果说“绝对不存在”，则直接返回。
        if (!bloomFilterContains(id)) {
            log.info("布隆过滤器拦截：ID {} 确定不存在，直接返回空。", id);
            return null;
        }

        // 2. 尝试从缓存获取
        String cacheKey = CACHE_QUESTION_KEY_PREFIX + id;
        String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            if (CACHE_NULL_VALUE.equals(cachedJson)) {
                log.info("缓存命中（空值），问题ID: {}", id);
                return null; // 这是一个缓存的空值标记
            }
            try {
                log.info("缓存命中，问题ID: {}", id);
                return JSON.parseObject(cachedJson, QuestionEntity.class); // 使用FastJSON反序列化
            } catch (JSONException e) {
                log.error("从缓存反序列化问题失败，ID: {}，错误: {}", id, e.getMessage());
                // 可选: 删除损坏的缓存条目
                // stringRedisTemplate.delete(cacheKey);
            }
        }

        // 3. 缓存未命中：尝试获取分布式锁，防止【缓存击穿】（热点Key并发重建）
        RLock lock = redissonClient.getLock(LOCK_QUESTION_KEY_PREFIX + id);
        try {
            // 尝试获取锁，等待10秒，锁租约30秒（Redisson的RLock会自动续期）
            boolean isLocked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (isLocked) {
                log.info("获取到锁，问题ID: {}", id);
                try {
                    // 【双重检查锁定 DCL】再次检查缓存，防止在等待锁期间其他线程已重建缓存
                    cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
                    if (cachedJson != null) {
                        if (CACHE_NULL_VALUE.equals(cachedJson)) return null;
                        return JSON.parseObject(cachedJson, QuestionEntity.class);
                    }

                    // 查询数据库
                    log.info("缓存未命中，查询数据库，问题ID: {}", id);
                    QuestionEntity questionFromDb = super.getById(id); // 调用MyBatis-Plus的原始方法
                    if (questionFromDb != null) {
                        // 序列化并存入缓存
                        String jsonToCache = JSON.toJSONString(questionFromDb); // 使用FastJSON序列化
                        // 【防缓存雪崩】设置随机化的过期时间
                        long ttl = CACHE_QUESTION_TTL + ThreadLocalRandom.current().nextLong(0, RANDOM_TTL_OFFSET_SECONDS + 1);
                        stringRedisTemplate.opsForValue().set(cacheKey, jsonToCache, ttl, TimeUnit.SECONDS);
                        log.info("问题ID: {} 已缓存，TTL: {}s", id, ttl);
                        return questionFromDb;
                    } else {
                        // 【缓存穿透保护】数据库中不存在，缓存一个空值标记
                        stringRedisTemplate.opsForValue().set(cacheKey, CACHE_NULL_VALUE, CACHE_NULL_TTL, TimeUnit.SECONDS);
                        log.info("数据库中问题ID: {} 不存在，已缓存空值标记。", id);
                        return null;
                    }
                } catch (JSONException e) {
                    log.error("在锁内处理问题时发生JSON序列化/反序列化错误，ID: {}", id, e);
                    return super.getById(id); // 出现异常，可选回退到直接查库
                } finally {
                    if (lock.isHeldByCurrentThread()) { // 确保是当前线程持有才解锁
                        lock.unlock();
                        log.info("释放锁，问题ID: {}", id);
                    }
                }
            } else {
                // 未能获取到锁：可以等待一小段时间后重试从缓存读取，或返回降级信息
                log.warn("未能获取到锁，问题ID: {}. 短暂等待后尝试再次从缓存读取。", id);
                Thread.sleep(100); // 短暂等待，让持有锁的线程完成操作
                return getByIdFromCacheOnly(id); // 仅从缓存尝试获取（不尝试加锁重建）
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取锁被中断，问题ID: {}", id, e);
            return super.getById(id); // 中断异常，可选回退到直接查库
        }
    }

    // 辅助方法：仅从缓存读取，不尝试加锁重建
    private QuestionEntity getByIdFromCacheOnly(Long id) {
        String cacheKey = CACHE_QUESTION_KEY_PREFIX + id;
        String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            if (CACHE_NULL_VALUE.equals(cachedJson)) return null;
            try {
                return JSON.parseObject(cachedJson, QuestionEntity.class);
            } catch (JSONException e) {
                log.error("重试从缓存反序列化问题失败，ID: {}", id, e.getMessage());
            }
        }
        // 如果等待后缓存仍然没有，或者业务允许直接查库（不推荐，可能导致DB压力）
        // return super.getById(id);
        return null; // 或抛出自定义异常表示临时不可用
    }
    
    private void sendRocketMQMessage(QuestionEntity question) {
        try {
            // 转换为ES模型
            QuestionEsModel esModel = convertToEsModel(question);
            // 构建消息，将ES模型转换为JSON字符串
            String message = JSON.toJSONString(esModel);
            // 发送消息到指定Topic和Tag
            rocketMQTemplate.syncSend("QUESTION_TOPIC:ES_RETRY", message);
            log.info("问题ID {} 的ES重试消息发送成功", question.getId());
        } catch (Exception e) {
            log.error("问题ID {} 的ES重试消息发送失败: {}", question.getId(), e.getMessage());
        }
    }
    
    private QuestionEsModel convertToEsModel(QuestionEntity question) {
        QuestionEsModel esModel = new QuestionEsModel();
        BeanUtils.copyProperties(question, esModel);
        
        // 获取题目类型名称
        TypeEntity typeEntity = typeService.getById(question.getType());
        if (typeEntity != null) {
            esModel.setTypeName(typeEntity.getType());
        }
        
        return esModel;
    }
}
