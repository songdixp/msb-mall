package com.msb.mall.ware.vo;

import lombok.Data;

import java.util.List;

@Data
public class MergeVO {
    //{ purchaseId: this.purchaseId, items: [purchase_detail_id,..] }
    private Long purchaseId;
    private List<Long> items;
}
