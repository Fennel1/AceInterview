package com.fennel.aceinterview.service;

import com.fennel.aceinterview.vo.SearchParam;
import com.fennel.aceinterview.vo.SearchQuestionResponse;

public interface QuestionSearchService {
    SearchQuestionResponse search(SearchParam param);
}
