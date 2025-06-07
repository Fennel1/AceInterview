package com.fennel.aceinterview.question.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fennel.aceinterview.question.entity.ExamSubmission;
import com.fennel.aceinterview.question.service.ExamSubmissionService;
import com.fennel.aceinterview.question.service.impl.ExamSubmissionServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RocketMQMessageListener(
        topic = ExamSubmissionServiceImpl.GRADING_TOPIC, // 监听的 Topic
        consumerGroup = "exam-grading-consumer-group",   // 消费者组，要和 properties 中规划的一致
        selectorExpression = ExamSubmissionServiceImpl.GRADING_TAG_SUBMIT // 可选，只消费特定 Tag 的消息
)
public class ExamGradingListener implements RocketMQListener<String> {

    @Autowired
    private ExamSubmissionService examSubmissionService;

    @Override
    public void onMessage(String message) {
        log.info("接收到判卷任务消息: {}", message);
        Long submissionId = null;
        try {
            JSONObject messagePayload = JSON.parseObject(message);
            submissionId = messagePayload.getLong("submissionId");

            if (submissionId == null) {
                log.error("从消息中未能解析出 submissionId: {}", message);
                // 消息格式错误，无法处理，可以直接 ack (RocketMQ默认) 或根据业务抛异常使其重试
                return;
            }

            ExamSubmission submission = examSubmissionService.getById(submissionId);
            if (submission == null) {
                log.warn("未找到 Submission ID: {} 对应的提交记录，可能已被删除或ID错误。", submissionId);
                return;
            }

            // 防止重复处理 (如果 MQ 意外重发消息)
            if (!"SUBMITTED".equalsIgnoreCase(submission.getStatus()) && !"GRADING_RETRY".equalsIgnoreCase(submission.getStatus())) { // 可以增加一个重试状态
                log.info("Submission ID: {} 状态为 {}，非SUBMITTED/GRADING_RETRY，可能已被处理或正在处理，跳过。", submissionId, submission.getStatus());
                return;
            }

            // 1. 更新状态为判卷中 (可选，但推荐)
            submission.setStatus("GRADING");
            // submission.setGradingStartTime(new Date()); // 可以增加字段记录判卷开始时间
            examSubmissionService.updateById(submission);
            log.info("Submission ID: {} 状态更新为 GRADING。", submissionId);

            // 2. 执行实际的判卷逻辑 (这里仍然是模拟)
            log.info("开始处理判卷逻辑 for Submission ID: {}...", submissionId);
            // 这里应该是调用你的真实判卷服务
            // gradingEngineService.grade(submission);
            Thread.sleep(3000); // 模拟耗时的判卷操作
            log.info("判卷逻辑处理完成 for Submission ID: {}.", submissionId);

            // 3. 更新判卷结果和状态
            // 假设判卷后得到分数和批改详情
            // submission.setScore(90.0);
            // submission.setGradingDetails("{\"q1\":\"correct\", \"q2\":\"partially_correct\"}");
            submission.setStatus("GRADED"); // 或 "GRADING_FAILED"
            // submission.setGradingEndTime(new Date());
            boolean updated = examSubmissionService.updateById(submission);
            if (updated) {
                log.info("Submission ID: {} 判卷完成并成功更新状态为 GRADED。", submissionId);
            } else {
                log.error("Submission ID: {} 判卷完成但更新状态失败！需要关注。", submissionId);
                // 这里可能需要补偿机制或告警
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("判卷任务 for Submission ID: {} 被中断。", submissionId, e);
            // 根据业务决定是否重试，如果抛出异常，RocketMQ会进行重试
            // 如果不想重试，需要捕获所有异常并自行处理
            updateSubmissionStatusOnError(submissionId, "GRADING_FAILED_INTERRUPTED");
            // throw new RuntimeException("Grading interrupted, will retry by MQ", e); // 抛出异常让MQ重试
        } catch (Exception e) {
            log.error("处理判卷任务 for Submission ID: {} 时发生错误: {}", submissionId, e.getMessage(), e);
            // 发生其他异常，也可能需要重试或标记为失败
            updateSubmissionStatusOnError(submissionId, "GRADING_FAILED_EXCEPTION");
            // throw new RuntimeException("Grading failed, will retry by MQ", e); // 抛出异常让MQ重试
        }
    }

    private void updateSubmissionStatusOnError(Long submissionId, String status) {
        if (submissionId != null) {
            try {
                ExamSubmission submissionToUpdate = new ExamSubmission();
                submissionToUpdate.setSubmissionId(submissionId);
                submissionToUpdate.setStatus(status);
                // submissionToUpdate.setGradingEndTime(new Date());
                examSubmissionService.updateById(submissionToUpdate);
                log.info("Submission ID: {} 状态因错误更新为 {}.", submissionId, status);
            } catch (Exception ex) {
                log.error("更新 Submission ID: {} 状态为 {} 失败: {}", submissionId, status, ex.getMessage(), ex);
            }
        }
    }
}
