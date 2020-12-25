package com.matt.project.seckill.service.impl;

import com.matt.project.seckill.dao.*;
import com.matt.project.seckill.dataobject.*;
import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.error.EnumBusinessError;
import com.matt.project.seckill.mq.MQProducer;
import com.matt.project.seckill.service.ItemService;
import com.matt.project.seckill.service.OrderService;
import com.matt.project.seckill.service.PromoService;
import com.matt.project.seckill.service.model.ItemModel;
import com.matt.project.seckill.service.model.OrderModel;
import com.matt.project.seckill.service.model.PromoModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author matt
 * @create 2020-12-11 17:15
 */
@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private ItemDOMapper itemDOMapper;
    @Autowired
    private ItemStockDOMapper itemStockDOMapper;
    @Autowired
    private OrderDOMapper orderDOMapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    private PromoService promoService;
    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQProducer producer;

    public OrderDO convertDOFromModel(OrderModel orderModel) {

        if (orderModel == null) {
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        orderDO.setAmount(orderModel.getAmount().toString());
        return orderDO;
    }

    @Transactional
    @Override
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount,
                                  Integer promoId,String stockLogId) throws BusinessException, UnsupportedEncodingException {

        // 校验状态

        UserDO userDO = userDOMapper.selectByPrimaryKey(userId);
        if (userDO == null) {
            throw new BusinessException(EnumBusinessError.USER_NOT_EXISTS);
        }

        //ItemModel itemModel = itemService.getItemById(itemId);
        ItemModel itemModel = itemService.getItemById(itemId);

        if (itemModel == null) {
            throw new BusinessException(EnumBusinessError.ITEM_NOT_EXIST);
        }

       // ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemId);
        if (amount <= 0 || amount > 99 || amount > itemModel.getStock()) {
            throw new BusinessException(EnumBusinessError.ITEM_AMOUNT_ERROR);
        }

        // 查询对应的活动
        PromoModel promoModel = promoService.getPromoByItemId(itemId);
        if (promoId != null) {
            if (itemModel.getPromoModel().getId() != promoId) {
                throw new BusinessException(EnumBusinessError.PROMO_ERROR);
            } else if (itemModel.getPromoModel().getStatus() == 2) {
                throw new BusinessException(EnumBusinessError.PROMO_EXPIRE);
            }
        }

        // 落单减库存

        Boolean decreaseStock = itemService.decreaseStock(itemId, amount);

        if (!decreaseStock) {
            throw new BusinessException(EnumBusinessError.ITEM_STOCK_ERROR);
        }

        // 订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);

        if(promoId != null){
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        }else{
            orderModel.setItemPrice(itemModel.getPrice());
        }

        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(new BigDecimal(amount)));
        // 获取订单号
        orderModel.setId(generateOrderNo());

        OrderDO orderDO = convertDOFromModel(orderModel);
        int i = orderDOMapper.insertSelective(orderDO);

        // 更新销量
        itemService.increaseSales(itemId,amount);
        // redisTemplate.delete("ITEM_VALIDATE_" + itemId);
        // redisTemplate.delete("USER_VALIDATE_" + userId);
        StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
        stockLogDO.setStatus(2);
        stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);

        return orderModel;
    }




    @Transactional(propagation = Propagation.REQUIRES_NEW)
     String generateOrderNo() {

        StringBuilder stringBuilder = new StringBuilder();

        LocalDateTime localDateTime = LocalDateTime.now();
        String format = localDateTime.format(DateTimeFormatter.BASIC_ISO_DATE);
        stringBuilder.append(format);


        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        Integer currentValue = sequenceDO.getCurrentValue();

        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDOMapper.updateByPramKeySelective(sequenceDO);

        String currentValueStr = String.valueOf(currentValue);
        for (int i = 0; i < 6 - currentValueStr.length(); i++) {
            stringBuilder.append('0');
        }
        stringBuilder.append(currentValueStr);

        stringBuilder.append("00");
        return stringBuilder.toString();
    }
}
