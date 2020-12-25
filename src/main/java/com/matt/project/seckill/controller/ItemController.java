package com.matt.project.seckill.controller;

import com.matt.project.seckill.controller.viewobject.ItemVO;
import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.response.CommonReturnType;
import com.matt.project.seckill.service.CacheService;
import com.matt.project.seckill.service.ItemService;
import com.matt.project.seckill.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private ItemService itemService;

    ItemVO convertVOFromModel(ItemModel itemModel) {
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);

        // 判空处理
        if (itemModel.getPromoModel() == null) {
            return itemVO;
        }

        itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
        itemVO.setPromoId(itemModel.getPromoModel().getId());
        itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
        itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString("yyyy-MM-dd HH:mm:ss"));


        return itemVO;
    }

    /**
     * 功能：创建商品
     * @author matt
     * @date 2020/12/15
     * @param title
     * @param description
     * @param price
     * @param stock
     * @param imgUrl
     * @return com.matt.project.seckill.response.CommonReturnType
    */
    @PostMapping("/create")
    public CommonReturnType createItem(@RequestParam(name = "title")String title,
                                       @RequestParam(name = "description")String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock")Integer stock,
                                       @RequestParam(name = "imgUrl")String imgUrl) throws BusinessException {

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




    /**
     * 功能：根据商品ID查询商品
     * @author matt
     * @date 2020/12/15
     * @param id
     * @return com.matt.project.seckill.response.CommonReturnType
    */
    @GetMapping("/get")
    public CommonReturnType getItem(@RequestParam(name = "id")Integer id) {

        ItemModel itemModel = null;

        Object commonCache = cacheService.getFromCommonCache("ITEM_" + id);
        if (commonCache != null && commonCache instanceof ItemModel) {
            itemModel = (ItemModel)commonCache;
        }

        Object itemObj = redisTemplate.opsForValue().get("ITEM_" + id);
        if (itemModel == null && itemObj != null) {

            itemModel = (ItemModel)itemObj;
            cacheService.setCommonCache("ITEM_"+id,itemModel);
        } else if (itemModel == null) {
            itemModel = itemService.getItemById(id);
            cacheService.setCommonCache("ITEM_"+id,itemModel);
            redisTemplate.opsForValue().set("ITEM_" + itemModel.getId(), itemModel);
            redisTemplate.expire("ITEM_" + itemModel.getId(),10, TimeUnit.MINUTES);

        }

        ItemVO itemVO = convertVOFromModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    /**
     * 功能：列出所有商品
     * @author matt
     * @date 2020/12/15
     * @param
     * @return com.matt.project.seckill.response.CommonReturnType
    */
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
