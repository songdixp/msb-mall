package com.msb.common.dto;

import lombok.Data;

@Data
public class MemberRegisterDTO {

    private String userName; // 用户名|账号
    private String password ; // 密码
    private String phone;

}
