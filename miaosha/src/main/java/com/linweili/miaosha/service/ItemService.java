package com.linweili.miaosha.service;

import com.linweili.miaosha.error.BusinessException;
import com.linweili.miaosha.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    List<ItemModel> listItem();

    ItemModel getItemById(Integer id);

    ItemModel getItemByIdInCache(Integer id);

    boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException;

    boolean increaseStock(Integer itemId, Integer amount) throws BusinessException;

    boolean asyncDecreaseStock(Integer itemId, Integer amount);

    //初始化库存流水状态
    String initStockLog(Integer itemId, Integer amount);

    void increaseSales(Integer itemId, Integer amount) throws BusinessException;
}
