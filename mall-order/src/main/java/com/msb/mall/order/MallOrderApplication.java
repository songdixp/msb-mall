package com.msb.mall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/*
* 开启远程调用服务，发现feign的接口调用对应的远程服务
* */
@EnableRedisHttpSession
@EnableFeignClients(basePackages = "com.msb.mall.order.feign")
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.msb.mall.order.dao")
public class MallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallOrderApplication.class, args);
    }

}
