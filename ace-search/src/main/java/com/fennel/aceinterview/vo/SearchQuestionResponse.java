package com.fennel.aceinterview.vo;

import com.fennel.common.to.es.QuestionEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchQuestionResponse {
    private List<QuestionEsModel> questionList;
    private Integer pageNum;
    private Long total;
    private Integer totalPages;
}
