package com.msb.mall.product.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.mall.product.dao.SkuImagesDao;
import com.msb.mall.product.dao.SkuInfoDao;
import com.msb.mall.product.entity.SkuImagesEntity;
import com.msb.mall.product.entity.SkuInfoEntity;
import com.msb.mall.product.entity.SpuInfoDescEntity;
import com.msb.mall.product.service.*;
import com.msb.mall.product.vo.SkuItemSaleAttrVO;
import com.msb.mall.product.vo.SpuItemGroupAttrVo;
import com.msb.mall.product.vo.SpuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
    @Autowired
    SkuInfoDao skuInfoDao;
    @Autowired
    SkuImagesDao skuImagesDao;
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    AttrGroupService attrGroupService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();
        // 检索关键字
        String key = (String) params.get("key");
        if(StringUtils.isNotEmpty(key)){
            wrapper.and(w->{
                w.eq("sku_id",key).or().like("sku_name",key);
            });
        }

        // 分类
        String catalogId = (String)params.get("catalogId");
        if(StringUtils.isNotEmpty(catalogId) && !"0".equalsIgnoreCase(catalogId)){
            wrapper.eq("catalog_id",catalogId);
        }
        // 品牌
        String brandId = (String)params.get("brandId");
        if(StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }
        // 价格区间
        String min = (String) params.get("min");
        if(StringUtils.isNotEmpty(min)){
            wrapper.ge("price",min);
        }
        String max = (String) params.get("max");
        if(StringUtils.isNotEmpty(max)){
            try {
                // 如果max=0那么我们也不需要加这个条件
                BigDecimal bigDecimal = new BigDecimal(max);
                if(bigDecimal.compareTo(new BigDecimal(0)) > 0 &&
                        bigDecimal.compareTo(new BigDecimal(9999999)) <0){
                    // 说明 max > 0  < 9999999
                    wrapper.le("price",max);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);

    }

    /**
     * spuId 找到 Sku列表
     */
    @Override
    public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {
        return skuInfoDao.selectList(
                new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    /**
     * 封装成为spuItemVo对象 返回给前端的页面 item.html
     */
    @Override
    public SpuItemVO item(Long skuId) throws ExecutionException, InterruptedException {
        SpuItemVO spuItemVO = new SpuItemVO();
        CompletableFuture<SkuInfoEntity> skuInfoEntityFuture = CompletableFuture.supplyAsync(() -> {
            //1 找到sku基本信息
            SkuInfoEntity skuInfoEntity = this.getById(skuId);
            spuItemVO.setSkuInfoEntity(skuInfoEntity);
            // Long spuId = skuInfoEntity.getSpuId();
            // Long catalogId = skuInfoEntity.getCatalogId();
            return skuInfoEntity;
        }, threadPoolExecutor);

        CompletableFuture<Void> saleFuture = skuInfoEntityFuture.thenAcceptAsync((res) -> {
            //3 spu销售属性的组合
            List<SkuItemSaleAttrVO> skuSaleAttrVOS = skuSaleAttrValueService.getSaleAttrValueBySpuId(res.getSpuId());
            spuItemVO.setSpuSaleAttrVO(skuSaleAttrVOS);
        }, threadPoolExecutor);


        CompletableFuture<Void> spuDescFuture = skuInfoEntityFuture.thenAcceptAsync((res) -> {
            //4 获取spu的介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            spuItemVO.setSpuInfoDescEntity(spuInfoDescEntity);
        }, threadPoolExecutor);


        CompletableFuture<Void> spuGroupFuture = skuInfoEntityFuture.thenAcceptAsync((res) -> {
            //5 spu规格与包装
            List<SpuItemGroupAttrVo> spuItemGroupAttrVos = attrGroupService.getAttrgroupBySpuId(res.getSpuId(), res.getCatalogId());
            spuItemVO.setBaseAttrs(spuItemGroupAttrVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> skuImageFuture = CompletableFuture.runAsync(() -> {
            //2 sku图片信息
            List<SkuImagesEntity> skuImagesEntities = skuImagesDao.selectList(
                    new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            spuItemVO.setSkuImagesEntities(skuImagesEntities);
        }, threadPoolExecutor);

        CompletableFuture
                .allOf(saleFuture, spuGroupFuture, spuDescFuture, skuImageFuture)
                .get();

        return spuItemVO;
    }

    @Override
    public List<String> getSkuSaleAttrsBySkuId(Long skuId) {

        return this.skuInfoDao.getSkuSaleAttrsBySkuId(skuId);
    }

}