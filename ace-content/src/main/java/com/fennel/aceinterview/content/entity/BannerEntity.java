package com.fennel.aceinterview.content.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 内容-横幅广告表
 * 
 * @author fennel
 * @email fennel1@163.com
 * @date 2025-05-30 22:30:38
 */
@Data
@TableName("cms_banner")
public class BannerEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 图片路径
	 */
	private String imgUrl;
	/**
	 * 标题
	 */
	private String title;
	/**
	 * 排序
	 */
	private Integer displayOrder;
	/**
	 * 是否显示
	 */
	private Integer enable;
	/**
	 * 跳转类型
	 */
	private Integer renderType;
	/**
	 * 跳转路径
	 */
	private String renderUrl;
	/**
	 * 删除标记（0-正常，1-删除）
	 */
	private Integer delFlag;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 更新时间
	 */
	private Date updateTime;

}
