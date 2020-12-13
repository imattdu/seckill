package com.matt.project.seckill.controller;

import com.fasterxml.jackson.databind.ser.Serializers;
import com.matt.project.seckill.dao.UserDOMapper;
import com.matt.project.seckill.dataobject.UserDO;
import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.error.EnumBusinessError;
import com.matt.project.seckill.response.CommonReturnType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author matt
 * @create 2020-12-06 10:57
 */
@RestController
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class HelloController extends BaseController {

    @Autowired
    private UserDOMapper userDOMapper;

    @GetMapping("/")
    public CommonReturnType hello(@RequestParam(name = "id")Integer id) throws BusinessException {


        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (StringUtils.isEmpty(userDO.getName())) {
            throw new BusinessException(EnumBusinessError.UNKOWN_ERROR);
        }

        return CommonReturnType.create(userDO);
    }
}
