package com.fennel.aceinterview.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fennel.common.utils.PageUtils;
import com.fennel.aceinterview.content.entity.BannerEntity;

import java.util.Map;

/**
 * 内容-横幅广告表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:30:38
 */
public interface BannerService extends IService<BannerEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

