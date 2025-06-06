package com.fennel.aceinterview.question.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ToString
public class ExamPaperRandomRequestDto {
    @NotNull(message = "创建用户ID不能为空")
    private Long userId; // 创建试卷的用户ID

    private String title; // 试卷标题, 可选, Service层可提供默认值
    private String description; // 试卷描述, 可选
    private Integer durationMinutes; // 建议考试时长(分钟), 可选, Service层可提供默认值
    private String totalPossibleScore; // 试卷总分(字符串), 可选, Service层可提供默认值

    @Min(value = 1, message = "题目数量至少为1")
    private Integer questionCount; // 期望的题目数量, 可选, Service层可提供默认值 (例如10)

    // 题目类型列表 (对应 qms_question.TYPE 字段)
    // 如果为 null、空列表或包含特殊值(如-1)，则表示所有类型
    private List<Integer> questionTypes;
}
