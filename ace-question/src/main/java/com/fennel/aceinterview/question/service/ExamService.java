package com.fennel.aceinterview.question.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fennel.common.utils.PageUtils;
import com.fennel.aceinterview.question.entity.ExamEntity;

import java.util.Map;

/**
 * 试卷表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
public interface ExamService extends IService<ExamEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

