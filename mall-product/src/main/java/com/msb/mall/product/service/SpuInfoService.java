package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.SpuInfoEntity;
import com.msb.mall.product.vo.SpuInfoVO;

import java.util.Map;

/**
 * spu信息
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuInfoVO vo);

    PageUtils queryPageByCondition(Map<String, Object> params);

    void upBySpuId(Long spuId);
}

