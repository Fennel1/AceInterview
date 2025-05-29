package com.fennel.aceinterview.question.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 试卷题目关联表
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
@Data
@TableName("qms_exam_question_relation")
public class ExamQuestionRelationEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键 id
	 */
	@TableId
	private Integer id;
	/**
	 * 问题 id
	 */
	private Integer questionId;
	/**
	 * 试卷 id
	 */
	private Integer examId;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 更新时间
	 */
	private Date updateTime;
	/**
	 * 是否已删除
	 */
	private Integer delFlag;
	/**
	 * 创建人
	 */
	private String createUser;
	/**
	 * 更新人
	 */
	private String updateUser;

}
