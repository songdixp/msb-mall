package com.msb.mall.product;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.msb.mall.product.entity.BrandEntity;
import com.msb.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

@SpringBootTest
class MallProductApplicationTests {
    @Autowired
    BrandService brandService;
    @Test
    void contextLoads() {
        BrandEntity entity = new BrandEntity();
        entity.setName("魅族");
        brandService.save(entity);
    }

    @Test
    void selectAll() {
        List<BrandEntity> list = brandService.list();
        for (BrandEntity entity : list) {
            System.out.println(entity);
        }
    }

    @Test
    void selectById() {
        List<BrandEntity> list = brandService
                .list(new QueryWrapper<BrandEntity>().eq("brand_id",2));
        for (BrandEntity entity : list) {
            System.out.println(entity);
        }
    }


}
