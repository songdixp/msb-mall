package com.msb.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class OrderItemVO {
    private Long skuId;
    // 商品的图片
    private String image;
    // 商品的标题
    private String title;

    private boolean check;
    // 商品的销售属性
    private List<String> skuAttr;
    // 商品的单价
    private BigDecimal price;
    // 购买的数量
    private Integer count;

    private BigDecimal totalPrice;

    private boolean hasStack=true;

}
