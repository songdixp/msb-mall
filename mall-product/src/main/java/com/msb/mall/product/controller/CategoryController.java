package com.msb.mall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msb.mall.product.entity.CategoryEntity;
import com.msb.mall.product.service.CategoryService;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;


/**
 * 商品三级分类
 */
@RestController
@RequestMapping("/product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryService.queryPage(params);
        return R.ok().put("page", page);
    }

    @RequestMapping("/listTree")
    public R listTree(@RequestParam Map<String, Object> params) {
        List<CategoryEntity> list = categoryService.queryPageTree(params);
        return R.ok().put("data", list);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category) {
        categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryEntity category) {
        categoryService.updateById(category);

        return R.ok();
    }
    /**
     * 批量修改
     */
    @RequestMapping("/updateBatch")
    public R updateBatch(@RequestBody CategoryEntity[] category) {
        // categoryService.updateById(category);

        categoryService.updateBatchById(Arrays.asList(category));
        System.out.println("进来没有？？？？？？"+Arrays.asList(category));
        return R.ok();
    }

    /**
     * 逻辑删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody List<Long> catIds) {
        // categoryService.removeByIds(Arrays.asList(catIds));真实删除
        categoryService.removeCategoryByIds(catIds);
        return R.ok();
    }

}
