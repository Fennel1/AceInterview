package com.fennel.aceinterview.study.service.impl;

import com.fennel.common.to.study.ViewLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.Query;

import com.fennel.aceinterview.study.dao.ViewLogDao;
import com.fennel.aceinterview.study.entity.ViewLogEntity;
import com.fennel.aceinterview.study.service.ViewLogService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("viewLogService")
public class ViewLogServiceImpl extends ServiceImpl<ViewLogDao, ViewLogEntity> implements ViewLogService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ViewLogEntity> page = this.page(
                new Query<ViewLogEntity>().getPage(params),
                new QueryWrapper<ViewLogEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void update(ViewLog viewLog) {
//        throw new IllegalArgumentException("错误");

        if (viewLog.getQuesId() == null || viewLog.getMemberId() == null) {
            log.warn("quesId 或 memberId 为空。");
            // 或者可以抛出 IllegalArgumentException
             throw new IllegalArgumentException("题目ID和用户ID不能为空");
        }

        QueryWrapper<ViewLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ques_id", viewLog.getQuesId());
        queryWrapper.eq("member_id", viewLog.getMemberId());
        ViewLogEntity existingLog = this.getOne(queryWrapper);
        if (existingLog != null) {
            existingLog.setCount(existingLog.getCount() + 1);
            existingLog.setUpdateTime(new Date());
            this.updateById(existingLog); // ServiceImpl 提供的方法，内部调用 baseMapper.updateById(existingLog)
            log.info("用户 {} 对题目 {} 的浏览次数更新为: {}", viewLog.getMemberId(), viewLog.getQuesId(), existingLog.getCount());
        }
        else {
            ViewLogEntity newLog = new ViewLogEntity();
            newLog.setQuesId(viewLog.getQuesId());
            newLog.setMemberId(viewLog.getMemberId());
            newLog.setCount(1L); // 初始计数为1
            newLog.setQuesType(0L);
            this.save(newLog);
            log.info("为用户 {} 创建了对题目 {} 的新浏览日志，次数为: 1", viewLog.getMemberId(), viewLog.getQuesId());
        }
    }
}