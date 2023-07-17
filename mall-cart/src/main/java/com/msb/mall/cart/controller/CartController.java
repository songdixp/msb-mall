package com.msb.mall.cart.controller;

import com.msb.mall.cart.service.ICartService;
import com.msb.mall.cart.vo.Cart;
import com.msb.mall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class CartController {
    @Autowired
    private ICartService iCartService;

    @GetMapping("/getCheckedCartItems")
    @ResponseBody
    public List<CartItem> queryCheckedCartItemList(){
        List<CartItem> cartItems = iCartService.queryCheckedCartItemList();
        return cartItems;
    }


    @GetMapping("/cart_list")
    public String queryCartList(Model model){
        Cart cart = iCartService.getCartList();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    @GetMapping("/addCart")
    public String addCart(@RequestParam("skuId") Long skuId,
                          @RequestParam("num") Integer num,
                          Model model){
        // 把商品加入购物车
        CartItem cartItem = null;
        try {
            cartItem = iCartService.addCart(skuId, num);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("item",cartItem);
        return "success";
    }

}
