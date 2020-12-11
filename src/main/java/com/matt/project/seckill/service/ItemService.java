package com.matt.project.seckill.service;



import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.service.model.ItemModel;

import java.util.List;

/**
 * @author matt
 * @create 2020-12-08 13:57
 */
public interface ItemService {



    ItemModel createItem(ItemModel itemModel);


    List<ItemModel> listItem();

    /**
     * 功能描述:
     * @Param: [id]
     * @Return: com.matt.project.seckill.model.ItemModel
     * @Author: matt
     * @Date: 2020/12/8 14:06
     */
    ItemModel getItemById(Integer id);
}
