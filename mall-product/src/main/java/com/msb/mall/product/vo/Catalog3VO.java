package com.msb.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 三级分类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catalog3VO{
    private String catalog2Id ; // 三级分类对应的二级分类的编号
    private String id; // 三级分类编号
    private String name; // 三级分类名称

}