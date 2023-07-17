package com.msb.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.msb.common.utils.PageUtils;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.vo.Catalog2VO;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询所有的category数据，然后返回树形结构给前端展示
     * @param params
     * @return
     */
    List<CategoryEntity> queryPageTree(Map<String, Object> params);

    /**
     * 逻辑删除 在entity实体类单独设置了
     * TableLogic(value = "1", delval = "0")
     * @param catIds  Long 类型的数组 传入catId
     */
    void removeCategoryByIds(List<Long> catIds);

    Long[] findCatelogPath(Long catelogId);

    void updateDetails(CategoryEntity category);

    List<CategoryEntity> getCategoryLevel1();

    Map<String, List<Catalog2VO>> getCatalog2JSON();

}

