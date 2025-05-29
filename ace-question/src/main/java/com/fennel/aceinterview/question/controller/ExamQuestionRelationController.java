package com.fennel.aceinterview.question.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fennel.aceinterview.question.entity.ExamQuestionRelationEntity;
import com.fennel.aceinterview.question.service.ExamQuestionRelationService;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.R;



/**
 * 试卷题目关联表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
@RestController
@RequestMapping("question/examquestionrelation")
public class ExamQuestionRelationController {
    @Autowired
    private ExamQuestionRelationService examQuestionRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = examQuestionRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Integer id){
		ExamQuestionRelationEntity examQuestionRelation = examQuestionRelationService.getById(id);

        return R.ok().put("examQuestionRelation", examQuestionRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody ExamQuestionRelationEntity examQuestionRelation){
		examQuestionRelationService.save(examQuestionRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody ExamQuestionRelationEntity examQuestionRelation){
		examQuestionRelationService.updateById(examQuestionRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
		examQuestionRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
