package com.fennel.aceinterview.question.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.Query;

import com.fennel.aceinterview.question.dao.ExamDao;
import com.fennel.aceinterview.question.entity.ExamEntity;
import com.fennel.aceinterview.question.service.ExamService;


@Service("examService")
public class ExamServiceImpl extends ServiceImpl<ExamDao, ExamEntity> implements ExamService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ExamEntity> page = this.page(
                new Query<ExamEntity>().getPage(params),
                new QueryWrapper<ExamEntity>()
        );

        return new PageUtils(page);
    }

}