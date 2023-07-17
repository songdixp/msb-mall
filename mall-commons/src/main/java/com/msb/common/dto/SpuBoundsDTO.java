package com.msb.common.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class SpuBoundsDTO {
    private Long spuId;
    private BigDecimal buyBounds; //购物积分
    private BigDecimal growBounds; // 成长积分
}
