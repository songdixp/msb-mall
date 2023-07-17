package com.msb.mall.auth.controller;

import com.msb.common.constant.SMSConstant;
import com.msb.common.constant.URLConstant;
import com.msb.common.utils.R;
import com.msb.mall.auth.feign.MemberFeignService;
import com.msb.mall.auth.feign.ThirdPartFeignService;
import com.msb.mall.auth.service.SMSCodeService;
import com.msb.mall.auth.vo.UserRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Controller
public class RegController {
    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    SMSCodeService smsCodeService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    /* @GetMapping("/reg.html")
    public String reg(){

        return "reg";
    } */

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendSMSCode(@RequestParam String phone){
        // 生成4为数字的随机数
        String random4Code = String.valueOf(new Random().nextInt(10000));
        thirdPartFeignService.sendSMSCode(phone, random4Code);

        smsCodeService.saveCodeInRedis(random4Code, phone);
        return R.ok();
    }

    @PostMapping("/sms/register")
    public String register(UserRegisterVO vo, BindingResult result, Model model){
        Map<String,String> map = new HashMap<>();
        if (result.hasErrors()){
            //说明vo对象字段校验有错误
            List<FieldError> fieldErrors = result.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                String field = fieldError.getField();
                String defaultMessage = fieldError.getDefaultMessage();
                map.put(field, defaultMessage);
            }
            model.addAttribute("error", map);
            System.out.println("vo对象字段校验有错误");
            return "reg";
        }else{
            // vo对象没有错误，验证code是否正确
            String redisCodeWithExpireTime = (String) redisTemplate.opsForValue().get(SMSConstant.SMS_CODE_PREFIX + vo.getPhone());
            String code = redisCodeWithExpireTime.split("_")[0];
            if (!code.equals(vo.getCode())){
                //验证码不正确 返回给reg页面错误信息
                map.put("code", "验证码错误"); //
                model.addAttribute("error", map);
                System.out.println("验证码不正确");
                return "/reg";
            }else {
                //验证码正确 删除缓存验证码
                redisTemplate.delete(SMSConstant.SMS_CODE_PREFIX + vo.getPhone());
                System.out.println("验证码正确，删除缓存验证码...");
                //完成注册功能
                R r = memberFeignService.register(vo);
                if (r.getCode()==0){
                    //注册成功
                    System.out.println("验证码正确，注册功能正确，注册成功");
                    // return "redirect:http://msb.auth.com/login.html";
                    return "redirect:"+ URLConstant.AUTH_LOGIN_HTML;
                }else{
                    //注册失败
                    System.out.println("验证码正确，但是注册失败");
                    map.put("msg", "注册失败");
                    model.addAttribute("error", map);
                    return "reg";
                }
            }
        }
    }
}
