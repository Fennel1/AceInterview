package com.fennel.aceinterview.question.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ToString
public class ExamPaperSpecificRequestDto {
    @NotNull(message = "创建用户ID不能为空")
    private Long userId; // 创建试卷的用户ID

    private String title; // 试卷标题, 可选, Service层可提供默认值
    private String description; // 试卷描述, 可选
    private Integer durationMinutes; // 建议考试时长(分钟), 可选, Service层可提供默认值
    private String totalPossibleScore; // 试卷总分(字符串), 可选, Service层可提供默认值

    @NotEmpty(message = "题目ID列表不能为空") // @NotEmpty 用于集合，确保列表不为空且至少包含一个元素
    private List<Long> questionIds; // 指定的题目ID列表 (对应 qms_question.id)

}
