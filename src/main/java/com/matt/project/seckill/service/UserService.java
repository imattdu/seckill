package com.matt.project.seckill.service;

import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.service.model.UserModel;

/**
 * @author matt
 * @create 2020-12-06 13:30
 */
public interface UserService {

    UserModel getUserById(Integer id);

    void register(UserModel userModel) throws BusinessException;
    UserModel validateLogin(String telephone,String encrptPassword) throws BusinessException;
}
