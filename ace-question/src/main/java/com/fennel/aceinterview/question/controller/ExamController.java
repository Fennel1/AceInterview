package com.fennel.aceinterview.question.controller;

import com.baomidou.mybatisplus.extension.api.R;
import com.fennel.aceinterview.question.dto.ExamPaperRandomRequestDto;
import com.fennel.aceinterview.question.dto.ExamPaperSpecificRequestDto;
import com.fennel.aceinterview.question.dto.ExamSubmissionRequestDto;
import com.fennel.aceinterview.question.entity.ExamPaper;
import com.fennel.aceinterview.question.entity.ExamSubmission;
import com.fennel.aceinterview.question.service.ExamPaperService;
import com.fennel.aceinterview.question.service.ExamSubmissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 试卷表
 *
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
@Slf4j
@RestController
@RequestMapping("question/exam")
public class ExamController {

    @Autowired
    private ExamPaperService examPaperService;

    @Autowired
    private ExamSubmissionService examSubmissionService;

    @PostMapping("/genRandomExam")
    public R generateRandomExam(@Validated @RequestBody ExamPaperRandomRequestDto dto) {
        log.info("接收到随机生成试卷请求: {}", dto);
        R<ExamPaper> result = examPaperService.generateRandomExamPaper(dto);
        return result;
    }

    @PostMapping("/generateSpecific")
    public R<ExamPaper> generateSpecificExamPaper(@Validated @RequestBody ExamPaperSpecificRequestDto dto) {
        log.info("接收到指定题目生成试卷请求: {}", dto);
        R<ExamPaper> result = examPaperService.generateSpecificExamPaper(dto);
        return result;
    }

    @PostMapping("/submit")
    public R<ExamSubmission> submitExam(@Validated @RequestBody ExamSubmissionRequestDto submissionDto) {
        log.info("Controller接收到试卷提交请求: {}", submissionDto);
        R<ExamSubmission> result = examSubmissionService.submitExamPaper(submissionDto);
        return result;
    }
}
