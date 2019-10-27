package com.linweili.miaosha.service;

import com.linweili.miaosha.error.BusinessException;
import com.linweili.miaosha.service.model.OrderModel;

public interface OrderService {

    OrderModel createOrder(Integer userId, Integer amount, Integer itemId) throws BusinessException;
}
