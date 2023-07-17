package com.msb.mall.ware.controller;

import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;
import com.msb.mall.ware.entity.PurchaseEntity;
import com.msb.mall.ware.service.PurchaseService;
import com.msb.mall.ware.vo.MergeVO;
import com.msb.mall.ware.vo.PurchaseDoneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;



/**
 * 采购信息
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 采购完成
     * {
     *     purchaseId:1,
     *     item:[
     *      {itemId:1, status:3,reason:""},
     *      {itemId:2, status:4,reason:""},
     *      {itemId:3, status:3,reason:"xxx"},
     *     ]
     * }
     */
    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVO vo){
        purchaseService.done(vo);
        return R.ok();
    }

    /**
     * 领取采购单
     * [2,3,4]
     */
    @PostMapping("/receive")
    public R receive(@RequestBody List<Long> purchaseIds){
        purchaseService.receive(purchaseIds);
        return R.ok();
    }

    /**
     * 批量操作--合并整单-点击确定
     * @param vo 前端传过来的 采购单id 和采购单详情对象(id,..)
     */
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVO vo){
        Integer flag = purchaseService.merge(vo);
        if (flag==-1){
            return R.error("merge：合单失败！");
        }
        return R.ok();
    }

    /**
     * 点击批量操作-合并整单，获取下拉列表数据
     */
    @GetMapping("/unreceive/list")
    public R unreceived(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryUnreceived(params);
        return R.ok().put("page",page);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
        public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
        public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
