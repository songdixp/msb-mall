package com.msb.mall.order.web;

import com.msb.mall.order.service.OrderService;
import com.msb.mall.order.vo.OrderConfirmVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderWebController {
    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model){
        // 确认订单页面需要的信息，返回所有选中的购物项
        OrderConfirmVO confirmVO = orderService.getOrderConfirmVO();
        model.addAttribute("confirmVo", confirmVO);
        return "confirm";
    }

}
