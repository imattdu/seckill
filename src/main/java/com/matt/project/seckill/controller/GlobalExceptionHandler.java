package com.matt.project.seckill.controller;

import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.error.EnumBusinessError;
import com.matt.project.seckill.response.CommonReturnType;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author matt
 * @create 2020-12-14 15:21
 */
@ControllerAdvice
public class GlobalExceptionHandler {


    /*
     * 功能描述: 异常处理
     * @Param: [httpServletRequest, httpServletResponse, ex]
     * @Return: com.matt.project.seckill.response.CommonReturnType
     * @Author: matt
     * @Date: 2020/12/15 11:40
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonReturnType doError(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Exception ex) {
        ex.printStackTrace();
        Map<String,Object> responseData = new HashMap<>();
        if(ex instanceof BusinessException){
            BusinessException businessException = (BusinessException)ex;
            responseData.put("errCode",businessException.getErrCode());
            responseData.put("errMsg",businessException.getErrMsg());
        }else if(ex instanceof ServletRequestBindingException){
            responseData.put("errCode", EnumBusinessError.URL_BIND_ERROR.getErrCode());
            responseData.put("errMsg",EnumBusinessError.URL_BIND_ERROR.getErrMsg());
        }else if(ex instanceof NoHandlerFoundException){
            responseData.put("errCode",EnumBusinessError.NOT_FOUOND.getErrCode());
            responseData.put("errMsg",EnumBusinessError.NOT_FOUOND.getErrMsg());
        }else{
            responseData.put("errCode", EnumBusinessError.UNKOWN_ERROR.getErrCode());
            responseData.put("errMsg",EnumBusinessError.UNKOWN_ERROR.getErrMsg());
        }
        return CommonReturnType.create(responseData,"fail");
    }
}
