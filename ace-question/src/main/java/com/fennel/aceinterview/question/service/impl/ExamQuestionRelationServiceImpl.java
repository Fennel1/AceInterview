package com.fennel.aceinterview.question.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.Query;

import com.fennel.aceinterview.question.dao.ExamQuestionRelationDao;
import com.fennel.aceinterview.question.entity.ExamQuestionRelationEntity;
import com.fennel.aceinterview.question.service.ExamQuestionRelationService;


@Service("examQuestionRelationService")
public class ExamQuestionRelationServiceImpl extends ServiceImpl<ExamQuestionRelationDao, ExamQuestionRelationEntity> implements ExamQuestionRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ExamQuestionRelationEntity> page = this.page(
                new Query<ExamQuestionRelationEntity>().getPage(params),
                new QueryWrapper<ExamQuestionRelationEntity>()
        );

        return new PageUtils(page);
    }

}