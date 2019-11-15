package com.linweili.miaosha.controller;

import com.linweili.miaosha.error.BusinessException;
import com.linweili.miaosha.error.EnumBusinessError;
import com.linweili.miaosha.mq.MqProducer;
import com.linweili.miaosha.response.CommonReturnType;
import com.linweili.miaosha.service.impl.ItemServiceImpl;
import com.linweili.miaosha.service.impl.OrderServiceImpl;
import com.linweili.miaosha.service.model.OrderModel;
import com.linweili.miaosha.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true", origins = "*")
public class OrderController extends BaseController {

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;

    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORM})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam("itemId") Integer itemID,
                                       @RequestParam("amount") Integer amount,
                                       @RequestParam(value = "promoId", required = false) Integer promoId) throws BusinessException {
//        Boolean isLogin = (Boolean) this.httpServletRequest.getSession().getAttribute("LOGIN");
//        if (isLogin == null || !isLogin.booleanValue()) {
//            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN, "用户未登录，不能下单");
//        }
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN, "用户未登录，不能下单");
        }
        //获取用户的登录信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN, "用户未登录，不能下单");
        }
//        UserModel userModel = (UserModel) this.httpServletRequest.getSession().getAttribute("LOGIN_USER");

//        OrderModel orderModel = orderService.createOrder(userModel.getId(), itemID, promoId, amount);
        //判断库存是否售罄
        if (redisTemplate.hasKey("promo_item_stock_invalid_" + itemID)) {
            throw  new BusinessException(EnumBusinessError.STOCK_NOT_ENOUGH);
        }
        //加入库存流水init状态
        String stockLogId = itemService.initStockLog(itemID, amount);
        //再完成对应的下单事务消息
        if (!mqProducer.transactionAsyncReduceStock(userModel.getId(), promoId, itemID, amount, stockLogId)) {
            throw  new BusinessException(EnumBusinessError.UNKNOWN_ERROR, "下单失败");
        }
        return CommonReturnType.create(null);
    }
}
