package com.msb.mall.auth.service;

import com.msb.common.utils.R;

public interface SMSCodeService {

    R saveCodeInRedis(String random4Code, String phone);
}
