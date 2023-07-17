package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.AttrEntity;
import com.msb.mall.product.vo.AttrResponseVO;
import com.msb.mall.product.vo.AttrVO;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttrVO(AttrVO attrVO);

    PageUtils queryBasePage(Map<String, Object> params, Long catelogId, String attrType);

    AttrResponseVO getAttrInfo(Long attrId);

    void updateBaseAttr(AttrVO attr);

    void removeByIdsDetail(Long[] attrIds);

    /**
     * 根据属性组attr_group_id 找到具体的属性attr List列表
     */
    List<AttrEntity> getAttrByGroupId(Long attrGroupId);

    PageUtils queryAttrNoRelation(Map<String, Object> params, Long attrGroupId);

    List<Long> selectSearchTypeIds(List<Long> attr_ids);
}

