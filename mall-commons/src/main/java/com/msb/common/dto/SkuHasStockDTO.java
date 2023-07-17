package com.msb.common.dto;

import lombok.Data;

/**
 * sku是否有库存
 */
@Data
public class SkuHasStockDTO {
    private Long skuId;
    private Boolean hasStock;
}
