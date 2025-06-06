package com.fennel.aceinterview.question.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fennel.aceinterview.question.dto.ExamPaperRandomRequestDto;
import com.fennel.aceinterview.question.dto.ExamPaperSpecificRequestDto;
import com.fennel.aceinterview.question.dao.QuestionDao;
import com.fennel.aceinterview.question.entity.ExamPaper;
import com.fennel.aceinterview.question.service.ExamPaperService;
import com.fennel.aceinterview.question.dao.ExamPaperDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【qms_exam_paper(考试试卷信息表)】的数据库操作Service实现
* @createDate 2025-06-06 16:01:45
*/
@Slf4j
@Service
public class ExamPaperServiceImpl extends ServiceImpl<ExamPaperDao, ExamPaper> implements ExamPaperService{

    @Autowired
    private QuestionDao questionDao;

    @Override
    public R<ExamPaper> generateRandomExamPaper(ExamPaperRandomRequestDto dto){
        // 1. 参数校验 (基本校验由DTO注解完成, 这里可以进行更复杂的业务校验)
        if (dto.getUserId() == null) {
            log.warn("随机生成试卷请求失败：用户ID为空");
            return R.failed("用户ID不能为空");
        }
        if (dto.getQuestionCount() == null || dto.getQuestionCount() <= 0) {
            dto.setQuestionCount(10); // 提供默认值或校验失败
            log.info("随机生成试卷请求：题目数量未提供或无效，已设置为默认值 10");
//            return R.failed("题目数量必须大于0");
        }

        // 2. 根据类型和数量随机选取题目ID
        List<Integer> types = dto.getQuestionTypes();
        boolean selectAllTypes = CollectionUtils.isEmpty(types) || types.contains(-1);
        List<Long> randomQuestionIds;
        log.info("开始随机选题，请求数量: {}, 类型: {}", dto.getQuestionCount(), selectAllTypes ? "全部" : types.toString());
        if (selectAllTypes) {
            randomQuestionIds = questionDao.findRandomQuestionIds(Collections.emptyList(), dto.getQuestionCount());
        }
        else{
            randomQuestionIds = questionDao.findRandomQuestionIds(types, dto.getQuestionCount());
        }

        if (CollectionUtils.isEmpty(randomQuestionIds)) {
            log.warn("随机选题失败：未能根据条件找到足够的题目。类型: {}, 请求数量: {}",
                    selectAllTypes ? "全部" : types.toString(), dto.getQuestionCount());
            return R.failed("未能根据条件找到足够的题目");
        }
        if (randomQuestionIds.size() < dto.getQuestionCount()) {
            log.warn("随机选题警告：请求生成 {} 道题目，但只找到了 {} 道符合条件的题目。",
                    dto.getQuestionCount(), randomQuestionIds.size());
            // 根据业务决定是报错还是按实际数量生成
             return R.failed("题库题目不足，无法生成指定数量的试卷");
        }
        log.info("随机选题成功，选中题目ID列表: {}", randomQuestionIds);

        ExamPaper examPaper = new ExamPaper();
        examPaper.setPaperId(UUID.randomUUID().toString());
        examPaper.setUserId(dto.getUserId());
        examPaper.setTitle(dto.getTitle() != null ? dto.getTitle() : "随机试卷");
        examPaper.setDescription(dto.getDescription());
        examPaper.setDurationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : 60);
        examPaper.setTotalPossibleScore(dto.getTotalPossibleScore() != null ? dto.getTotalPossibleScore() : "100");
        examPaper.setStatus("DRAFT"); // 初始状态为草稿

        try {
            // 使用 FastJSON 序列化
            examPaper.setQuestionIds(JSON.toJSONString(randomQuestionIds));
        } catch (JSONException e) {
            log.error("序列化题目ID列表失败 (FastJSON): {}，错误: {}", randomQuestionIds, e.getMessage(), e);
            return R.failed("生成试卷失败：无法处理题目ID");
        }

        // 4. 保存到数据库
        boolean result = save(examPaper);
        if (result) {
            log.info("随机试卷生成成功，试卷ID: {}", examPaper.getPaperId());
            return R.ok(examPaper);
        } else {
            log.error("保存随机试卷信息到数据库失败，试卷数据: {}", JSON.toJSONString(examPaper));
            return R.failed("保存试卷信息失败");
        }
    }

    @Override
    public R<ExamPaper> generateSpecificExamPaper(ExamPaperSpecificRequestDto dto){
        // 1. 参数校验
        if (dto.getUserId() == null) {
            log.warn("指定题目生成试卷请求失败：用户ID为空");
            return R.failed("用户ID不能为空");
        }
        if (CollectionUtils.isEmpty(dto.getQuestionIds())) {
            log.warn("指定题目生成试卷请求失败：题目ID列表为空");
            return R.failed("题目ID列表不能为空");
        }

        // 2. 校验提供的题目ID是否都有效 (可选, 但推荐)
        // 去重，防止传入重复ID导致校验数量不一致
        List<Long> distinctQuestionIds = dto.getQuestionIds().stream().distinct().collect(Collectors.toList());
        log.info("开始校验指定题目ID列表 (去重后): {}", distinctQuestionIds);

        int existingCount = questionDao.countExistingAndEnabledQuestions(distinctQuestionIds);
        if (existingCount != distinctQuestionIds.size()) {
            log.warn("指定题目生成试卷校验失败：提供的题目ID列表包含无效或已禁用的题目。请求ID数量 (去重后): {}, 有效ID数量: {}",
                    distinctQuestionIds.size(), existingCount);
            return R.failed("提供的题目ID列表中包含无效或已删除/禁用的题目，请检查。");
        }
        log.info("指定题目ID列表校验通过，所有题目均有效。");

        ExamPaper examPaper = new ExamPaper();
        examPaper.setPaperId(UUID.randomUUID().toString());
        examPaper.setUserId(dto.getUserId());
        examPaper.setTitle(dto.getTitle() != null ? dto.getTitle() : "试卷");
        examPaper.setDescription(dto.getDescription());
        examPaper.setDurationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : 60);
        examPaper.setTotalPossibleScore(dto.getTotalPossibleScore() != null ? dto.getTotalPossibleScore() : "100");
        examPaper.setStatus("DRAFT");

        try {
            // 使用 FastJSON 序列化
            examPaper.setQuestionIds(JSON.toJSONString(distinctQuestionIds));
        } catch (JSONException e) {
            log.error("序列化题目ID列表失败 (FastJSON): {}，错误: {}", distinctQuestionIds, e.getMessage(), e);
            return R.failed("生成试卷失败：无法处理题目ID");
        }

        // 4. 保存到数据库
        boolean result = save(examPaper);
        if (result) {
            log.info("指定题目试卷生成成功，试卷ID: {}", examPaper.getPaperId());
            return R.ok(examPaper);
        } else {
            log.error("保存指定题目试卷信息到数据库失败，试卷数据: {}", JSON.toJSONString(examPaper));
            return R.failed("保存试卷信息失败");
        }
    }
}




