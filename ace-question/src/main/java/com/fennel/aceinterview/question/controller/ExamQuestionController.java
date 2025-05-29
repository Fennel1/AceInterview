package com.fennel.aceinterview.question.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fennel.aceinterview.question.entity.ExamQuestionEntity;
import com.fennel.aceinterview.question.service.ExamQuestionService;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.R;



/**
 * 试卷题目表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
@RestController
@RequestMapping("question/examquestion")
public class ExamQuestionController {
    @Autowired
    private ExamQuestionService examQuestionService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = examQuestionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Integer id){
		ExamQuestionEntity examQuestion = examQuestionService.getById(id);

        return R.ok().put("examQuestion", examQuestion);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody ExamQuestionEntity examQuestion){
		examQuestionService.save(examQuestion);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody ExamQuestionEntity examQuestion){
		examQuestionService.updateById(examQuestion);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
		examQuestionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
