package com.matt.project.seckill.service;

import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.service.model.UserModel;
import org.apache.ibatis.annotations.Param;

/**
 *
 * @author matt
 * @create 2020-12-06 13:30
 */
public interface UserService {

    /**
     * 功能：根据用户ID查询用户
     * @author matt
     * @date 2020/12/15
     * @param id
     * @return com.matt.project.seckill.service.model.UserModel
    */
    UserModel getUserById(Integer id);


    /**
     * 功能：用户注册
     * @author matt
     * @date 2020/12/15
     * @param userModel
     * @return void
    */
    void register(UserModel userModel) throws BusinessException;


   /**
    * 功能：校验用户登录
    * @author matt
    * @date 2020/12/15
    * @param telephone
    * @param encrptPassword
    * @return com.matt.project.seckill.service.model.UserModel
   */
    UserModel validateLogin(String telephone,String encrptPassword) throws BusinessException;



    UserModel getUserModelByIdInCache(Integer id);

}
