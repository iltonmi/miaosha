package com.linweili.miaosha.controller;

import com.linweili.miaosha.error.BusinessException;
import com.linweili.miaosha.error.EnumBusinessError;
import com.linweili.miaosha.response.CommonReturnType;
import com.linweili.miaosha.service.impl.OrderServiceImpl;
import com.linweili.miaosha.service.model.OrderModel;
import com.linweili.miaosha.service.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
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
    private HttpServletRequest httpServletRequest;

    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORM})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam("itemId") Integer itemID,
                                       @RequestParam("amount") Integer amount,
                                       @RequestParam(value = "promoId", required = false) Integer promoId) throws BusinessException {
        Boolean isLogin = (Boolean) this.httpServletRequest.getSession().getAttribute("LOGIN");
        if (isLogin == null || !isLogin.booleanValue()) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN, "用户未登录，不能下单");
        }
        UserModel userModel = (UserModel) this.httpServletRequest.getSession().getAttribute("LOGIN_USER");
        //获取用户的登录信息
        OrderModel orderModel = orderService.createOrder(userModel.getId(), itemID, promoId, amount);

        return CommonReturnType.create(null);
    }
}
