package com.msb.mall.product.vo;

import lombok.Data;
import lombok.ToString;

/**
 * SkuSaleAttrValueEntity sku销售属性表而不是 规格参数表
 * 这个前端定义的名称是真无语！！
 */
@ToString
@Data
public class Attr {
    private Long attrId;
    private String attrName;
    private String attrValue; //
}