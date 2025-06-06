package com.fennel.aceinterview.question.service;

import com.baomidou.mybatisplus.extension.api.R;
import com.fennel.aceinterview.question.dto.ExamPaperRandomRequestDto;
import com.fennel.aceinterview.question.dto.ExamPaperSpecificRequestDto;
import com.fennel.aceinterview.question.entity.ExamPaper;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【qms_exam_paper(考试试卷信息表)】的数据库操作Service
* @createDate 2025-06-06 16:01:45
*/
public interface ExamPaperService extends IService<ExamPaper> {

    /**
     * 根据参数随机生成试卷
     * @param requestDto 请求参数
     * @return 生成的试卷信息
     */
    R<ExamPaper> generateRandomExamPaper(ExamPaperRandomRequestDto requestDto);

    /**
     * 根据指定的题目列表生成试卷
     * @param requestDto 请求参数
     * @return 生成的试卷信息
     */
    R<ExamPaper> generateSpecificExamPaper(ExamPaperSpecificRequestDto requestDto);

}
