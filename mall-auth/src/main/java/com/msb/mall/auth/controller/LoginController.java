package com.msb.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.msb.common.constant.AuthConstant;
import com.msb.common.constant.URLConstant;
import com.msb.common.dto.MemberLoginDTO;
import com.msb.common.dto.MemberSessionDTO;
import com.msb.common.utils.R;
import com.msb.mall.auth.feign.MemberFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {
    @Autowired
    MemberFeignService memberFeignService;
    /**
     * 登录
     */
    @PostMapping("/login")
    public String login(MemberLoginDTO dto,
                        HttpSession session){
        R r = memberFeignService.login(dto);
        String entity = (String) r.get("entity");
        MemberSessionDTO memberSessionDTO = JSON.parseObject(entity, MemberSessionDTO.class);
        if (r.getCode()==0){
            session.setAttribute(AuthConstant.AUTH_SESSION_REDIS,memberSessionDTO);
            // return "redirect:http:msb.mall.com/index";
            return "redirect:"+ URLConstant.MALL_HOME;
        }
        //登录失败，跳转登录页面
        /* Map<String, Object>map = new HashMap<>();
        map.put("msg", "用户名或者密码错误，登录失败"); */
        // redirectAttributes.addAttribute("errors", r.get("msg"));
        System.out.println("登录失败");
        // return "redirect:http://msb.auth.com/login.html";
        return "redirect:"+ URLConstant.AUTH_LOGIN_HTML;
    }

    /* @GetMapping("/login.html")
    public String login(){

        return "login";
    } */


}
