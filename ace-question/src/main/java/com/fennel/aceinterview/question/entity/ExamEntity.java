package com.fennel.aceinterview.question.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 试卷表
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-29 17:41:32
 */
@Data
@TableName("qms_exam")
public class ExamEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 题目主键 id
	 */
	@TableId
	private Integer id;
	/**
	 * 试卷标题
	 */
	private String title;
	/**
	 * 试卷限时
	 */
	private Integer limitTime;
	/**
	 * 试卷难度等级(1-简单，2-中等，3-最难）
	 */
	private Integer level;
	/**
	 * 是否开启
	 */
	private Integer enable;
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
