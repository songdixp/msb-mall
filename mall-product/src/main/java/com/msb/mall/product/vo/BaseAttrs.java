package com.msb.mall.product.vo;

import lombok.Data;

@Data
public class BaseAttrs {
    private Long attrId;
    private String attrValues;
    //快速展示 数据库中的quick_show
    private int showDesc;
}