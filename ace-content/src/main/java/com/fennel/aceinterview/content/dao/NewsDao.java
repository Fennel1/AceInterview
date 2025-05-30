package com.fennel.aceinterview.content.dao;

import com.fennel.aceinterview.content.entity.NewsEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 内容-资讯表
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:30:38
 */
@Mapper
public interface NewsDao extends BaseMapper<NewsEntity> {
	
}
