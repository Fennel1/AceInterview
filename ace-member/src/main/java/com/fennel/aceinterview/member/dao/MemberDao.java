package com.fennel.aceinterview.member.dao;

import com.fennel.aceinterview.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员-会员表
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:48:51
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
