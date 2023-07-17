package com.msb.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.msb.common.constant.AuthConstant;
import com.msb.common.constant.URLConstant;
import com.msb.common.constant.WeiboConstant;
import com.msb.common.dto.MemberSessionDTO;
import com.msb.common.dto.SocialUserDTO;
import com.msb.common.utils.HttpUtils;
import com.msb.common.utils.R;
import com.msb.mall.auth.feign.MemberFeignService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class OAuth2Controller {
    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * 先获取 code，拿到code之后才获取Token
     */
    @RequestMapping("/oauth/weibo/success")
    public String weiboAuth(@RequestParam("code") String code,
                            HttpSession session) throws Exception {
        //从url上就能拉到code了，然后需要获取token信息
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("client_id",WeiboConstant.CLIENT_ID);
        bodyMap.put("client_secret",WeiboConstant.CLIENT_SECRET);
        bodyMap.put("grant_type",WeiboConstant.GRANT_TYPE);
        bodyMap.put("redirect_uri",WeiboConstant.REDIRECT_URI);
        bodyMap.put("code", code);
        // 使用HttpUtils post方式提交请求 获取token
        HttpResponse httpResponse = HttpUtils.doPost(
                WeiboConstant.HOST,
                WeiboConstant.ACCESS_TOKEN_PATH,
                WeiboConstant.POST,
                new HashMap<>(),
                new HashMap<>(),
                bodyMap);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode != 200){
            //说明获取token失败
            // return "redirect:http://msb.mall.com/login.html";
            return "redirect:"+ URLConstant.MALL_LOGIN_HTML;
        }
        //获取token成功 需要转换成entity实体类对象 然后调用member服务进行会员注册操作
        String httpEntityJson = EntityUtils.toString(httpResponse.getEntity());
        SocialUserDTO socialUserDTO = JSON.parseObject(httpEntityJson, SocialUserDTO.class);
        R r = memberFeignService.socialLogin(socialUserDTO);
        if (r.getCode()!=0){
            //登录错误
            return "redirect:"+ URLConstant.MALL_LOGIN_HTML;
        }
        String entity = (String) r.get("entity");
        MemberSessionDTO memberSessionDTO = JSON.parseObject(entity, MemberSessionDTO.class);
        session.setAttribute(AuthConstant.AUTH_SESSION_REDIS, memberSessionDTO);
        // 注册成功需要跳转到商城首页
        // return "redirect:http://msb.mall.com/home";
        return "redirect:" + URLConstant.MALL_HOME;
    }

}
