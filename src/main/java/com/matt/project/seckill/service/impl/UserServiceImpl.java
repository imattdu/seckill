package com.matt.project.seckill.service.impl;

import com.matt.project.seckill.dao.UserDOMapper;
import com.matt.project.seckill.dao.UserPasswordDOMapper;
import com.matt.project.seckill.dataobject.UserDO;
import com.matt.project.seckill.dataobject.UserPasswordDO;
import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.error.EnumBusinessError;
import com.matt.project.seckill.service.UserService;
import com.matt.project.seckill.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author matt
 * @create 2020-12-06 13:31
 */
@Service
public class UserServiceImpl implements UserService {


    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Override
    public UserModel getUserById(Integer id) {

        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null) {
            return null;
        }

        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        UserModel userModel = convertFromUserDo(userDO,userPasswordDO);


        return userModel;
    }

    @Transactional
    @Override
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            return;
        }

        if (StringUtils.isEmpty(userModel.getName())) {
            return;
        }


        UserDO userDO = convertFromUserModel(userModel);

        try {
            userDOMapper.insertSelective(userDO);
        } catch (Exception e) {
           throw new BusinessException(EnumBusinessError.UNKOWN_ERROR,"手机号重复");
        }


        userModel.setId(userDO.getId());
        UserPasswordDO userPasswordDO = convertPasswordFromUserModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);


    }

    @Override
    public UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException {


        UserDO userDO = userDOMapper.selectByTelePhone(telephone);
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        if (userDO == null || userPasswordDO == null || !encrptPassword.equals(userPasswordDO.getEncrptPassword())) {
            throw new BusinessException(EnumBusinessError.USER_NOT_PATCH);
        }

        return convertFromUserDo(userDO,userPasswordDO);
    }

    private UserModel convertFromUserDo(UserDO userDO,UserPasswordDO userPasswordDO) {

        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);

        if (userPasswordDO != null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }

    private UserDO convertFromUserModel(UserModel userModel) {
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }

    private UserPasswordDO convertPasswordFromUserModel(UserModel userModel) {
        UserPasswordDO userPasswordDO = new UserPasswordDO();

        userPasswordDO.setUserId(userModel.getId());
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        return userPasswordDO;
    }
}
