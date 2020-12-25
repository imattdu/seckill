package com.matt.project.seckill.service.impl;

import com.matt.project.seckill.dao.ItemDOMapper;
import com.matt.project.seckill.dao.ItemStockDOMapper;
import com.matt.project.seckill.dao.StockLogDOMapper;
import com.matt.project.seckill.dataobject.ItemDO;
import com.matt.project.seckill.dataobject.ItemStockDO;
import com.matt.project.seckill.dataobject.StockLogDO;
import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.error.EnumBusinessError;
import com.matt.project.seckill.mq.MQProducer;
import com.matt.project.seckill.service.CacheService;
import com.matt.project.seckill.service.ItemService;
import com.matt.project.seckill.service.PromoService;
import com.matt.project.seckill.service.model.ItemModel;
import com.matt.project.seckill.service.model.PromoModel;
import com.matt.project.seckill.validator.ValidationResult;
import com.matt.project.seckill.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author matt
 * @create 2020-12-08 14:07
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private PromoService promoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQProducer mqProducer;

    @Autowired
    private CacheService cacheService;


    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    ItemDO convertItemDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel,itemDO);
        // double
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    ItemStockDO convertItemStockDOFromItemModel(ItemModel itemModel) {
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    ItemModel convertModelFromDataObject(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemModel itemModel = new ItemModel();

        BeanUtils.copyProperties(itemStockDO,itemModel);
        BeanUtils.copyProperties(itemDO,itemModel);
        // bigDecimal double
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        return itemModel;
    }

    @Transactional
    @Override
    public ItemModel createItem(ItemModel itemModel) throws BusinessException {
        //校验
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EnumBusinessError.ITEM_PARAM_ERROR,
                    result.getErrorMsg());
        }

        //转换
        ItemDO itemDO = convertItemDOFromItemModel(itemModel);
        //插入数据
        int i = itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());

        ItemStockDO itemStockDO = convertItemStockDOFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);

        return this.getItemById(itemModel.getId());
    }

    @Override
    public String initItemStockLog(Integer itemId, Integer amount, Integer status) {

        StockLogDO stockLogDO = new StockLogDO();

        stockLogDO.setStockLogId(UUID.randomUUID().toString().replace("-",""));
        stockLogDO.setItemId(itemId);
        stockLogDO.setAmount(amount);
        stockLogDO.setStatus(status);

        stockLogDOMapper.insertSelective(stockLogDO);

        return stockLogDO.getStockLogId();
    }


    @Transactional
    @Override
    public ItemModel getItemById(Integer id) {

        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);

        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
        ItemModel itemModel = convertModelFromDataObject(itemDO, itemStockDO);

        // 查询活动
        PromoModel promoModel = promoService.getPromoByItemId(id);

        if (promoModel != null && promoModel.getStatus() != 2) {
            itemModel.setPromoModel(promoModel);
        }

        return itemModel;
    }


    @Transactional
    @Override
    public List<ItemModel> listItem() {

        List<ItemDO> itemDOList = itemDOMapper.listItem();

        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = convertModelFromDataObject(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());

        return itemModelList;
    }

    @Transactional
    @Override
    public Boolean decreaseStock(Integer itemId, Integer amount) {


        ItemModel itemModel = (ItemModel) cacheService.getFromCommonCache("ITEM_"+itemId);

        if (itemModel != null) {
            itemModel.setStock(itemModel.getStock() - amount);
            cacheService.setCommonCache("ITEM_"+itemId,itemModel);

        }
        Object itemModelTemp = redisTemplate.opsForValue().get("ITEM_" + itemId);

        if (itemModelTemp != null && itemModelTemp instanceof ItemModel) {
            itemModel = (ItemModel)itemModelTemp;
            itemModel.setStock(itemModel.getStock() - amount);
            redisTemplate.opsForValue().set("ITEM_" + itemId,itemModel);

        }

        return true;
    }

    @Transactional
    @Override
    public Boolean increaseSales(Integer itemId, Integer amount) {

        ItemModel itemModel = (ItemModel) cacheService.getFromCommonCache("ITEM_"+itemId);

        if (itemModel != null) {
            itemModel.setSales(itemModel.getSales() + amount);
            cacheService.setCommonCache("ITEM_"+itemId,itemModel);

        }
        Object itemModelTemp = redisTemplate.opsForValue().get("ITEM_" + itemId);

        if (itemModelTemp != null && itemModelTemp instanceof ItemModel) {
            itemModel = (ItemModel)itemModelTemp;
            itemModel.setSales(itemModel.getSales() + amount);
            redisTemplate.opsForValue().set("ITEM_" + itemId,itemModel);

        }
        itemDOMapper.increaseSales(itemId,amount);

        return true;

    }

    @Override
    public ItemModel getItemModelInCache(Integer itemId) {

        Object itemModelObj =redisTemplate.opsForValue().get("ITEM_VALIDATE_" + itemId);
        ItemModel itemModel = null;
        if (itemModelObj == null) {
            itemModel = this.getItemById(itemId);
            redisTemplate.opsForValue().set("ITEM_VALIDATE_" + itemId,itemModel);
            redisTemplate.expire("ITEM_VALIDATE_" + itemId,1, TimeUnit.HOURS);
        } else {
            itemModel = (ItemModel) itemModelObj;
        }

        return itemModel;
    }
}
