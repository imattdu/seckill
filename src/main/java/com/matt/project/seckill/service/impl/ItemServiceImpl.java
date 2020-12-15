package com.matt.project.seckill.service.impl;

import com.matt.project.seckill.dao.ItemDOMapper;
import com.matt.project.seckill.dao.ItemStockDOMapper;
import com.matt.project.seckill.dataobject.ItemDO;
import com.matt.project.seckill.dataobject.ItemStockDO;
import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.error.EnumBusinessError;
import com.matt.project.seckill.service.ItemService;
import com.matt.project.seckill.service.PromoService;
import com.matt.project.seckill.service.model.ItemModel;
import com.matt.project.seckill.service.model.PromoModel;
import com.matt.project.seckill.validator.ValidationResult;
import com.matt.project.seckill.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
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

        int i = itemStockDOMapper.decreaseStock(itemId, amount);
        return i > 0;
    }

    @Transactional
    @Override
    public Boolean increaseSales(Integer itemId, Integer amount) {

        int i = itemDOMapper.increaseSales(itemId,amount);
        return i > 0;
    }
}
