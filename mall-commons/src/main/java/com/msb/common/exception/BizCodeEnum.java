package com.msb.common.exception;


/**
 * 错误编码和错误信息的枚举类
 * 10 通用
 * 11 商品
 * 12 订单
 * 13 购物车
 * 14 物流
 * 15 会员
 *
 */
public enum BizCodeEnum {

    UNKNOWN_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001, "参数格式异常"),
    VALID_SMS_EXCEPTION(10010, "60s 之后才能发送验证码"),
    PRODUCT_UP_EXCEPTION(11001, "商品上架异常"),
    USERNAME_EXIST_EXCEPTION(15001, "参数格式异常"),
    PHONE_EXIST_EXCEPTION(15002, "参数格式异常"),
    PASSWORD_FAILED_EXCEPTION(15003, "密码不正确"),

    ;

    private final Integer code;
    private final String msg;


    BizCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
