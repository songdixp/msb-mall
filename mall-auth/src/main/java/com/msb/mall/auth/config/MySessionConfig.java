package com.msb.mall.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class MySessionConfig {
    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        defaultCookieSerializer.setDomainName("msb.com");
        defaultCookieSerializer.setCookieName("msb_session");
        return defaultCookieSerializer;
    }

    /**
     * 对存储在Redis中的数据指定序列化的方式
     */
    @Bean
    public RedisSerializer<Object> redisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }
}
