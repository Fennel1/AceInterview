package com.fennel.aceinterview.channel.dao;

import com.fennel.aceinterview.channel.entity.AccessTokenEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 渠道-认证表
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:14:21
 */
@Mapper
public interface AccessTokenDao extends BaseMapper<AccessTokenEntity> {
	
}
