package com.msb.common.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
@Data
public class SkuReductionDTO {
    private Long skuId;
    //远程 Coupon服务中的字段
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus; // 是否叠加优惠
    // 远程member中的字段
    private List<MemberPriceDTO> memberPrice;

}
