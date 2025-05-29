package com.fennel.aceinterview.question.dao;

import com.fennel.aceinterview.question.entity.ExamQuestionRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷题目关联表
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
@Mapper
public interface ExamQuestionRelationDao extends BaseMapper<ExamQuestionRelationEntity> {
	
}
