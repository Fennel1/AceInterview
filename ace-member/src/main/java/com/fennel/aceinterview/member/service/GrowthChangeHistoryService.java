package com.fennel.aceinterview.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fennel.common.to.member.GrowthChangeHistory;
import com.fennel.common.utils.PageUtils;
import com.fennel.aceinterview.member.entity.GrowthChangeHistoryEntity;

import java.util.Map;

/**
 * 会员-积分值变化历史记录表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:48:51
 */
public interface GrowthChangeHistoryService extends IService<GrowthChangeHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void update(GrowthChangeHistory growthChangeHistory);
}

