package com.msb.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.mall.product.dao.SkuSaleAttrValueDao;
import com.msb.mall.product.entity.SkuSaleAttrValueEntity;
import com.msb.mall.product.service.SkuSaleAttrValueService;
import com.msb.mall.product.vo.SkuItemSaleAttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {
    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据spuId 来找到spu下面所有的sku销售属性的组合信息
     */
    @Override
    public List<SkuItemSaleAttrVO> getSaleAttrValueBySpuId(Long spuId) {

        return skuSaleAttrValueDao.getSaleAttrValueBySpuId(spuId);
    }

}