package com.msb.mall.ware.vo;

import lombok.Data;

/**
 * 完成采购，采购项的VO数据
 */
@Data
public class PurchaseItemDoneVO {

    private Long itemId;
    private Integer status;
    private String reason;
}
