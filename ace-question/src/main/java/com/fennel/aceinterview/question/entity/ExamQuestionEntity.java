package com.fennel.aceinterview.question.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 试卷题目表
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
@Data
@TableName("qms_exam_question")
public class ExamQuestionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 题目主键 id
	 */
	@TableId
	private Integer id;
	/**
	 * 问题
	 */
	private String question;
	/**
	 * 答案选项 A
	 */
	private String chooseA;
	/**
	 * 答案选项 B
	 */
	private String chooseB;
	/**
	 * 答案选项 C
	 */
	private String chooseC;
	/**
	 * 答案选项 D
	 */
	private String chooseD;
	/**
	 * 正确选项(单选或多选，A/B/C/D)
	 */
	private String rightChoose;
	/**
	 * 题目类型，如 JVM
	 */
	private Integer type;
	/**
	 * 是否多选
	 */
	private Integer multiple;
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
	/**
	 * 是否开启
	 */
	private Integer enable;

}
