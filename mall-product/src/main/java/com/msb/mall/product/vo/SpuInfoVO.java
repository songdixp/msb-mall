package com.msb.mall.product.vo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class SpuInfoVO {
    // spu_id 在entity实体类中拿
    private Long id;  // 分页查询的时候，添加上来了
    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private String catelogName;
    private Long brandId;
    private String brandName;
    private BigDecimal weight;
    private Integer publishStatus; // 上架状态 0新建 1上架 2下架
    // spu_info_desc spu详情页面信息 也就是商品介绍的图片url List集合
    private List<String> decript;
    // spu_images 前端传过来的是商品图集，images_url的集合
    private List<String> images;

    // attr 规格参数 product_attr_value表 但是这里面的show_desc不知道是什么 quick_show？
    private List<BaseAttrs> baseAttrs;
    // sku
    private List<Skus> skus;

    // 会员积分信息
    private Bounds bounds;

    private Date createTime;
    private Date updateTime;

}