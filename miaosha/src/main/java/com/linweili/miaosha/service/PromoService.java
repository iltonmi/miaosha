package com.linweili.miaosha.service;

import com.linweili.miaosha.error.BusinessException;
import com.linweili.miaosha.service.model.PromoModel;

public interface PromoService {
    PromoModel getPromoByItemId(Integer itemId);

    //活动发布
    void publishPromo(Integer promoId);

    String generateSecondKillToken(Integer promoId, Integer itemId, Integer userId);
}
