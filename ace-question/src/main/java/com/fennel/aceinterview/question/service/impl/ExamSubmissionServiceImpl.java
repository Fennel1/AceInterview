package com.fennel.aceinterview.question.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fennel.aceinterview.question.dao.ExamPaperDao;
import com.fennel.aceinterview.question.dto.ExamSubmissionRequestDto;
import com.fennel.aceinterview.question.entity.ExamPaper;
import com.fennel.aceinterview.question.entity.ExamSubmission;
import com.fennel.aceinterview.question.service.ExamSubmissionService;
import com.fennel.aceinterview.question.dao.ExamSubmissionDao;
import com.fennel.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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

    private static final String LOCK_SUBMIT_PREFIX = "lock:submit:paper:";
    private static final long LOCK_WAIT_TIME_SECONDS = 10;
    private static final long LOCK_LEASE_TIME_SECONDS = 60;

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

            boolean saved = this.save(submission);
            if (!saved) {
                log.error("保存试卷提交记录到数据库失败: {}", submission);
                return R.failed("提交失败，请稍后重试。");
            }
            log.info("试卷提交记录保存成功，Submission ID: {}", submission.getSubmissionId());

            // 6. 模拟调用判卷服务
            log.info("模拟调用判卷服务，Submission ID: {}...", submission.getSubmissionId());
            try {
                Thread.sleep(3000);
                log.info("模拟判卷服务调用完成，Submission ID: {}", submission.getSubmissionId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("模拟判卷服务调用被中断，Submission ID: {}", submission.getSubmissionId(), e);
            }
            return R.ok(submission);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取提交锁时被中断: {}", lockKey, e);
            return R.failed("系统繁忙，请稍后重试。");
        } catch (Exception e) {
            log.error("处理试卷提交时发生未知异常 for lockKey {}: ", lockKey, e); // 增加了 lockKey 方便排查
            return R.failed("提交失败，系统内部错误。");
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("释放提交锁: {}", lockKey);
            }
        }
    }
}




