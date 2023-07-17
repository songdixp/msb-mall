package com.msb.mall.auth.feign;

import com.msb.common.dto.MemberLoginDTO;
import com.msb.common.dto.SocialUserDTO;
import com.msb.common.utils.R;
import com.msb.mall.auth.vo.UserRegisterVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("mall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVO vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody MemberLoginDTO dto);

    @PostMapping("/member/member/oauth2/login")
    R socialLogin(@RequestBody SocialUserDTO dto);
}
