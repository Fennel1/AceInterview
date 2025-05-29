package com.fennel.aceinterview.question.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fennel.common.utils.PageUtils;
import com.fennel.aceinterview.question.entity.ExamQuestionEntity;

import java.util.Map;

/**
 * 试卷题目表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
public interface ExamQuestionService extends IService<ExamQuestionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

