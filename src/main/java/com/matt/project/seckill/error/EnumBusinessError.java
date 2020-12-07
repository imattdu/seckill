package com.matt.project.seckill.error;

/**
 * @author matt
 * @create 2020-12-06 14:17
 */
public enum EnumBusinessError implements CommonError {

    PARAMETER_VALIDATION_ERROR(10001,"参数错误"),
    UNKOWN_ERROR(10002,"未知错误"),
    // 10001
    USER_NOT_EXISTS(20001,"用户不存在"),
    USER_NOT_PATCH(20002,"用户名密码不匹配")
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
