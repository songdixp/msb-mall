package com.msb.mall.product.vo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Skus {
    // sku_info 表
    private List<Attr> attr;
    private String skuName;
    private String skuTitle;
    private String skuSubtitle;
    private BigDecimal price;

    private List<Images> images;
    private List<String> descar;
    //远程 Coupon服务中的字段
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    // 远程member中的字段
    private List<MemberPrice> memberPrice;

}