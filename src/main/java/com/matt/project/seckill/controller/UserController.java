package com.matt.project.seckill.controller;

import com.matt.project.seckill.controller.viewobject.UserVo;
import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.error.EnumBusinessError;
import com.matt.project.seckill.response.CommonReturnType;
import com.matt.project.seckill.service.UserService;
import com.matt.project.seckill.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * @author matt
 * @create 2020-12-06 13:46
 */
@RestController("user")
@RequestMapping("/user")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private  HttpServletRequest httpServletRequest;
    // 和当前线程绑定


    @GetMapping("get")
    public CommonReturnType getUserById(@RequestParam(name = "id")Integer id) throws BusinessException {

        UserModel userModel = userService.getUserById(id);

        if (userModel == null) {
            throw new BusinessException(EnumBusinessError.USER_NOT_EXISTS);
        }

        UserVo userVo = convertFromUserModel(userModel);

        return CommonReturnType.create(userVo);
    }

    public UserVo convertFromUserModel(UserModel userModel) {

        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(userModel,userVo);
        return userVo;
    }

    @PostMapping(value = "getotp",consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType getOtp(@RequestParam(name="telephone")String telephone) {

        //生成验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt = 100000 + randomInt;

        String otpCode = String.valueOf(randomInt);

        // 和手机号绑定
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute(telephone,otpCode);

        System.out.println("otpCode=" + otpCode);
        //发送验证码
        System.out.println(this.httpServletRequest.getSession().getAttribute("1"));

        return CommonReturnType.create(null);
    }

    @PostMapping(value = "register")
    public CommonReturnType register(@RequestParam(name="telphone")String telephone,
                                     @RequestParam(name="otpCode")String otpCode,
                                     @RequestParam(name="name")String name,
                                     @RequestParam(name="gender")Integer gender,
                                     @RequestParam(name="age")Integer age,
                                     @RequestParam(name="password")String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        System.out.println(this.httpServletRequest.getSession().getAttribute("1"));

        String inSessionOtpCode =(String) this.httpServletRequest.getSession().getAttribute(telephone);

        if (!StringUtils.equals(inSessionOtpCode,otpCode)) {
            throw new BusinessException(EnumBusinessError.UNKOWN_ERROR,"验证码错误");
        }
        UserModel userModel = new UserModel();
        userModel.setTelephone(telephone);
        userModel.setName(name);
        userModel.setGender(new Byte(String.valueOf(gender)));
        userModel.setAge(age);
        userModel.setEncrptPassword(EncodeByMd5(password));

        userService.register(userModel);

        return CommonReturnType.create(null);
    }

     private String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密字符串
        String newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

    @PostMapping("/login")
    public CommonReturnType login(@RequestParam(name = "telephone")String telephone,
                                  @RequestParam(name = "password")String password)
            throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        if (StringUtils.isEmpty(telephone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(EnumBusinessError.UNKOWN_ERROR,"用户名或者密码为空");
        }

        // 用户名密码判断
        UserModel userModel = userService.validateLogin(telephone,EncodeByMd5(password));
        userModel.setEncrptPassword("");
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("USERMODEL",userModel);
        return CommonReturnType.create(null);

    }


}