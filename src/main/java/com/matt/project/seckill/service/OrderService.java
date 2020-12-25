package com.matt.project.seckill.service;

import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.service.model.OrderModel;

import java.io.UnsupportedEncodingException;

/**
 * @author matt
 * @create 2020-12-11 17:14
 */
public interface OrderService {

    /**
     * 功能：创建订单
     * @author matt
     * @date 2020/12/16
     * @param userId
     * @param itemId
     * @param amount
     * @param promoId
     * @return com.matt.project.seckill.service.model.OrderModel
    */
    OrderModel createOrder(Integer userId, Integer itemId, Integer amount,
                           Integer promoId,String stockLogId) throws BusinessException, UnsupportedEncodingException;



}
