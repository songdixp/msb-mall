package com.msb.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */

	// @NotNull
	// @NotEmpty
	@NotBlank(message = "品牌名称不能为空")
	private String name;
	/**
	 * 品牌logo地址
	 */
	@URL(message = "必须是合法的URL")
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@Pattern(regexp = "/^[a-zA-Z]$/",message = "检索首字母必须是单个字母")
	private String firstLetter;
	/**
	 * 排序
	 */
	@Min(value = 0, message = "最小值为：0")
	@Max(value = 999, message = "最大值：999")
	private Integer sort;

}
