package com.fennel.aceinterview.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fennel.common.utils.PageUtils;
import com.fennel.aceinterview.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员-会员表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:48:51
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    MemberEntity getMemberByUserId(String userId);
}

