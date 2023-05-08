package com.msb.mall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;

import com.msb.mall.product.dao.CategoryDao;
import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> queryPageTree(Map<String, Object> params) {
        //1 查询所有的商品分类信息
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        //2 将商品分类信息拆解为树形结构
        // 2.1 遍历出所有的大类 parent_cid=0
        List<CategoryEntity> list = categoryEntities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0) //过滤出parent_cid=0的元素
                //2.2 一级大类找到所有的小类 递归查询，例如找到手机 0 那么还要找到下面的子类、品牌，所以怎样存储这个数据
                .map(categoryEntity -> {
                    categoryEntity.setChildrens(getCategoryChildrens(categoryEntity, categoryEntities)); // 添加子类型，这里需要定义方法
                    return categoryEntity;
                })
                // 2.3 进行数据分类排序，热门分类在前，冷门在后 entity1 比较参数
                /*.sorted((entity1, entity2) -> {
                    return (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort());
                })*/
                .sorted(Comparator.comparingInt(entity -> (entity.getSort() == null ? 0 : entity.getSort())))
                //2.4 收集数据,转换成List集合
                .collect(Collectors.toList());
        return list;
    }

    /**
     * 逻辑删除
     * @param catIds  Long 类型的数组 传入catId
     */

    @Override
    public void removeCategoryByIds(List<Long> catIds) {
        System.out.println("逻辑删除的id："+catIds.toString());
        baseMapper.deleteBatchIds(catIds);
    }

    /**
     * 查找大类下面的小类，递归获取
     *
     * @param categoryEntity   某个大类
     * @param categoryEntities 所有的类别数据
     * @return 返回 List 集合 包含所有小类
     */
    private List<CategoryEntity> getCategoryChildrens(CategoryEntity categoryEntity,
                                                      List<CategoryEntity> categoryEntities) {
        List<CategoryEntity> collect = categoryEntities.stream()
                .filter(subCategoryEntity -> {
                    //根据大类找到直属小类 子类的父id == 当前大类的 id
                    // long 类型比较不在-128 127 之间数据 是Long对象
                    return subCategoryEntity.getParentCid().equals(categoryEntity.getCatId());
                })
                //找到小类之后，如果还有小类，就找小类的小类，这里就用递归：自己调用自己
                /*
                * .map(subCategoryEntity->{
                    subCategoryEntity.setChildrens(getCategoryChildrens(subCategoryEntity, categoryEntities));
                    return subCategoryEntity;
                })*/
                .peek(subCategoryEntity -> subCategoryEntity.setChildrens(getCategoryChildrens(subCategoryEntity, categoryEntities)))
                .sorted(Comparator.comparingInt(entity -> (entity.getSort() == null ? 0 : entity.getSort())))
                .collect(Collectors.toList());
                return collect;
    }
}