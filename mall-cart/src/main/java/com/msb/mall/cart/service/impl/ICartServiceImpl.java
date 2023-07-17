package com.msb.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.msb.common.constant.CartConstant;
import com.msb.common.dto.MemberSessionDTO;
import com.msb.common.utils.R;
import com.msb.mall.cart.feign.ProductFeignService;
import com.msb.mall.cart.interceptor.AuthInterceptor;
import com.msb.mall.cart.service.ICartService;
import com.msb.mall.cart.vo.Cart;
import com.msb.mall.cart.vo.CartItem;
import com.msb.mall.cart.vo.SkuInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class ICartServiceImpl implements ICartService {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor threadPoolExecutor;


    /**
     * 获取当前用户的所有cartitem 信息
     * @return
     */
    @Override
    public Cart getCartList() {
        BoundHashOperations<String, Object, Object> hashOperations = getCartKeyOperation();
        String key1 = hashOperations.getKey();
        System.out.println("当前用户的key是cart:1吗 = " + key1);

        Set<Object> keys = hashOperations.keys();
        List<CartItem> list = new ArrayList<>();
        for (Object k : keys) {
            System.out.println("查询出的所有key是什么 = " + k);
            String key = (String) k;
            String json = (String)hashOperations.get(key);
            System.out.println("这里查询的是 hash的 key value吗"+json);
            list.add(JSON.parseObject(json, CartItem.class));
        }
        Cart cart = new Cart();
        cart.setItems(list);
        return cart;

    }

    /**
     * 添加购物车
     * 1. 先去redis中查看这个商品是否在cart里面，有的话更新一下数量
     * 2.没有商品则插入商品到redis中
     */
    @Override
    public CartItem addCart(Long skuId, Integer num) throws Exception{

        //查询redis是否存在当前用户的购物车信息
        BoundHashOperations<String, Object, Object> userHashMapOps = getCartKeyOperation();
        Object o = userHashMapOps.get(skuId.toString());
        if (o!=null){
            //说明购物车中已经存在该sku，只需要更新数量
            String json = (String) o;
            CartItem cartItem = JSON.parseObject(json, CartItem.class);
            cartItem.setCount(cartItem.getCount()+num);
            String s = JSON.toJSONString(cartItem);
            userHashMapOps.put(skuId.toString(), s);
            return cartItem;
        }

        //说明购物车中没有这个sku，查询这个sku的详情，然后设置属性，保存到redis中json格式
        CartItem cartItem = new CartItem();

        CompletableFuture future1 = CompletableFuture.runAsync(()->{
            //远程：查询商品的详情 根据skuId
            R r = productFeignService.info(skuId);
            String skuInfoJSON = (String) r.get("skuInfoJSON");
            SkuInfoVO skuInfoVO = JSON.parseObject(skuInfoJSON, SkuInfoVO.class) ;
            // 1 商品详情信息
            cartItem.setSkuId(skuInfoVO.getSkuId());
            cartItem.setCheck(true);
            cartItem.setCount(num);
            cartItem.setPrice(skuInfoVO.getPrice());
            cartItem.setImage(skuInfoVO.getSkuDefaultImg());
            cartItem.setTitle(skuInfoVO.getSkuTitle());
        },threadPoolExecutor);

        CompletableFuture future2 = CompletableFuture.runAsync(()->{
            //2 远程：查询出销售属性
            // cartItem.setSkuAttr();
            List<String> skuSaleAttrs = productFeignService.getSkuSaleAttrs(skuId);
            cartItem.setSkuAttr(skuSaleAttrs);
        },threadPoolExecutor);

        CompletableFuture.allOf(future1, future2).get();
        // 3 存储到redis中
        String s = JSON.toJSONString(cartItem);
        userHashMapOps.put(skuId.toString(), s);

        return cartItem;
    }

    /**
     * 当前用户选中的购物车项集合
     */
    @Override
    public List<CartItem> queryCheckedCartItemList() {
        BoundHashOperations<String, Object, Object> hashOperations = getCartKeyOperation();
        List<Object> cartItems = hashOperations.values();
        if (cartItems!=null) {
            List<CartItem> cartItemList = cartItems.stream()
                    .map((item) ->{
                        String json = (String) item;
                        return JSON.parseObject(json, CartItem.class);
                    })
                    .filter((item)-> item.isCheck())
                    .collect(Collectors.toList());
            return cartItemList;
        }else{
            return null;
        }

    }




    /**
     * 本地线程拿到用户信息 userid，然后查询redis中cart:userid 返回hash Map信息
     * @return  当前用户信息的 hashMap信息
     */
    private BoundHashOperations<String, Object, Object> getCartKeyOperation() {
        //redis中没有，把商品添加到redis中 hash cart:1(用户id)  :{skuID:cartItem}
        //先得到线程中的用户信息
        MemberSessionDTO memberSessionDTO = AuthInterceptor.threadLocal.get();
        String key = CartConstant.CART_PREFIX +memberSessionDTO.getId();
        return redisTemplate.boundHashOps(key);
    }
}
