package com.msb.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.mall.product.dao.CategoryBrandRelationDao;
import com.msb.mall.product.entity.BrandEntity;
import com.msb.mall.product.entity.CategoryBrandRelationEntity;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.BrandService;
import com.msb.mall.product.service.CategoryBrandRelationService;
import com.msb.mall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    CategoryBrandRelationDao relationDao;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    CategoryBrandRelationEntity entity = null;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询品牌名称 和 种类名称
     * 保存到 categoryBrandRelation 中间表中
     *
     * @param categoryBrandRelation 中间表实体类
     */
    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        CategoryEntity categoryEntity = categoryService.getById(catelogId);
        BrandEntity brandEntity = brandService.getById(brandId);

        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());
        this.save(categoryBrandRelation);
    }

    /**
     * 级联更新 categoryBrandRelation表 brand_name字段
     */

    @Override
    public void updateBrandName(Long brandId, String name) {
        entity = new CategoryBrandRelationEntity();
        entity.setBrandName(name);
        this.update(entity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    /**
     * 更新categoryBrandRelation表 catelog_name字段
     */
    @Override
    public void updateCatelogName(Long catId, String name) {
        entity = new CategoryBrandRelationEntity();
        entity.setCatelogName(name);
        this.update(entity,
                new UpdateWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
    }

    /**
     * 点击选择分类，获取分类下面的所有品牌
     * @param catId
     */
    @Override
    public List<CategoryBrandRelationEntity> getBrandsList(Long catId) {
        List<CategoryBrandRelationEntity> categoryBrandRelationEntities = relationDao.selectList(
                new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        return categoryBrandRelationEntities;
    }

}