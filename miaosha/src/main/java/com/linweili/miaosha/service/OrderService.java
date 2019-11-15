package com.linweili.miaosha.service;

import com.linweili.miaosha.error.BusinessException;
import com.linweili.miaosha.service.model.OrderModel;

public interface OrderService {
    /**
     * 前端上传promoId
     * 两种方式下单：
     * 1.校验promoId对应活动是否已经开始
     * 2.直接判断该商品是否存在秒杀活动，存在则直接以秒杀价下单
     * 选用1号方案，具有扩展性（一个商品可能有多个活动），且比2号方案性能更好（因为2号方案对平销商品也需要判断）
     */
    OrderModel createOrder(Integer userId, Integer itemId, Integer promoId,
                           Integer amount, String stockLogId) throws BusinessException;


}
