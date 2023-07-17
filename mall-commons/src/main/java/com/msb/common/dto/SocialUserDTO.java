package com.msb.common.dto;

import lombok.Data;

@Data
public class SocialUserDTO {
    private String access_token;
    private Long remind_in;
    private Long expires_in;
    private String uid;
    private Boolean isRealName;
}
