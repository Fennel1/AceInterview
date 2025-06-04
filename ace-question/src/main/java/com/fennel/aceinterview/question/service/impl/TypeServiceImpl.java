package com.fennel.aceinterview.question.service.impl;

import com.alibaba.fastjson.JSON;
import com.fennel.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.Query;

import com.fennel.aceinterview.question.dao.TypeDao;
import com.fennel.aceinterview.question.entity.TypeEntity;
import com.fennel.aceinterview.question.service.TypeService;

import static java.lang.Thread.sleep;

@Slf4j
@Service("typeService")
public class TypeServiceImpl extends ServiceImpl<TypeDao, TypeEntity> implements TypeService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<TypeEntity> page = this.page(
                new Query<TypeEntity>().getPage(params),
                new QueryWrapper<TypeEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageRedis(Map<String, Object> params) {
        String Cache = stringRedisTemplate.opsForValue().get("typeEntityList");
        if (StringUtils.isEmpty(Cache)) {
            log.info("Redis 缓存为空");

            PageUtils page = queryPage(params);
            Cache = JSON.toJSONString(page);
            stringRedisTemplate.opsForValue().set("typeEntityList", Cache);
            return page;
        }
        return JSON.parseObject(Cache, PageUtils.class);
    }

    @Override
    public PageUtils queryPageRedisLock(Map<String, Object> params) {
        synchronized (this) {
            String cache = stringRedisTemplate.opsForValue().get("typeEntityList");
            if (StringUtils.isEmpty(cache)) {
                log.info("RedisLock 缓存为空");

                PageUtils page = queryPage(params);
                cache = JSON.toJSONString(page);
                stringRedisTemplate.opsForValue().set("typeEntityList", cache);
                return page;
            }
            return JSON.parseObject(cache, PageUtils.class);
        }
    }

    private PageUtils queryPageFromDB(Map<String, Object> params) {
        String typeEntityListCache;
        // 从缓存中查询数据
        String cache = stringRedisTemplate.opsForValue().get("typeEntityList");
        if (!StringUtils.isEmpty(cache)) {
            // 如果缓存中有数据，则从缓存中拿出来，并反序列化为实例对象，并返回结果
            return JSON.parseObject(cache, PageUtils.class);
        }
        // 如果缓存中没有数据，从数据库中查询数据
        log.info("Redis 缓存为空");
        PageUtils page = queryPage(params);
        cache = JSON.toJSONString(page);
        // 将序列化后的数据存入缓存中
        stringRedisTemplate.opsForValue().set("typeEntityList", cache);
        return page;
    }

    /**
     * 休眠等待分布式锁释放，防止程序递归调用，栈空间溢出
     * 问题：业务代码出现异常或者服务器宕机，没有执行删除锁的逻辑，会造成死锁
     * @param params
     * @return
     * @throws InterruptedException
     */
    public PageUtils queryPageDLock1(Map<String, Object> params) throws InterruptedException {
        // 1.先抢占锁
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "123");
        if(Boolean.TRUE.equals(lock)) {
            // 2.抢占成功，执行业务
            PageUtils page =  queryPageFromDB(params);
            // 3.解锁
            stringRedisTemplate.delete("lock");
            return page;
        } else {
            // 4.休眠一段时间
            sleep(100);
            // 5.抢占失败，等待锁释放
            return queryPageDLock1(params);
        }
    }

    /**
     * 在占锁成功后，设置锁的过期时间
     * 问题：占锁和设置过期时间之间发生异常，则锁永远不能过期
     * @param params
     * @return
     * @throws InterruptedException
     */
    public PageUtils queryPageDLock2(Map<String, Object> params) throws InterruptedException {
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "123");
        if(Boolean.TRUE.equals(lock)) {
            // 设置自动清除时间
            stringRedisTemplate.expire("lock", 10, TimeUnit.SECONDS);
            PageUtils page =  queryPageFromDB(params);
            stringRedisTemplate.delete("lock");
            return page;
        } else {
            sleep(100);
            return queryPageDLock1(params);
        }
    }

    /**
     * 原子占锁和设置过期时间
     * 问题：任务执行时间长，还未执行完，锁就释放了，造成冲突（同时多个用户执行）
     * @param params
     * @return
     * @throws InterruptedException
     */
    public PageUtils queryPageDLock3(Map<String, Object> params) throws InterruptedException {
        // 原子占锁并设置过期时间
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "123", 10, TimeUnit.SECONDS);
        if(Boolean.TRUE.equals(lock)) {
            PageUtils page =  queryPageFromDB(params);
            stringRedisTemplate.delete("lock");
            return page;
        } else {
            sleep(100);
            return queryPageDLock1(params);
        }
    }

    /**
     * 加锁时设置UUID，保证只能删除自己的锁
     * 问题：查询锁和删除锁的逻辑不是原子的
     * @param params
     * @return
     * @throws InterruptedException
     */
    public PageUtils queryPageDLock4(Map<String, Object> params) throws InterruptedException {
        String uuid = UUID.randomUUID().toString();
        log.info("尝试加锁：{}", uuid);
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 10, TimeUnit.SECONDS);
        if(Boolean.TRUE.equals(lock)) {
            log.info("占锁成功{}", uuid);
            PageUtils page =  queryPageFromDB(params);
            String value = stringRedisTemplate.opsForValue().get("lock");
            if (uuid.equals(value)) {
                log.info("清除锁：{}", uuid);
                stringRedisTemplate.delete("lock");
            }
            return page;
        } else {
            log.info("占锁失败{}", uuid);
            sleep(100);
            return queryPageDLock1(params);
        }
    }

    /**
     * 用 Lua 脚本实现原子性
     * @param params
     * @return
     * @throws InterruptedException
     */
    public PageUtils queryPageDLock5(Map<String, Object> params) throws InterruptedException {
        String uuid = UUID.randomUUID().toString();
        log.info("尝试加锁：{}", uuid);
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 10, TimeUnit.SECONDS);
        if(Boolean.TRUE.equals(lock)) {
            log.info("占锁成功{}", uuid);
            PageUtils page =  queryPageFromDB(params);
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            return page;
        } else {
            log.info("占锁失败{}", uuid);
            sleep(100);
            return queryPageDLock1(params);
        }
    }

    /**
     * Redission
     * @param params
     * @return
     * @throws InterruptedException
     */
    public PageUtils queryPageDLock6(Map<String, Object> params) throws InterruptedException {
        RLock lock = redisson.getLock("lock");
        lock.lock();
        PageUtils page = new PageUtils();
        try {
            page =  queryPageFromDB(params);
            Thread.sleep(10000); // 模拟长时间执行任务
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return page;
    }
}