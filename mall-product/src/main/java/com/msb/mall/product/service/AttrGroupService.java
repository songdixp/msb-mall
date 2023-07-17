package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.AttrGroupEntity;
import com.msb.mall.product.vo.AttrgroupWithAttrsVO;
import com.msb.mall.product.vo.SpuItemGroupAttrVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);


    List<AttrgroupWithAttrsVO> getAttrgroupWithAttrsByCatelogId(Long catelogId);

    List<SpuItemGroupAttrVo> getAttrgroupBySpuId(Long spuId, Long catalogId);
}

