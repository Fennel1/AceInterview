package com.fennel.aceinterview.question.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fennel.aceinterview.question.dao.ExamPaperDao;
import com.fennel.aceinterview.question.dto.ExamSubmissionRequestDto;
import com.fennel.aceinterview.question.entity.ExamPaper;
import com.fennel.aceinterview.question.entity.ExamSubmission;
import com.fennel.aceinterview.question.feign.MemberFeignService;
import com.fennel.aceinterview.question.feign.StudyFeignService;
import com.fennel.aceinterview.question.service.ExamSubmissionService;
import com.fennel.aceinterview.question.dao.ExamSubmissionDao;
import com.fennel.common.to.member.GrowthChangeHistory;
import com.fennel.common.to.study.ViewLog;
import com.fennel.common.utils.StringUtils;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
* @author Administrator
* @description 针对表【qms_exam_submission(用户试卷提交记录表)】的数据库操作Service实现
* @createDate 2025-06-06 16:03:03
*/
@Slf4j
@Service
public class ExamSubmissionServiceImpl extends ServiceImpl<ExamSubmissionDao, ExamSubmission> implements ExamSubmissionService{

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ExamPaperDao examPaperDao;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private StudyFeignService studyFeignService;

    private static final String LOCK_SUBMIT_PREFIX = "lock:submit:paper:";
    private static final long LOCK_WAIT_TIME_SECONDS = 10;
    private static final long LOCK_LEASE_TIME_SECONDS = 60;

    // 定义 RocketMQ Topic 和 Tag
    public static final String GRADING_TOPIC = "EXAM_GRADING_TOPIC";
    public static final String GRADING_TAG_SUBMIT = "SUBMISSION_CREATED";

    private static final Integer POINTS_FOR_EXAM_SUBMISSION = 10; // 例如：提交试卷获得10积分
    private static final Integer SOURCE_TYPE_EXAM_SUBMISSION = 3;

    private static final Integer FEIGN_SUCCESS_CODE = 0;

    @GlobalTransactional(name = "submit-exam-paper-tx")
    @Override
    public R<ExamSubmission> submitExamPaper(ExamSubmissionRequestDto submissionDto) {
        log.info("接收到试卷提交请求: {}", submissionDto);

        // 1. 基本参数校验 (DTO注解已处理NotNull，这里可以对内容做进一步检查)
        if (submissionDto.getAnswers() == null) { // DTO的@NotNull保证了不为null，但可以检查其内容是否有效
            return R.failed("答案内容不能为空对象");
        }
        // 如果 answers 是字符串，可以检查是否为空字符串
        if (submissionDto.getAnswers() instanceof String && !StringUtils.hasText((String)submissionDto.getAnswers())) {
            return R.failed("答案内容不能为空字符串");
        }

        // 2. 校验试卷是否存在且可提交
        ExamPaper examPaper = examPaperDao.selectById(submissionDto.getPaperId());
        if (examPaper == null) {
            log.warn("提交失败：试卷ID {} 不存在。", submissionDto.getPaperId());
            return R.failed("提交失败：无效的试卷。");
        }
        if (!"PUBLISHED".equalsIgnoreCase(examPaper.getStatus())) {
            log.warn("提交失败：试卷ID {} 状态为 {}，不可提交。", submissionDto.getPaperId(), examPaper.getStatus());
            return R.failed("提交失败：该试卷当前不可提交。");
        }

        // 3. 使用Redisson分布式锁
        String lockKey = LOCK_SUBMIT_PREFIX + submissionDto.getPaperId() + ":user:" + submissionDto.getUserId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(LOCK_WAIT_TIME_SECONDS, LOCK_LEASE_TIME_SECONDS, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("获取提交锁失败，可能正在处理该用户的提交: {}", lockKey);
                return R.failed("操作频繁，请稍后再试或提交正在处理中。");
            }
            log.info("成功获取提交锁: {}", lockKey);

            // 4. (在锁内) 检查用户是否已经提交过这份试卷
            LambdaQueryWrapper<ExamSubmission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ExamSubmission::getPaperId, submissionDto.getPaperId())
                    .eq(ExamSubmission::getUserId, submissionDto.getUserId());
            long existingSubmissions = this.count(queryWrapper);
            if (existingSubmissions > 0) {
                log.warn("用户 {} 已提交过试卷 {}，拒绝重复提交。", submissionDto.getUserId(), submissionDto.getPaperId());
                return R.failed("您已提交过该试卷，请勿重复提交。");
            }

            // 5. 创建并保存 ExamSubmission 实体
            ExamSubmission submission = new ExamSubmission();
            submission.setPaperId(submissionDto.getPaperId());
            submission.setUserId(submissionDto.getUserId());

            // 处理 answers 字段 (Object 类型)
            // 如果 answers 已经是JSON字符串，可以直接设置。
            // 如果是Map或其他对象，需要序列化为JSON字符串。
            Object answersObject = submissionDto.getAnswers();
            String answersJsonString;
            if (answersObject instanceof String) {
                answersJsonString = (String) answersObject;
                // 可选：校验一下这个字符串是否是合法的JSON (如果前端保证不了)
                try {
                    JSON.parse(answersJsonString); // 尝试解析，验证格式
                } catch (Exception e) {
                    log.error("前端传递的answers字符串不是有效的JSON格式: {}", answersJsonString, e);
                    return R.failed("提交失败：答案格式无效。");
                }
            } else {
                try {
                    answersJsonString = JSON.toJSONString(answersObject);
                } catch (Exception e) {
                    log.error("序列化用户答案对象失败: {}", answersObject, e);
                    return R.failed("提交失败：无法处理答案内容。");
                }
            }
            submission.setAnswers(answersJsonString); // 存入序列化后的JSON字符串
            submission.setStartTime(submissionDto.getStartTime()); // 使用DTO中的startTime
            // 对于 submissionTime:
            // 方案1: 信任前端传递的 submissionTime
            // submission.setSubmissionTime(submissionDto.getSubmissionTime());
            // 方案2: 以服务器接收到的时间为准 (更可靠)
            submission.setSubmissionTime(new Date());
            submission.setStatus("SUBMITTED");

            // 5.1 调用会员服务，增加成长值/积分
            GrowthChangeHistory history = new GrowthChangeHistory();
            history.setMemberId(submission.getUserId());
            history.setChangeCount(POINTS_FOR_EXAM_SUBMISSION);
            history.setNote(String.format("提交试卷《%s》(ID:%s)", examPaper.getTitle(), examPaper.getPaperId())); // 使用试卷名称和ID，更友好
            history.setSourceType(SOURCE_TYPE_EXAM_SUBMISSION);
            try {
                log.info("准备调用会员服务 (Seata XID: {})...", io.seata.core.context.RootContext.getXID());
                com.fennel.common.utils.R memberFeignResult = memberFeignService.update(history); // 接收Feign调用的返回结果
                if (memberFeignResult == null || !FEIGN_SUCCESS_CODE.equals(memberFeignResult.get("code"))) {
                    String errorMsg = "会员服务增加成长值业务处理失败";
                    if (memberFeignResult != null && memberFeignResult.get("msg") != null) {
                        errorMsg += ". 原因: " + memberFeignResult.get("msg");
                    } else if (memberFeignResult == null) {
                        errorMsg += ". 原因: 返回结果为null";
                    }
                    log.error(errorMsg + "，用户ID: {}", submission.getUserId());
                    throw new RuntimeException(errorMsg + "，触发回滚");
                }
                log.info("会员服务调用成功。");
            } catch (Exception e) { // 捕获 Feign 调用本身可能抛出的底层异常 (如网络、Ribbon找不到服务等)
                log.error("调用会员服务时发生底层异常，用户ID: {}，试卷ID: {}。",
                        submission.getUserId(), submission.getPaperId(), e);
                throw new RuntimeException("调用会员服务失败，触发回滚", e); // 将异常重新抛出
            }

            // 5.2 调用学习服务，为试卷中的每个问题记录查看/操作日志
            Object questionIdsObj = examPaper.getQuestionIds();
            if (questionIdsObj == null) {
                log.warn("试卷 {} 的题目ID列表 (questionIds) 为空，无法为具体题目记录学习日志。", examPaper.getPaperId());
                // 根据业务需求，如果题目列表为空是异常情况，可以取消注释下一行以中断操作并回滚
                 throw new RuntimeException("试卷题目列表为空，无法记录学习日志，触发回滚");
            } else {
                List<Long> questionIdList;
                if (questionIdsObj instanceof String) {
                    String questionIdsJson = (String) questionIdsObj;
                    if (StringUtils.hasText(questionIdsJson)) {
                        try {
                            // 假设 qms_question.id 是 Long 类型，并且在生成试卷时存储的是 List<Long> 的JSON
                            questionIdList = JSON.parseArray(questionIdsJson, Long.class);
                        } catch (JSONException e) {
                            log.error("反序列化试卷 {} 的题目ID列表失败: {}。错误: {}", examPaper.getPaperId(), questionIdsJson, e.getMessage(), e);
                            throw new RuntimeException("处理试卷题目ID列表失败，触发回滚", e);
                        }
                    } else {
                        questionIdList = Collections.emptyList(); // 空JSON字符串视为空列表
                        log.warn("试卷 {} 的题目ID列表 (questionIds) 为空字符串，不记录题目学习日志。", examPaper.getPaperId());
                    }
                } else {
                    // 如果 questionIdsObj 不是 String，说明数据存储可能有问题
                    log.error("试卷 {} 的题目ID列表 (questionIds) 类型不正确，期望 String，实际为: {}。内容: {}",
                            examPaper.getPaperId(), questionIdsObj.getClass().getName(), questionIdsObj.toString());
                    throw new RuntimeException("试卷题目ID列表格式错误，触发回滚");
                }

                if (questionIdList != null && !questionIdList.isEmpty()) {
                    log.info("准备为试卷 {} 中的 {} 个题目记录学习日志。", examPaper.getPaperId(), questionIdList.size());
                    for (Long questionId : questionIdList) {
                        if (questionId == null) {
                            log.warn("题目ID列表中存在null值，跳过此条学习日志记录。试卷ID: {}", examPaper.getPaperId());
                            continue;
                        }
                        ViewLog viewLog = new ViewLog();
                        viewLog.setQuesId(questionId); // 使用从列表中获取的单个题目ID
                        viewLog.setMemberId(submission.getUserId());
                        // 可根据需要设置ViewLog的其他属性，如logType, createTime(通常由服务方处理)

                        try {
                            log.info("准备调用学习服务记录题目日志 (Seata XID: {}), 用户ID: {}, 题目ID: {}",
                                    RootContext.getXID(), submission.getUserId(), questionId);
                            com.fennel.common.utils.R studyFeignResult = studyFeignService.update(viewLog);
                            if (studyFeignResult == null || !FEIGN_SUCCESS_CODE.equals(studyFeignResult.get("code"))) {
                                String errorMsg = "学习服务记录题目日志业务处理失败";
                                if (studyFeignResult != null && studyFeignResult.get("msg") != null) {
                                    errorMsg += ". 原因: " + studyFeignResult.get("msg");
                                } else if (studyFeignResult == null) {
                                    errorMsg += ". 原因: 返回结果为null";
                                }
                                log.error(errorMsg + "，用户ID: {}，题目ID: {}", submission.getUserId(), questionId);
                                throw new RuntimeException(errorMsg + "，触发回滚");
                            }
                            log.info("学习服务为题目ID {} (试卷ID: {}) 记录日志成功。", questionId, examPaper.getPaperId());
                        } catch (Exception e) {
                            log.error("调用学习服务为题目ID {} (试卷ID: {}) 记录日志时发生异常，用户ID: {}。",
                                    questionId, examPaper.getPaperId(), submission.getUserId(), e);
                            // 如果e已经是RuntimeException且是期望的，可以直接throw e; 否则包装一下
                            if (e instanceof RuntimeException) {
                                throw (RuntimeException) e;
                            }
                            throw new RuntimeException("调用学习服务记录题目日志失败，触发回滚", e);
                        }
                    }
                } else {
                    log.info("试卷 {} 的题目ID列表为空或解析后为空，不为具体题目记录学习日志。", examPaper.getPaperId());
                }
            }

            boolean saved = this.save(submission);
            if (!saved) {
                log.error("保存试卷提交记录到数据库失败: {}", submission);
                throw new RuntimeException("保存试卷提交记录失败，触发回滚"); // 抛出异常
            }
            log.info("试卷提交记录保存成功，Submission ID: {}", submission.getSubmissionId());

            // 6. 模拟调用判卷服务
//            log.info("模拟调用判卷服务，Submission ID: {}...", submission.getSubmissionId());
//            try {
//                Thread.sleep(3000);
//                log.info("模拟判卷服务调用完成，Submission ID: {}", submission.getSubmissionId());
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                log.warn("模拟判卷服务调用被中断，Submission ID: {}", submission.getSubmissionId(), e);
//            }
            // 6. 发送消息到 RocketMQ 进行异步判卷
            try {
                // 构建消息体，通常只需要提交记录的ID，消费者可以根据ID查询详细信息
                Map<String, Object> payload = new HashMap<>();
                payload.put("submissionId", submission.getSubmissionId());
                // 你也可以传递其他冗余信息，如 paperId, userId，减少消费者查询次数
                // payload.put("paperId", submission.getPaperId());
                // payload.put("userId", submission.getUserId());

                // 发送同步消息，如果发送失败会抛出异常
                // topic:tag
                String destination = GRADING_TOPIC + ":" + GRADING_TAG_SUBMIT;
                rocketMQTemplate.syncSend(destination, MessageBuilder.withPayload(JSON.toJSONString(payload)).build());
                log.info("成功发送判卷任务到MQ，Topic: {}, Submission ID: {}", GRADING_TOPIC, submission.getSubmissionId());
                // 如果希望提交接口更快返回，可以使用异步发送 rocketMQTemplate.asyncSend(...)
                // 此时需要处理发送成功/失败的回调
            } catch (Exception e) {
                // 消息发送失败的处理逻辑
                // 1. 记录错误日志
                log.error("发送判卷任务到MQ失败，Submission ID: {}. 错误: {}", submission.getSubmissionId(), e.getMessage(), e);
                // 2. 补偿机制：可以将失败的任务存入数据库的失败队列，由定时任务重试发送
                //    或者，如果业务允许，可以暂时忽略，依赖后续的对账/巡检机制发现未判卷的记录
                //    这里为了简单，我们仅记录日志。在生产环境中，这里需要更健壮的处理。
                // 3. 也可以考虑更新 submission 状态为 "PENDING_GRADING_MSG_FAILED" 等，方便追踪
            }

            return R.ok(submission);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取提交锁时被中断: {}", lockKey, e);
//            return R.failed("系统繁忙，请稍后重试。");
            throw new RuntimeException("系统操作被中断，触发回滚。", e);
        } catch (RuntimeException e) { // 优先捕获 RuntimeException，以便直接重新抛出
            log.error("处理试卷提交时发生运行时异常 for lockKey {}: {}，将触发全局事务回滚", lockKey, e.getMessage(), e);
            throw e; // 直接重新抛出 RuntimeException，确保Seata能捕获到并回滚
        } catch (Exception e) {
            log.error("处理试卷提交时发生未知异常 for lockKey {}: ", lockKey, e); // 增加了 lockKey 方便排查
//            return R.failed("提交失败，系统内部错误。");
            throw new RuntimeException("提交失败，系统内部错误，触发回滚。", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("释放提交锁: {}", lockKey);
            }
        }
    }
}




