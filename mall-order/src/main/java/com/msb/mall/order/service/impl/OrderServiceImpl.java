package com.msb.mall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.msb.common.dto.MemberSessionDTO;
import com.msb.common.utils.PageUtils;
import com.msb.common.utils.Query;
import com.msb.mall.order.dao.OrderDao;
import com.msb.mall.order.entity.OrderEntity;
import com.msb.mall.order.feign.CartFeignService;
import com.msb.mall.order.feign.MemberFeignService;
import com.msb.mall.order.interceptor.AuthInterceptor;
import com.msb.mall.order.service.OrderService;
import com.msb.mall.order.vo.MemberAddressVO;
import com.msb.mall.order.vo.OrderConfirmVO;
import com.msb.mall.order.vo.OrderItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取结算页面中需要的信息
     * 1收货地址 从member中远程查询
     * 2购物车中选中的商品 远程调用cart服务
     * 3总的金额，这个在VO中自动计算了
     * @return
     */
    @Override
    public OrderConfirmVO getOrderConfirmVO() {
        OrderConfirmVO orderConfirmVO = new OrderConfirmVO();
        MemberSessionDTO memberSessionDTO = AuthInterceptor.threadLocal.get();
        //获取请求上下文
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            //与主线程请求上线文保持同步
            RequestContextHolder.setRequestAttributes(requestAttributes);
            Long id = memberSessionDTO.getId();
            //1查询收货地址
            List<MemberAddressVO> addressVOS = memberFeignService.getAddressByMemberId(id);
            orderConfirmVO.setAddressVOS(addressVOS);
        }, executor);

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            //与主线程请求上线文保持同步
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //2查询购物车中选中的信息
            List<OrderItemVO> orderItemVOS = cartFeignService.queryCheckedCartItemList();
            System.out.println("能接受到orderItemVOS吗"+orderItemVOS);
            orderConfirmVO.setOrderItemVOS(orderItemVOS);
        }, executor);

        //主线程等待
        try {
            CompletableFuture.allOf(future1,future2).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return orderConfirmVO;
    }

}