package com.matt.project.seckill.service;

import com.matt.project.seckill.service.model.PromoModel;

/**
 * @author matt
 * @create 2020-12-13 13:27
 */
public interface PromoService {

    PromoModel getPromoByItemId(Integer itemId);
}
