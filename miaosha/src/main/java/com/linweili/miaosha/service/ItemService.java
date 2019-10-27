package com.linweili.miaosha.service;

import com.linweili.miaosha.error.BusinessException;
import com.linweili.miaosha.service.model.ItemModel;

import java.util.List;

public interface ItemService {
    ItemModel createItem(ItemModel itemModel) throws BusinessException;

    List<ItemModel> listItem();

    ItemModel getItemById(Integer id);

    boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException;

    void increaseSales(Integer itemId, Integer amount) throws BusinessException;
}
