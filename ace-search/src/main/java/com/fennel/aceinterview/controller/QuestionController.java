package com.fennel.aceinterview.controller;

import com.fennel.aceinterview.service.QuestionService;
import com.fennel.common.exception.BizCodeEnum;
import com.fennel.common.to.es.QuestionEsModel;
import com.fennel.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/search")
@RestController
public class QuestionController {

    @Autowired
    QuestionService questionService;

    @PostMapping("/question/save")
    public R saveQuestion(@RequestBody QuestionEsModel questionEsModel) {
        boolean result = false;
        try {
            result = questionService.save(questionEsModel);
        } catch (Exception e) {
            result = false;
        }

        if (!result) {
            return R.error(BizCodeEnum.QUESTION_SAVE_EXCEPTION.getCode(), BizCodeEnum.QUESTION_SAVE_EXCEPTION.getMsg());
        }
        return R.ok();
    }
}
