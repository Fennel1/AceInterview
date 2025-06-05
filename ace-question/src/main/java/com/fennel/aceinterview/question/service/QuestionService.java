package com.fennel.aceinterview.question.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fennel.common.utils.PageUtils;
import com.fennel.aceinterview.question.entity.QuestionEntity;

import java.util.Map;

/**
 * 八股文题目和解答
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
public interface QuestionService extends IService<QuestionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    boolean saveQuestion(QuestionEntity question);

    boolean updateQuestion(QuestionEntity question);

    boolean deleteQuestion(Long[] ids);

    boolean createQuestion(QuestionEntity question);

    // 公开给外部调用，例如Controller中创建新问题后
    void addQuestionIdToBloomFilter(Long id);

    QuestionEntity getById(Long id);
}

