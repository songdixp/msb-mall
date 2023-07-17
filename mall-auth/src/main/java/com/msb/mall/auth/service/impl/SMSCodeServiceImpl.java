package com.msb.mall.auth.service.impl;

import com.msb.common.constant.SMSConstant;
import com.msb.common.exception.BizCodeEnum;
import com.msb.common.utils.R;
import com.msb.mall.auth.feign.ThirdPartFeignService;
import com.msb.mall.auth.service.SMSCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SMSCodeServiceImpl implements SMSCodeService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThirdPartFeignService thirdPartFeignService;
    /**
     * 存储随机验证码到redis缓存中
     */
    @Override
    public R saveCodeInRedis(String random4Code, String phone) {
        // 先去redis中取 code 防止60s重复发送code
        Object redisCode = redisTemplate.opsForValue().get(SMSConstant.SMS_CODE_PREFIX + phone);
        if (redisCode != null){
            String redisCodeStr = redisCode.toString();
            // 把过期时间截取出来
            Long expireTime = Long.parseLong(redisCodeStr.split("_")[1]);
            if (System.currentTimeMillis() -expireTime <= 60000){
                //验证码发送时间不足60s 返回错误码
                return R.error(BizCodeEnum.VALID_SMS_EXCEPTION.getCode(),
                        BizCodeEnum.VALID_SMS_EXCEPTION.getMsg());
            }
        }

        // 没有取到，就存储redis，先拼接过期时间
        String random4CodeWithExpireTime = random4Code+"_" + System.currentTimeMillis();
        System.out.println("拼接之后的随机码字符串 = " + random4CodeWithExpireTime);
        // 把传过来的4位随机数字存放到 redis中 sms_19969083172: 1234
        redisTemplate.opsForValue().set(SMSConstant.SMS_CODE_PREFIX+phone, random4CodeWithExpireTime, 10, TimeUnit.MINUTES);
        return R.ok();
    }
}
