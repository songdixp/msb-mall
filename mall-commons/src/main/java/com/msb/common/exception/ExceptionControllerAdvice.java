package com.msb.common.exception;


import com.msb.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {
    /**
     * 实体类字段验证异常
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handlerValidException(MethodArgumentNotValidException e){
        Map<String, String > map = new HashMap<>();
        e.getFieldErrors().forEach((fieldError)->{
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg())
                .put("data", map);
    }

    /**
     * 系统其他异常信息
     * @param throwable
     * @return
     */
    @ExceptionHandler(Throwable.class)
    public R handlerException(Throwable throwable){
        log.error("product服务错误信息：", throwable);
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg())
                .put("data", throwable.getMessage());
    }

}
