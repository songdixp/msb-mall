package com.msb.mall.order.feign;

import com.msb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/*
* @FeignClient 指明我们要从注册中心中发现的服务的名称
* */
@FeignClient(name="mall-product")
public interface ProductFeign {

    /*
    * order 调用远程 product 服务的 BrandController 方法
    * */
    @RequestMapping("product/brand/queryAll")
    public R queryAllBrand();
}
