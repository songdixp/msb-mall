package com.msb.mall.order.feign;

import com.msb.mall.order.vo.MemberAddressVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("mall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    public List<MemberAddressVO> getAddressByMemberId(@PathVariable("memberId") Long memberId);


}
