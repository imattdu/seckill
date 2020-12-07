package com.matt.project.seckill.error;

/**
 * @author matt
 * @create 2020-12-06 14:17
 */
public interface CommonError {

    String getErrMsg();

    Integer getErrCode();

    CommonError setErrMsg(String message);
}
