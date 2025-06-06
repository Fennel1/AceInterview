package com.fennel.aceinterview.question.dao;

import com.fennel.aceinterview.question.entity.ExamPaper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Administrator
* @description 针对表【qms_exam_paper(考试试卷信息表)】的数据库操作Mapper
* @createDate 2025-06-06 16:01:45
* @Entity com.fennel.aceinterview.question.entity.ExamPaper
*/
public interface ExamPaperDao extends BaseMapper<ExamPaper> {
}




