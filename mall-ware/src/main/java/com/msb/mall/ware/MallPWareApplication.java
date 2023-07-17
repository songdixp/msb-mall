package com.msb.mall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients("com.msb.mall.ware.feign")
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.msb.mall.ware.dao")
public class MallPWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallPWareApplication.class, args);
    }

}
