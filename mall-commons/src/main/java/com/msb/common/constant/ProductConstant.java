package com.msb.common.constant;

public class ProductConstant {
    /**
     * 商品属性
     */
    public enum Attr{
        ATTR_TYPE_BASE(1,"基本信息"),
        ATTR_TYPE_SALE(0,"销售信息");
        private final Integer code;
        private final String msg;

        Attr(Integer code, String msg){
            this.code = code;
            this.msg = msg;
        }
        public Integer getCode(){
            return code;
        }

        public String getMsg(){
            return msg;
        }
    }

    public enum PublishStatus{
        NEW(0,"新建"),
        UP(1,"上架"),
        DOWN(2,"下架");
        private final Integer code;
        private final String msg;

        PublishStatus(Integer code, String msg){
            this.code = code;
            this.msg = msg;
        }
        public Integer getCode(){
            return code;
        }

        public String getMsg(){
            return msg;
        }
    }
}
