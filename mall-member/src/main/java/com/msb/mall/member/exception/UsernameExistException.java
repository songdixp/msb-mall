package com.msb.mall.member.exception;

public class UsernameExistException extends RuntimeException {

    public UsernameExistException(){
        super("用户名已存在");
    }
}
