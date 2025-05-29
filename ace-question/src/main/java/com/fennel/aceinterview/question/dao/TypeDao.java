package com.fennel.aceinterview.question.dao;

import com.fennel.aceinterview.question.entity.TypeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题目-题目类型表
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
@Mapper
public interface TypeDao extends BaseMapper<TypeEntity> {
	
}
