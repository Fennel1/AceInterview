package com.fennel.aceinterview.question.service.impl;

import com.fennel.aceinterview.question.entity.TypeEntity;
import com.fennel.aceinterview.question.feign.SearchFeignService;
import com.fennel.aceinterview.question.service.TypeService;
import com.fennel.common.to.es.QuestionEsModel;
import com.fennel.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.Query;

import com.fennel.aceinterview.question.dao.QuestionDao;
import com.fennel.aceinterview.question.entity.QuestionEntity;
import com.fennel.aceinterview.question.service.QuestionService;

@Slf4j
@Service("questionService")
public class QuestionServiceImpl extends ServiceImpl<QuestionDao, QuestionEntity> implements QuestionService {

    @Autowired
    TypeService typeService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<QuestionEntity> page = this.page(
                new Query<QuestionEntity>().getPage(params),
                new QueryWrapper<QuestionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public boolean saveQuestion(QuestionEntity question) {
        boolean saveResult = save(question);
        saveEs(question);
        return saveResult;
    }

    @Override
    public boolean updateQuestion(QuestionEntity question) {
        updateById(question);
        saveEs(question);
        return true;
    }

    @Override
    public boolean createQuestion(QuestionEntity question) {
        boolean saveResult = save(question);
        createEs(question);
        return saveResult;
    }

    private boolean saveEs(QuestionEntity question) {
        // 1.创建 ES model
        QuestionEsModel esModel = new QuestionEsModel();

        // 2.复制属性
        // 2.1 复制属性
        BeanUtils.copyProperties(question, esModel);
        // 2.2 获取“题目类型”的名称
        log.info("typeEntity:{}", question);
        TypeEntity typeEntity = typeService.getById(question.getType());
        log.info("typeEntity:{}", typeEntity);
        String typeName = typeEntity.getType();
        // 2.3 给 ES model 的“类型”字段赋值
        esModel.setTypeName(typeName);
        log.info("esModel:{}", esModel);

        // 3. 调用 passjava-search 服务，将数据发送到 ES 中保存。
        R r = searchFeignService.saveQuestion(esModel);
        log.info("r:{}", r);

        return true;
    }

    private boolean createEs(QuestionEntity question) {
        // 1.创建 ES model
        QuestionEsModel esModel = new QuestionEsModel();

        // 2.复制属性
        // 2.1 复制属性
        BeanUtils.copyProperties(question, esModel);
        log.info("esModel:{}", esModel);

        // 3. 调用 passjava-search 服务，将数据发送到 ES 中保存。
        R r = searchFeignService.saveQuestion(esModel);
        log.info("r:{}", r);

        return true;
    }


}