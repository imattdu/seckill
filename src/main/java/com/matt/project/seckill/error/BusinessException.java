package com.matt.project.seckill.error;

/**
 * @author matt
 * @create 2020-12-06 14:27
 */
public class BusinessException extends Exception implements CommonError {


    private CommonError commonError;

    public BusinessException(CommonError commonError) {
        super();
        this.commonError = commonError;
    }

    public BusinessException(CommonError commonError,String errMessage) {
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMessage);
    }

    @Override
    public String getErrMsg() {
        return this.commonError.getErrMsg();
    }

    @Override
    public Integer getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public CommonError setErrMsg(String message) {

        this.commonError.setErrMsg(message);
        return this;

    }
}
