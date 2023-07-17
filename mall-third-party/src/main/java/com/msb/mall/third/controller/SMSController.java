package com.msb.mall.third.controller;

import com.msb.common.utils.R;
import com.msb.mall.third.utils.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SMSController {
    @Autowired
    SmsComponent smsComponent;


    @GetMapping("/sms/sendCode")
    public R sendSMSCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        smsComponent.sendSmsCode(phone, code);
        return R.ok();
    }
}