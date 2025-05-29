package com.fennel.aceinterview.question.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.Query;

import com.fennel.aceinterview.question.dao.ExamQuestionDao;
import com.fennel.aceinterview.question.entity.ExamQuestionEntity;
import com.fennel.aceinterview.question.service.ExamQuestionService;


@Service("examQuestionService")
public class ExamQuestionServiceImpl extends ServiceImpl<ExamQuestionDao, ExamQuestionEntity> implements ExamQuestionService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ExamQuestionEntity> page = this.page(
                new Query<ExamQuestionEntity>().getPage(params),
                new QueryWrapper<ExamQuestionEntity>()
        );

        return new PageUtils(page);
    }

}