package com.fennel.aceinterview.controller;

import com.fennel.aceinterview.service.QuestionSearchService;
import com.fennel.aceinterview.vo.SearchParam;
import com.fennel.aceinterview.vo.SearchQuestionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/search")
@RestController
public class SearchController {

    @Autowired
    QuestionSearchService questionSearchService;

    @PostMapping("/question/list")
    public SearchQuestionResponse list(SearchParam param) {
        return questionSearchService.search(param);
    }
}
