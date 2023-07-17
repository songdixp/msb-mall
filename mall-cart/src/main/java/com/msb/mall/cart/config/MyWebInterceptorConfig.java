package com.msb.mall.cart.config;

import com.msb.mall.cart.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyWebInterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加我们定义的拦截器，拦截哪些请求
        // 我们定义的拦截器中定义了 这些请求preHandler 做哪些事情
        registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/**");
    }
}
