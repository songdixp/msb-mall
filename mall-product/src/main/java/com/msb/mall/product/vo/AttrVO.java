package com.msb.mall.product.vo;

import lombok.Data;

/**
 * VO  view object 视图层对象信息
 */
@Data
public class AttrVO {

    private static final long serialVersionUID = 1L;

    private Long attrId;
    private String attrName;
    private Integer searchType;
    private String icon;
    private String valueSelect;
    private Integer attrType;
    private Long enable;
    private Long catelogId;
    private Integer showDesc;
    // 属性分组
    private Long attrGroupId;
}
