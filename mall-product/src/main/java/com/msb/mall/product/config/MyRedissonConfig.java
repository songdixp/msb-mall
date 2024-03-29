package com.msb.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedissonConfig {

    @Bean
    public RedissonClient redissonClient(){
        Config config =new Config();
        config.useSingleServer()
                        .setAddress("redis://192.168.31.225:6379");

        return Redisson.create(config);
    }

}
