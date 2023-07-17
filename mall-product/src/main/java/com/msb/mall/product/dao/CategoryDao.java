package com.msb.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.mall.product.entity.CategoryEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品三级分类
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {

    List<CategoryEntity> getCategoryLevel1();

    List<CategoryEntity> selectCatalogLevelByParentCid(Long catId);
}
