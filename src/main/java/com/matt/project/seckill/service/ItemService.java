package com.matt.project.seckill.service;



import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.service.model.ItemModel;

import java.util.List;

/**
 * @author matt
 * @create 2020-12-08 13:57
 */
public interface ItemService {



    /**
     * 功能：创建商品
     * @author matt
     * @date 2020/12/15
     * @param itemModel
     * @return com.matt.project.seckill.service.model.ItemModel
    */
    ItemModel createItem(ItemModel itemModel) throws BusinessException;


    /**
     * 功能：查询所有商品
     * @author matt
     * @date 2020/12/15
     * @param
     * @return java.util.List<com.matt.project.seckill.service.model.ItemModel>
    */
    List<ItemModel> listItem();

    /**
     * 功能描述: 根据商品ID查询商品
     * @Param: [id]
     * @Return: com.matt.project.seckill.model.ItemModel
     * @Author: matt
     * @Date: 2020/12/8 14:06
     */
    ItemModel getItemById(Integer id);

    /**
     * 功能：减库存
     * @author matt
     * @date 2020/12/16
     * @param itemId
     * @param amount
     * @return java.lang.Boolean
    */
    Boolean decreaseStock(Integer itemId,Integer amount);

    /**
     * 功能：增加销量
     * @author matt
     * @date 2020/12/16
     * @param itemId
     * @param amount
     * @return java.lang.Boolean
    */
    Boolean increaseSales(Integer itemId, Integer amount);
}
