package com.msb.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.msb.common.valid.ListValue;
import com.msb.common.valid.groups.AddGroupsInterface;
import com.msb.common.valid.groups.UpdateGroupsInterface;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 * 更新的时候 id 不能没有
	 * 添加的时候 id 不能有
	 */
	@NotNull(message = "更新时brandId不能为空", groups = UpdateGroupsInterface.class)
	@Null(message = "添加品牌时，brandId需要为空",groups = AddGroupsInterface.class)
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 * 添加品牌，以及更新品牌都不能为空
	 */

	// @NotNull
	// @NotEmpty
	@NotBlank(message = "品牌名称不能为空",
			groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@URL(message = "必须是合法的URL",
			groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(message = "显示状态不能为空",
			groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
	@ListValue(val = {0,1},groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@Pattern(regexp = "^[a-zA-Z]$",message = "检索首字母必须是单个字母",
			groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@Min(value = 0, message = "最小值为：0",groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
	@Max(value = 999, message = "最大值：999",groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
	private Integer sort;

}
