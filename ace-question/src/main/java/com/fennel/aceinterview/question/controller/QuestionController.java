package com.fennel.aceinterview.question.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fennel.aceinterview.question.entity.QuestionEntity;
import com.fennel.aceinterview.question.service.QuestionService;
import com.fennel.common.utils.PageUtils;
import com.fennel.common.utils.R;

import javax.validation.Valid;


/**
 * 八股文题目和解答
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
@RestController
@RequestMapping("question/question")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = questionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		QuestionEntity question = questionService.getById(id);

        return R.ok().put("question", question);
    }

    /**
     * 保存
     */
    @RequestMapping(value="/save", method=RequestMethod.POST)
    public R save(@RequestBody QuestionEntity question){
		questionService.saveQuestion(question);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping(value="/update", method=RequestMethod.POST)
    public R update(@RequestBody QuestionEntity question){
		questionService.updateQuestion(question);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		questionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @RequestMapping(value="/create", method=RequestMethod.POST)
    // mock create
    public R create(@Valid @RequestBody QuestionEntity question){
        questionService.createQuestion(question);
        return R.ok();
    }

}
