package com.linweili.miaosha.service.impl;

import com.linweili.miaosha.dao.OrderDOMapper;
import com.linweili.miaosha.dao.SequenceDOMapper;
import com.linweili.miaosha.dao.StockLogDOMapper;
import com.linweili.miaosha.dataobject.OrderDO;
import com.linweili.miaosha.dataobject.SequenceDO;
import com.linweili.miaosha.dataobject.StockLogDO;
import com.linweili.miaosha.error.BusinessException;
import com.linweili.miaosha.error.EnumBusinessError;
import com.linweili.miaosha.service.OrderService;
import com.linweili.miaosha.service.model.ItemModel;
import com.linweili.miaosha.service.model.OrderModel;
import com.linweili.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sound.midi.Sequence;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Override
    @Transactional
    public OrderModel createOrder(Integer userId, Integer itemId, Integer promoId,
                                  Integer amount, String stockLogId) throws BusinessException {
        //校验下单状态：下单商品是否存在，用户是否合法，购买数量是否正确
//        ItemModel itemModel = itemService.getItemById(itemId);
        ItemModel itemModel = itemService.getItemByIdInCache(itemId);
        if (itemModel == null) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "商品信息不存在");
        }
//        UserModel userModel = userService.getUserByIdInCache(userId);
//        if (userModel == null) {
//            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "用户信息不存在");
//        }
//        if (amount <= 0 || amount > 99) {
//            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "数量信息不正确");
//        }

        //校验活动信息
//        if (promoId != null) {
//            //1校验对应活动是否存在这个使用商品
//            if (promoId.intValue() != itemModel.getPromoModel().getId()) {
//                throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
//            } else if (itemModel.getPromoModel().getStatus() != 2) {
//                //2校验活动是否进行中
//                throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "活动时间已过期");
//            }
//        }

        //落单减库存(区分于支付减库存)
        boolean decreaseStockSuccess = itemService.decreaseStock(itemId, amount);
        if (!decreaseStockSuccess) {
            throw new BusinessException(EnumBusinessError.STOCK_NOT_ENOUGH);
        }
        //订单入户
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        if (promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoItemPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setPromoId(promoId);
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(BigDecimal.valueOf(amount)));

        //生成交易流水号
        orderModel.setId(generateOrderN0());
        OrderDO orderDO = this.convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //加上商品的销量
        itemService.increaseSales(itemId, amount);

        //设置库存流水状态为成功
        StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
        if (stockLogDO == null) {
            throw new BusinessException(EnumBusinessError.UNKNOWN_ERROR);
        }
        stockLogDO.setStatus(2);
        stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);

        //返回前端
        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected String generateOrderN0() {
        //订单号16位
        StringBuilder stringBuilder = new StringBuilder();
        //前8位时间信息,年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(nowDate);
        //中间6位为自增序列
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6 - sequenceStr.length(); ++i) {
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);
        //最后2步为分库分表位,暂时写死
        stringBuilder.append("00");
        return stringBuilder.toString();
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}
