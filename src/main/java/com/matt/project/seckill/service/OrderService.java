package com.matt.project.seckill.service;

import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.service.model.OrderModel;

/**
 * @author matt
 * @create 2020-12-11 17:14
 */
public interface OrderService {

    /*
     * 功能描述: 商品下单
     * @Param: [userId, itemId, amount]
     * @Return: void
     * @Author: matt
     * @Date: 2020/12/11 17:14
     */
    OrderModel createOrder(Integer userId, Integer itemId, Integer amount,
                           Integer promoId) throws BusinessException;





}
