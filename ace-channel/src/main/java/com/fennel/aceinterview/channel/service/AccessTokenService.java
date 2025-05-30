package com.fennel.aceinterview.channel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fennel.common.utils.PageUtils;
import com.fennel.aceinterview.channel.entity.AccessTokenEntity;

import java.util.Map;

/**
 * 渠道-认证表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:14:21
 */
public interface AccessTokenService extends IService<AccessTokenEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

