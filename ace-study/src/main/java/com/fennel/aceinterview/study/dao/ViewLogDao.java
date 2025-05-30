package com.fennel.aceinterview.study.dao;

import com.fennel.aceinterview.study.entity.ViewLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习-用户学习浏览记录表
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:36:34
 */
@Mapper
public interface ViewLogDao extends BaseMapper<ViewLogEntity> {
	
}
