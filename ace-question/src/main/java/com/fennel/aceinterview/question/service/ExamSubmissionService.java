package com.fennel.aceinterview.question.service;

import com.baomidou.mybatisplus.extension.api.R;
import com.fennel.aceinterview.question.dto.ExamSubmissionRequestDto;
import com.fennel.aceinterview.question.entity.ExamSubmission;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Administrator
* @description 针对表【qms_exam_submission(用户试卷提交记录表)】的数据库操作Service
* @createDate 2025-06-06 16:03:03
*/
public interface ExamSubmissionService extends IService<ExamSubmission> {

    R<ExamSubmission> submitExamPaper(ExamSubmissionRequestDto submissionDto);
}
