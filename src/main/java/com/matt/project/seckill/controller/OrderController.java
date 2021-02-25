package com.matt.project.seckill.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.RateLimiter;
import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.error.EnumBusinessError;
import com.matt.project.seckill.mq.MQProducer;
import com.matt.project.seckill.response.CommonReturnType;
import com.matt.project.seckill.service.ItemService;
import com.matt.project.seckill.service.OrderService;
import com.matt.project.seckill.service.PromoService;
import com.matt.project.seckill.service.model.UserModel;
import com.matt.project.seckill.util.CodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author matt
 * @create 2020-12-13 10:41
 */
@RestController
@RequestMapping("/order")
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class OrderController extends BaseController {


    @Autowired
    private HttpServletRequest request;
    @Autowired
    private ItemService itemService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PromoService promoService;

    private ExecutorService executorService;


    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQProducer mqProducer;

    private RateLimiter orderCreateRateLimiter;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(20);
        orderCreateRateLimiter = RateLimiter.create(300);
    }


    /**
     * 功能：生成验证
     * @author matt
     * @date 2021/2/23
     * @param request
     * @param response
     * @return com.matt.project.seckill.response.CommonReturnType
    */
    @GetMapping("/generateVerifyCode")
    public CommonReturnType generateVerifyCode(HttpServletRequest request,
                                               HttpServletResponse response) throws BusinessException, IOException {
        String userToken = request.getParameter("token");

        if (StringUtils.isEmpty(userToken)) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(userToken);
        if (userModel == null) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }

        // OutputStream out = new FileOutputStream("/"+System.currentTimeMillis()+".jpg");
        Map<String, Object> map = CodeUtil.generateCodeAndPic();
        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", response.getOutputStream());
        System.out.println("验证码的值为：" + map.get("code"));
        redisTemplate.opsForValue().set("VERIFY_CODE_" + userModel.getId(), map.get("code"));
        redisTemplate.expire("VERIFY_CODE_" + userModel.getId(), 5,TimeUnit.MINUTES);
        return CommonReturnType.create(null);
    }

    @PostMapping("/generatePromoToken")
    public CommonReturnType generatePromoToken(@RequestParam(name = "itemId") Integer itemId,
                                               @RequestParam(name = "promoId") Integer promoId,
                                               @RequestParam(name = "verifyCode") String verifyCode,
                                               HttpServletRequest request) throws BusinessException {

        if (!orderCreateRateLimiter.tryAcquire()) {
            throw  new BusinessException(EnumBusinessError.UNKOWN_ERROR);
        }
        String userToken = request.getParameter("token");

        if (StringUtils.isEmpty(userToken)) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(userToken);
        // System.out.println(userModel.toString());
        if (userModel == null) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }

        Object verifyCodeObjInRedis = redisTemplate.opsForValue().get("VERIFY_CODE_" + userModel.getId());
        if (verifyCode == null || verifyCodeObjInRedis == null || !StringUtils.equals(verifyCode,(String)verifyCodeObjInRedis)){
            throw new BusinessException(EnumBusinessError.UNKOWN_ERROR,"验证码不匹配");
        }

        String promoToken = promoService.generateSecondKillToken(userModel.getId(), itemId, promoId);

        return CommonReturnType.create(promoToken);

    }


    /**
     * 功能：创建订单
     *
     * @param itemId
     * @param amount
     * @param promoId
     * @return com.matt.project.seckill.response.CommonReturnType
     * @author matt
     * @date 2020/12/16
     */
    @PostMapping("/createorder")
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount,
                                        @RequestParam(name = "promoId") Integer promoId,
                                        HttpServletRequest request,
                                        @RequestParam(name = "promoToken", required = false) String promoToken)
            throws BusinessException, UnsupportedEncodingException, MQClientException {


        String userToken = request.getParameter("token");
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(userToken);
        if (userModel == null) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN);
        }

        // 获取token
        Object promoTokenObj = redisTemplate.opsForValue().get("USER_" + userModel.getId() + "ITEM_" + itemId + "PROMO_TOKEN_" + promoId);
        if (promoTokenObj == null || !StringUtils.equals(promoToken, (String) promoTokenObj)) {
            throw new BusinessException(EnumBusinessError.ORODER_GENERATE_PROMO_TOKEN);
        }

        Future<Object> submit = executorService.submit((() -> {
            // 初始化库存流水状态
            String stockLogId = itemService.initStockLog(itemId, amount);

            // 下单
            Boolean createOrder = mqProducer.transactionalAsyncSendCreateOrder(userModel.getId(),
                    itemId, amount, promoId, stockLogId);

            if (!createOrder) {
                throw new BusinessException(EnumBusinessError.ITEM_STOCK_NOT_ENOUGH);
            }

            return null;
        }));
        try {
            submit.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return CommonReturnType.create(null);
    }

}
