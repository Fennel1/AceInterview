package com.fennel.aceinterview.question.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@ToString
public class ExamSubmissionRequestDto {

    @NotNull(message = "试卷ID不能为空")
    private String paperId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "答案不能为空")
    private Object answers; // 用户提交的答案, 例如 {"questionId1": "answer1", "questionId2": "answer2_option_id"}

    private Date startTime; // 用户开始答题时间 (可选, 前端传递)
}
