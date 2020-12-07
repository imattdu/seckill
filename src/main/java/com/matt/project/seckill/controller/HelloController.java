package com.matt.project.seckill.controller;

import com.matt.project.seckill.dao.UserDOMapper;
import com.matt.project.seckill.dataobject.UserDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author matt
 * @create 2020-12-06 10:57
 */
@RestController
public class HelloController {

    @Autowired
    private UserDOMapper userDOMapper;

    @GetMapping("/")
    public UserDO hello(){

        UserDO userDO = userDOMapper.selectByPrimaryKey(1);
        return userDO;
    }
}
