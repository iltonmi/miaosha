package com.linweili.miaosha.service.model;

import java.math.BigDecimal;

public class OrderModel {
    /**
     * 订单号
     */
    private String id;

    private Integer userId;

    private Integer itemId;

    /**
     * 若非空则表示是以秒杀方式下单
     */
    private Integer promoId;

    /**
     * 若promoId非空，则表示秒杀商品单价
     */
    private BigDecimal itemPrice;

    /**
     * 数量
     */
    private Integer amount;

    /**
     * 若promoId非空，则表示秒杀商品价格
     */
    private BigDecimal orderPrice;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }
}
