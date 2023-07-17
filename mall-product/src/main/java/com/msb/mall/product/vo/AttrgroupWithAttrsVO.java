package com.msb.mall.product.vo;

import com.msb.mall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrgroupWithAttrsVO {
    private Long attrGroupId;

    private String attrGroupName;

    private Integer sort;
    private String descript;
    private String icon;
    //三级分类id
    private Long catelogId;
    // 属性实体类
    private List<AttrEntity> attrs;
}
