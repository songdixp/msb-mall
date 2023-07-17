package com.msb.mall.search.controller;

import com.msb.common.dto.es.SkuESModel;
import com.msb.common.exception.BizCodeEnum;
import com.msb.common.utils.R;
import com.msb.mall.search.service.ElasticSearchSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSearchSaveController {
    @Autowired
    private ElasticSearchSaveService elasticSearchSaveService;

    @PostMapping("/product/up")
    public R productUpStatus(@RequestBody List<SkuESModel> skuESModels){
        Boolean success = null;
        try {
            success = elasticSearchSaveService.productUpStatus(skuESModels);
        } catch (IOException e) {
            log.error("skuESModels上传ES服务异常：{}",e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),
                    BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (success){
            return R.ok().put("upSuccess",success);
        }
        return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),
                BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
    }

}
