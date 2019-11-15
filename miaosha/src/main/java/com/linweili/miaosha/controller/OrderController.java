package com.linweili.miaosha.controller;

import com.linweili.miaosha.error.BusinessException;
import com.linweili.miaosha.error.EnumBusinessError;
import com.linweili.miaosha.mq.MqProducer;
import com.linweili.miaosha.response.CommonReturnType;
import com.linweili.miaosha.service.impl.ItemServiceImpl;
import com.linweili.miaosha.service.impl.OrderServiceImpl;
import com.linweili.miaosha.service.impl.PromoServiceImpl;
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

    @Autowired
    private PromoServiceImpl promoService;

    @RequestMapping(value = "/generatetoken", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORM})
    @ResponseBody
    public CommonReturnType generateToken(@RequestParam("itemId") Integer itemID,
                                       @RequestParam(value = "promoId", required = false) Integer promoId) throws BusinessException {
        //获取token
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN, "用户未登录");
        }
        //获取用户的登录信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN, "用户未登录");
        }
        //获取秒杀令牌
        String promoToken = promoService.generateSecondKillToken(promoId, itemID, userModel.getId());
        if (promoToken == null) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "生成令牌失败");
        }
        return CommonReturnType.create(promoToken);
    }

    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORM})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam("itemId") Integer itemID,
                                       @RequestParam("amount") Integer amount,
                                       @RequestParam(value = "promoId", required = false) Integer promoId,
                                       @RequestParam(value = "promoToken", required = false) String promoToken) throws BusinessException {
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN, "用户未登录，不能下单");
        }
        //获取用户的登录信息
        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN, "用户未登录，不能下单");
        }
        //校验秒杀令牌是否正确
        if (promoId != null) {
            String inRedisPromoToken = (String) redisTemplate.opsForValue().
                    get("promo_token_" + promoId + "_userid_" + userModel.getId() + "_itemid_" + itemID);
            if (inRedisPromoToken == null) {
                throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌校验失败");
            }
            if (!StringUtils.equals(promoToken, inRedisPromoToken)) {
                throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "秒杀令牌校验失败");
            }
        }

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
