package com.matt.project.seckill.controller;

import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.error.EnumBusinessError;
import com.matt.project.seckill.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author matt
 * @create 2020-12-06 14:52
 */
public class BaseController {

    public static final String CONTENT_TYPE_FORMED="application/x-www-form-urlencoded";

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Object handlerException (HttpServletRequest request, Exception ex) {

        CommonReturnType commonReturnType = new CommonReturnType();
        commonReturnType.setStatus("fail");
        Map<String,String> map = new HashMap<>();
        if (ex instanceof BusinessException) {
            BusinessException bex = (BusinessException)ex;


            map.put("errCode",bex.getErrCode().toString());
            map.put("errMsg",bex.getErrMsg());

        } else {
            map.put("errCode", EnumBusinessError.UNKOWN_ERROR.getErrCode().toString());
            map.put("errMsg",EnumBusinessError.UNKOWN_ERROR.getErrMsg());
        }
        commonReturnType.setData(map);
        return commonReturnType;
    }
}
