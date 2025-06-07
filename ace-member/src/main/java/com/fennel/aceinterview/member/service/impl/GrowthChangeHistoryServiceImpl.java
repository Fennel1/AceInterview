package com.fennel.aceinterview.member.service.impl;

import com.fennel.common.to.member.GrowthChangeHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.Query;

import com.fennel.aceinterview.member.dao.GrowthChangeHistoryDao;
import com.fennel.aceinterview.member.entity.GrowthChangeHistoryEntity;
import com.fennel.aceinterview.member.service.GrowthChangeHistoryService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("growthChangeHistoryService")
public class GrowthChangeHistoryServiceImpl extends ServiceImpl<GrowthChangeHistoryDao, GrowthChangeHistoryEntity> implements GrowthChangeHistoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<GrowthChangeHistoryEntity> page = this.page(
                new Query<GrowthChangeHistoryEntity>().getPage(params),
                new QueryWrapper<GrowthChangeHistoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void update(GrowthChangeHistory growthChangeHistory) {
        if (growthChangeHistory.getMemberId() == null) {
            log.warn("GrowthChangeHistory 中的 memberId 为空，无法记录历史。");
            throw new IllegalArgumentException("会员ID不能为空");
        }
        if (growthChangeHistory.getChangeCount() == null) {
            log.warn("GrowthChangeHistory 中的 changeCount 为空，无法记录历史。");
            throw new IllegalArgumentException("变化值不能为空");
        }

        GrowthChangeHistoryEntity historyEntity = new GrowthChangeHistoryEntity();
        historyEntity.setMemberId(growthChangeHistory.getMemberId());
        historyEntity.setChangeCount(growthChangeHistory.getChangeCount());
        historyEntity.setNote(growthChangeHistory.getNote()); // note 可以为 null
        historyEntity.setSourceType(growthChangeHistory.getSourceType());
        historyEntity.setDelFlag(0);
        this.save(historyEntity);
        log.info("成功为会员ID: {} 记录了积分变化历史, 变化值: {}, ID: {}",
                historyEntity.getMemberId(), historyEntity.getChangeCount(), historyEntity.getId());
    }
}