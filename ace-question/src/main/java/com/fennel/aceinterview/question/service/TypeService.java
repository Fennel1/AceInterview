package com.fennel.aceinterview.question.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fennel.common.utils.PageUtils;
import com.fennel.aceinterview.question.entity.TypeEntity;

import java.util.Map;

/**
 * 题目-题目类型表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
public interface TypeService extends IService<TypeEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageRedis(Map<String, Object> params);

    PageUtils queryPageRedisLock(Map<String, Object> params);
}

