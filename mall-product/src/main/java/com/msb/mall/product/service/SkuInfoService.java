package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.SkuInfoEntity;

import java.util.Map;

/**
 * sku信息
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

