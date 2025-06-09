package com.fennel.aceinterview.consumer;

import com.alibaba.fastjson.JSON;
import com.fennel.aceinterview.service.QuestionService;
import com.fennel.common.to.es.QuestionEsModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(
    topic = "QUESTION_TOPIC",
    selectorExpression = "ES_RETRY",
    consumerGroup = "question-retry-consumer-group"
)
public class QuestionEsRetryConsumer implements RocketMQListener<String> {

    private static final Logger log = LoggerFactory.getLogger(QuestionEsRetryConsumer.class);
    
    private final QuestionService questionService;

    @Autowired
    public QuestionEsRetryConsumer(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Override
    public void onMessage(String message) {
        try {
            QuestionEsModel esModel = JSON.parseObject(message, QuestionEsModel.class);
            log.info("接收到ES重试消息，问题ID: {}", esModel.getId());
            
            // 保存到ES（使用无异常方法）
            questionService.saveQuestion(esModel);
            log.info("问题ID {} 已成功保存到ES", esModel.getId());
        } catch (Exception e) {
            log.error("处理ES重试消息失败: {}", e.getMessage());
            // 这里可以添加重试或死信队列处理逻辑
        }
    }
}
