package com.matt.project.seckill.service.impl;

import com.matt.project.seckill.dao.ItemDOMapper;
import com.matt.project.seckill.dao.PromoDOMapper;
import com.matt.project.seckill.dataobject.ItemDO;
import com.matt.project.seckill.dataobject.PromoDO;
import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.error.EnumBusinessError;
import com.matt.project.seckill.service.ItemService;
import com.matt.project.seckill.service.PromoService;
import com.matt.project.seckill.service.UserService;
import com.matt.project.seckill.service.model.ItemModel;
import com.matt.project.seckill.service.model.PromoModel;
import com.matt.project.seckill.service.model.UserModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.util.calendar.LocalGregorianCalendar;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author matt
 * @create 2020-12-13 13:28
 */
@Service
public class PromoServiceImpl implements PromoService {


    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private PromoDOMapper promoDOMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional
    @Override
    public PromoModel getPromoByItemId(Integer itemId) {

        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        PromoModel promoModel = convertModelFromDO(promoDO);
        return promoModel;
    }

    @Override
    public String generateSecondKillToken(Integer userId,Integer itemId,Integer promoId) throws BusinessException {


        // 售空判断



        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        PromoModel promoModel = convertModelFromDO(promoDO);

        ItemModel itemModel = itemService.getItemById(itemId);

        UserModel userModel = userService.getUserById(userId);



        if (userModel == null) {
            throw new BusinessException(EnumBusinessError.USER_NOT_EXISTS);
        }

        if (itemModel == null) {
            throw new BusinessException(EnumBusinessError.ITEM_NOT_EXIST);
        }

        if (promoModel == null) {
            throw new BusinessException(EnumBusinessError.PROMO_ERROR);
        }

        if (promoModel.getStatus() == 2) {
            throw new BusinessException(EnumBusinessError.PROMO_EXPIRE);
        }


        Object promoTokenCountObj = redisTemplate.opsForValue().get("PROMO_COUNT" + promoId);
        if (promoTokenCountObj == null) {
            redisTemplate.opsForValue().set("PROMO_COUNT_" + promoId,itemModel.getStock() * 2);
        }
        Long promoTokenCount = redisTemplate.opsForValue().increment("PROMO_COUNT_" + promoId, -1);
        if (promoTokenCount < 0) {
            throw new BusinessException(EnumBusinessError.UNKOWN_ERROR,"商品数量不足");
        }

        String secondKillToken = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set("USER_"+userId+"ITEM_"+itemId+"PROMO_TOKEN_"+promoId,secondKillToken);
        redisTemplate.expire("USER_"+userId+"ITEM_"+itemId+"PROMO_TOKEN_"+promoId,10, TimeUnit.MINUTES);
        return secondKillToken;
    }

    public PromoModel convertModelFromDO(PromoDO promoDO) {

        if(promoDO == null) {
            return null;
        }

        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO,promoModel);

        promoModel.setStartDate(new DateTime(promoDO.getStartDate()));
        promoModel.setEndDate(new DateTime(promoDO.getEndDate()));
        promoModel.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));

        DateTime dateTime = DateTime.now();
        // 设置商品的状态
        if (promoModel.getEndDate().isBefore(dateTime)){
            promoModel.setStatus(2);
        } else if (promoModel.getStartDate().isAfter(dateTime)){
            promoModel.setStatus(0);
        } else {
            promoModel.setStatus(1);
        }

        return promoModel;

    }
}
