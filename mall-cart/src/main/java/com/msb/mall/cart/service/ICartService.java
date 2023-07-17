package com.msb.mall.cart.service;

import com.msb.mall.cart.vo.Cart;
import com.msb.mall.cart.vo.CartItem;

import java.util.List;

public interface ICartService {

    Cart getCartList();

    CartItem addCart(Long skuId, Integer num) throws Exception;

    List<CartItem> queryCheckedCartItemList();
}
