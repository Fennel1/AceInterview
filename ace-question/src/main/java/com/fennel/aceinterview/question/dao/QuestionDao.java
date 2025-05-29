package com.fennel.aceinterview.question.dao;

import com.fennel.aceinterview.question.entity.QuestionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 八股文题目和解答
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
@Mapper
public interface QuestionDao extends BaseMapper<QuestionEntity> {
	
}
