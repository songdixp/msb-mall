package com.msb.mall.order.controller;

import com.msb.common.utils.R;
import com.msb.mall.order.feign.ProductFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通过 ProductFeign 远程调用 mall-product 服务的controller
 */
@RestController
@RequestMapping("/mall/order")
public class OrderProductController {

    @Autowired
    ProductFeign productFeign;

    @RequestMapping("/products")
    public R queryProduct(){
        return R.ok().put("商品：", productFeign.queryAllBrand());
    }
}
