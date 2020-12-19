package com.matt.project.seckill.controller;

import com.alibaba.fastjson.JSON;
import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.error.EnumBusinessError;
import com.matt.project.seckill.response.CommonReturnType;
import com.matt.project.seckill.service.OrderService;
import com.matt.project.seckill.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.util.Enumeration;

/**
 * @author matt
 * @create 2020-12-13 10:41
 */
@RestController
@RequestMapping("/order")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class OrderController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 功能：创建订单
     * @author matt
     * @date 2020/12/16
     * @param itemId
     * @param amount
     * @param promoId
     * @return com.matt.project.seckill.response.CommonReturnType
    */
    @PostMapping("/createorder")
    public CommonReturnType createOrder(@RequestParam(name = "itemId")Integer itemId,
                                        @RequestParam(name = "amount")Integer amount,
                                        @RequestParam(name = "promoId")Integer promoId,
                                        HttpServletRequest request)
            throws BusinessException {

        HttpSession session = request.getSession();

        Object is_login = session.getAttribute("IS_LOGIN");
        // 判断用户是否登录，登录信息保存在session中
        // if (is_login == null || !(Boolean)is_login){
        //     throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        // }
        String userToken = request.getParameter("token");

        if (StringUtils.isEmpty(userToken)){
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }


        UserModel userModel = (UserModel)redisTemplate.opsForValue().get(userToken);
        System.out.println(userModel.toString());
        if (userModel == null ) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }


        orderService.createOrder(userModel.getId(),itemId,amount,promoId);

        return CommonReturnType.create(null);
    }

}
