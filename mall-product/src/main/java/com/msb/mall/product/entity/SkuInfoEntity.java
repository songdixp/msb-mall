package com.msb.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * sku信息
 */
@Data
@TableName("pms_sku_info")
public class SkuInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * skuId
	 */
	@TableId
	private Long skuId;
	/**
	 * spuId 单独处理1
	 */
	private Long spuId;
	/**
	 * sku名称 vo1
	 */
	private String skuName;
	/**
	 * sku介绍描述  前端没有传过来这个字段
	 */
	private String skuDesc;
	/**
	 * 所属分类id  单独处理2
	 */
	private Long catalogId;
	/**
	 * 品牌id 单独处理3
	 */
	private Long brandId;
	/**
	 * 默认图片 单独处理4
	 */
	private String skuDefaultImg;
	/**
	 * 标题 vo2
	 */
	private String skuTitle;
	/**
	 * 副标题 vo3
	 */
	private String skuSubtitle;
	/**
	 * 价格 vo4
	 */
	private BigDecimal price;
	/**
	 * 销量  单独处理5 给了一个默认的0
	 */
	private Long saleCount;

}
