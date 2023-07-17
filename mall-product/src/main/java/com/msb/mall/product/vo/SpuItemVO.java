package com.msb.mall.product.vo;

import com.msb.mall.product.entity.SkuImagesEntity;
import com.msb.mall.product.entity.SkuInfoEntity;
import com.msb.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * 商品详情页的数据对象
 */
@ToString
@Data
public class SpuItemVO {
    // 1.sku的基本信息 pms_sku_info
    SkuInfoEntity skuInfoEntity;
    Boolean hasStock = true;
    // 2.sku的图片信息pms_sku_images
    List<SkuImagesEntity> skuImagesEntities;
    // 3.获取spu中的销售属性的组合
    List<SkuItemSaleAttrVO> spuSaleAttrVO;
    // 4.获取SPU的介绍
    SpuInfoDescEntity spuInfoDescEntity;

    // 5.获取SPU的规格参数
    List<SpuItemGroupAttrVo> baseAttrs;

}
