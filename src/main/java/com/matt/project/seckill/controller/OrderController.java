package com.matt.project.seckill.controller;

import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.error.EnumBusinessError;
import com.matt.project.seckill.response.CommonReturnType;
import com.matt.project.seckill.service.OrderService;
import com.matt.project.seckill.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.*;

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

    @PostMapping("/createorder")
    public CommonReturnType createOrder(@RequestParam(name = "itemId")Integer itemId,
                                        @RequestParam(name = "amount")Integer amount,
                                        @RequestParam(name = "promoId")Integer promoId)
            throws BusinessException {

        HttpSession session = request.getSession();
        Object is_login = session.getAttribute("IS_LOGIN");
        if (is_login == null || !(Boolean)is_login){
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }

        UserModel userModel = (UserModel) session.getAttribute("USERMODEL");
        orderService.createOrder(userModel.getId(),itemId,amount,promoId);

        return CommonReturnType.create(null);
    }

}
