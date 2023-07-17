package com.msb.mall.product.feign;

import com.msb.common.dto.SkuReductionDTO;
import com.msb.common.dto.SpuBoundsDTO;
import com.msb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mall-coupon")
public interface CouponFeignService {

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveFullReductionInfo(@RequestBody SkuReductionDTO dto);

    @RequestMapping("/coupon/spubounds/saveSpuBounds")
    R saveSpuBounds(@RequestBody SpuBoundsDTO spuBoundsDTO);
}
