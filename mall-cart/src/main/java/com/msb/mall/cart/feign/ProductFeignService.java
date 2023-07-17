package com.msb.mall.cart.feign;

import com.msb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("mall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skuinfo/saleAttr/{skuId}")
    public List<String> getSkuSaleAttrs(@PathVariable("skuId") Long skuId);
}
