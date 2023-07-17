package com.msb.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * 完成采购，采购单的VO数据
 */
@Data
public class PurchaseDoneVO {

    private Long id;
    private List<PurchaseItemDoneVO> items;

}
