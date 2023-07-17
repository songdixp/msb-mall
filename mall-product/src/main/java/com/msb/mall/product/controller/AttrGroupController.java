package com.msb.mall.product.controller;

import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;
import com.msb.mall.product.entity.AttrEntity;
import com.msb.mall.product.entity.AttrGroupEntity;
import com.msb.mall.product.service.AttrAttrgroupRelationService;
import com.msb.mall.product.service.AttrGroupService;
import com.msb.mall.product.service.AttrService;
import com.msb.mall.product.service.CategoryService;
import com.msb.mall.product.vo.AttrGroupRelationVO;
import com.msb.mall.product.vo.AttrgroupWithAttrsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 属性分组
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private AttrAttrgroupRelationService relationService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AttrService attrService;


    /**
     * 获取三级分类下面的规格参数 attr
     * /225/withattr?t=1684372019866
     */
    @GetMapping("/{catelogId}/withattr")
    public R AttrgroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        List<AttrgroupWithAttrsVO> list = attrGroupService.getAttrgroupWithAttrsByCatelogId(catelogId);

        return R.ok().put("data",list);
    }

    /**
     * attr/relation 点击新增关联，勾选规格参数，点击确认新增按钮保存
     * @param vos 前端传过来的数组对象[{attr_id:1, attr_group_id:22},...]
     */
    @PostMapping("/attr/relation")
    public R attrRelation(@RequestBody List<AttrGroupRelationVO> vos) {
        relationService.saveBatch(vos);
        return R.ok();
    }

    /**
     * 点击新增关联按钮获取列表数据
     * 前端url /7/noattr/relation
     * @return 返回当前分类下，当前分组所有未关联的规格属性 分页
     */
    @GetMapping("/{attrGroupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String,Object> params,
                            @PathVariable("attrGroupId") Long attrGroupId) {
        PageUtils pageUtils = attrService.queryAttrNoRelation(params, attrGroupId);
        return R.ok().put("page",pageUtils);
    }


    //attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R attrRelationDelete(@RequestBody AttrGroupRelationVO[] vos){
        relationService.deleteAttrRelation(vos);
        return R.ok();
    }
    @RequestMapping("/{attrGroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrEntity> list = attrService.getAttrByGroupId(attrGroupId);
        return R.ok().put("data",list);
    }
    /**
     * 分页查询列表信息
     * 根据 categoryId 查询
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId){
        // PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }



    /**
     * 前端传过来分组属性id 通过这个id找到分类id catelogId然后递归查找到 catelogPath
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(catelogPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
