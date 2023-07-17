package com.msb.mall.product;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.msb.mall.product.entity.BrandEntity;
import com.msb.mall.product.service.AttrGroupService;
import com.msb.mall.product.service.BrandService;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.service.SkuSaleAttrValueService;
import com.msb.mall.product.vo.SkuItemSaleAttrVO;
import com.msb.mall.product.vo.SpuItemGroupAttrVo;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class MallProductApplicationTests {
    @Autowired
    BrandService brandService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Test
    void testAttrSaleAttr() {
        List<SkuItemSaleAttrVO> skuItemSaleAttrVOS = skuSaleAttrValueService.getSaleAttrValueBySpuId(8l);
        for (SkuItemSaleAttrVO skuItemSaleAttrVO : skuItemSaleAttrVOS) {
            System.out.println("skuItemSaleAttrVO = " + skuItemSaleAttrVO);
        }
        
    }
    @Test
    void testAttrGroup() {
        List<SpuItemGroupAttrVo> spuItemGroupAttrVos = attrGroupService.getAttrgroupBySpuId(8l, 225l);
        for (SpuItemGroupAttrVo spuItemGroupAttrVo : spuItemGroupAttrVos) {
            System.out.println("spuItemGroupAttrVo = " + spuItemGroupAttrVo);
        }
    }
    @Test
    void testRedisson() {
        System.out.println("redissonClient"+redissonClient);
    }
    @Test
    void contextLoads() {
        BrandEntity entity = new BrandEntity();
        entity.setName("魅族");
        brandService.save(entity);
    }

    @Test
    void selectAll() {
        List<BrandEntity> list = brandService.list();
        for (BrandEntity entity : list) {
            System.out.println(entity);
        }
    }

    @Test
    void selectById() {
        List<BrandEntity> list = brandService
                .list(new QueryWrapper<BrandEntity>().eq("brand_id",2));
        for (BrandEntity entity : list) {
            System.out.println(entity);
        }
    }

    @Autowired
    private CategoryService categoryService;
    @Test
    public void test1(){
        Long[] catelogPath = categoryService.findCatelogPath(165L);
        System.out.println(Arrays.toString(catelogPath));
    }

    @Test
    public void test2(){
        System.out.println("test");
    }
}
