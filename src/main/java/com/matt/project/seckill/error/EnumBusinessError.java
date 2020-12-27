package com.matt.project.seckill.error;

/**
 * @author matt
 * @create 2020-12-06 14:17
 */
public enum EnumBusinessError implements CommonError {


    PARAMETER_VALIDATION_ERROR(10001,"参数错误"),
    UNKOWN_ERROR(10002,"未知错误"),
    NOT_FOUOND(10003,"没有找到对应的访问路径"),
    URL_BIND_ERROR(10004,"url绑定路由问题"),

    USER_INFO_EMPTY(20001,"用户名或者密码为空"),
    USER_NOT_EXISTS(20002,"用户不存在"),
    USER_NOT_PATCH(20003,"用户名密码不匹配"),
    USER_NOT_LOGIN(20004,"用户未登录"),
    OPT_CODE_ERROR(20005,"验证码不正确"),

    ITEM_AMOUNT_ERROR(30001,"商品数量不合法"),
    ITEM_STOCK_NOT_ENOUGH(30002,"库存不足"),
    ITEM_PARAM_ERROR(30003,"商品参数不合法"),
    ITEM_NOT_EXIST(30004,"商品不存在"),
    ITEM_STOCK_ERROR(30005,"更新库存失败"),

    PROMO_ERROR(40001,"不存在该活动"),
    PROMO_EXPIRE(40002,"活动过期"),


    ORODER_GENERATE_PROMO_TOKEN(50001,"生成秒杀token失败")

    ;

    private int errCode;
    private String errMessage;

    private EnumBusinessError (Integer errCode, String errMessage) {
        this.errCode = errCode;
        this.errMessage = errMessage;
    }

    @Override
    public String getErrMsg() {
        return this.errMessage;
    }

    @Override
    public Integer getErrCode() {
        return this.errCode;
    }

    @Override
    public CommonError setErrMsg(String message) {

        this.errMessage = message;
        return this;
    }
}
