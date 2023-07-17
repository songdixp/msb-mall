package com.msb.mall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


public class OrderConfirmVO {

    //收货地址
    @Setter@Getter
    List<MemberAddressVO> addressVOS;

    //购物车中所有选中的商品信息
    @Setter@Getter
    List<OrderItemVO> orderItemVOS;
    // 支付方式
    // 发票信息
    // 优惠信息

    //获取总数量
    public Integer getCountNum(){
        int count =0;
        if(orderItemVOS!=null){
            for (OrderItemVO orderItemVO : orderItemVOS) {
                count+=orderItemVO.getCount();
            }
        }
        return count;
    }
    public BigDecimal getTotalPrice(){
        BigDecimal sum = new BigDecimal(0);
        if (orderItemVOS!=null){
            for (OrderItemVO orderItemVO : orderItemVOS) {
                BigDecimal count = new BigDecimal(orderItemVO.getCount());
                BigDecimal itemPrice = count.multiply(orderItemVO.getPrice());
                sum = sum.add(itemPrice);
            }
        }
        return sum;
    }

    public BigDecimal getPayTotalPrice(){
        return getTotalPrice();
    }

}
