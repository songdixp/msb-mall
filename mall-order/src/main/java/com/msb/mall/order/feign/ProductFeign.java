package com.msb.mall.order.feign;

import com.msb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name="mall-product")
public interface ProductFeign {

    /*
    * order 调用远程 product 服务的controller 方法
    * */
    @RequestMapping("/queryAll")
    public R queryAllBrand();
}
