package com.fennel.aceinterview.study.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 学习-用户学习浏览记录表
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:36:34
 */
@Data
@TableName("sms_view_log")
public class ViewLogEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 题目id
	 */
	private Long quesId;
	/**
	 * 题目类型id
	 */
	private Long quesType;
	/**
	 * 用户id
	 */
	private Long memberId;
	/**
	 * 删除标记（0-正常，1-删除）
	 */
	private Long count;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 更新时间
	 */
	private Date updateTime;

}
