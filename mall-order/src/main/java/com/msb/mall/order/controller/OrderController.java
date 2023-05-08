package com.msb.mall.order.controller;

import java.util.Arrays;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msb.mall.order.entity.OrderEntity;
import com.msb.mall.order.service.OrderService;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.R;



/**
 * 订单
 */
@RefreshScope // 动态刷新注解
@RestController
@RequestMapping("/order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Value("${user.userName}")
    private String userName;
    @Value("${user.age}")
    private Integer age;
    @RequestMapping("/users")
    public R queryUser(){
        System.out.println("查询所有的用户 user...");
        return R.ok().put("userName", userName).put("age", age);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
        public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
        public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
        public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
        public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
        public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
