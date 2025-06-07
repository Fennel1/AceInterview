package com.fennel.aceinterview.study.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fennel.common.to.study.ViewLog;
import com.fennel.common.utils.PageUtils;
import com.fennel.aceinterview.study.entity.ViewLogEntity;

import java.util.Map;

/**
 * 学习-用户学习浏览记录表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:36:34
 */
public interface ViewLogService extends IService<ViewLogEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void update(ViewLog viewLog);
}

