package com.msb.mall.cart.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车中的商品信息
 */
public class CartItem {
    // 商品的编号 SkuId
    @Setter@Getter
    private Long skuId;
    // 商品的图片
    @Setter@Getter
    private String image;
    // 商品的标题
    @Setter@Getter
    private String title;
    // 是否选中
    @Setter@Getter
    private boolean check = true;
    // 商品的销售属性
    @Setter@Getter
    private List<String> skuAttr;
    // 商品的单价
    @Setter@Getter
    private BigDecimal price;
    // 购买的数量
    @Setter@Getter
    private Integer count;
    // 商品的总价
    private BigDecimal totalPrice;

    public BigDecimal getTotalPrice() {
        // 商品的总价  price * count
        return price.multiply(new BigDecimal(count));
    }




}
