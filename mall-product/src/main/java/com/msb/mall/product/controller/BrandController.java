package com.msb.mall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.msb.common.valid.groups.AddGroupsInterface;
import com.msb.common.valid.groups.UpdateGroupsInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msb.mall.product.entity.BrandEntity;
import com.msb.mall.product.service.BrandService;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;

import javax.validation.Valid;


/**
 * 品牌
 */

@RestController
@RequestMapping("/product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;


    /**
     * 测试接口，查询所有品牌
     * @return
     */
    @RequestMapping("/queryAll")
    public R queryAllBrand(){
        System.out.println("进入product服务：/product/brand/queryAll");
        BrandEntity brand = new BrandEntity();
        brand.setName("测试数据：华为");
        return R.ok().put("brand", brand);
    }

    /**
     * 所有品牌列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 查询品牌信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 添加品牌
     * 走的是添加校验的分组 AddGroupInterface
     */
    @RequestMapping("/save")
    public R save(
            @Validated(AddGroupsInterface.class)
            @RequestBody BrandEntity brand) {
        brandService.save(brand);
        return R.ok();
    }


    /**
     * 更新品牌
     * 走更新分组校验 UpdateGroupsInterface
     */
    @RequestMapping("/update")
    public R update(
            @Validated(UpdateGroupsInterface.class)
            @RequestBody BrandEntity brand) {
        // brandService.updateById(brand);
        brandService.updateDetails(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
