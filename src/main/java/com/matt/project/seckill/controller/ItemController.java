package com.matt.project.seckill.controller;

import com.matt.project.seckill.controller.viewobject.ItemVO;
import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.response.CommonReturnType;
import com.matt.project.seckill.service.ItemService;
import com.matt.project.seckill.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author matt
 * @create 2020-12-08 14:48
 */
@RestController
@RequestMapping("/item")
@CrossOrigin(origins = {"*"},allowCredentials = "true")
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemService;

    /**
     * 功能描述: 创建商品
     * @Param: []
     * @Return: com.matt.project.seckill.response.CommonReturnType
     * @Author: matt
     * @Date: 2020/12/8 14:54
     */
    @PostMapping("/create")
    public CommonReturnType createItem(@RequestParam(name = "title")String title,
                                       @RequestParam(name = "description")String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock")Integer stock,
                                       @RequestParam(name = "imgUrl")String imgUrl) {

        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);


        itemService.createItem(itemModel);
        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO,"success");
    }


    ItemVO convertVOFromModel(ItemModel itemModel) {
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);
        return itemVO;
    }

    @GetMapping("/get")
    public CommonReturnType getItem(@RequestParam(name = "id")Integer id) {

        ItemModel itemModel = itemService.getItemById(id);
        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    @GetMapping("/list")
    public CommonReturnType listItem(){

        List<ItemModel> itemModelList = itemService.listItem();

        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = convertVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());

        return CommonReturnType.create(itemVOList);
    }

}
