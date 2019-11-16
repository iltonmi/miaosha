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
import com.linweili.miaosha.util.CodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

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

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(20);
    }

    @RequestMapping(value = "/generateverifycode", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public void generateVerifyCode(HttpServletResponse httpServletResponse)
            throws BusinessException, IOException {
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
        Map<String, Object> map = CodeUtil.generateCodeAndPic();
        redisTemplate.opsForValue().set("verify_code_" + userModel.getId(), map.get("code"));
        redisTemplate.expire("verify_code_" + userModel.getId(), 10, TimeUnit.MINUTES);
        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", httpServletResponse.getOutputStream());
    }

    @RequestMapping(value = "/generatetoken", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORM})
    @ResponseBody
    public CommonReturnType generateToken(@RequestParam("itemId") Integer itemID,
                                          @RequestParam("promoId") Integer promoId,
                                          @RequestParam("verifyCode") String verifyCode) throws BusinessException {
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
        String inRedisVerifyCode = (String) redisTemplate.opsForValue().get("verify_code_" + userModel.getId());
        if (StringUtils.isEmpty(inRedisVerifyCode)) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "请求非法");
        }
        if (!inRedisVerifyCode.equalsIgnoreCase(verifyCode)) {
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "请求非法，验证码错误");
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

        //同步调用线程池的submit方法
        //拥塞窗口为20的等待队列，用来队列化泄洪
        Future<Object> future = executorService.submit(() -> {
            //加入库存流水init状态
            String stockLogId = itemService.initStockLog(itemID, amount);
            //再完成对应的下单事务消息
            if (!mqProducer.transactionAsyncReduceStock(userModel.getId(), promoId, itemID, amount, stockLogId)) {
                throw  new BusinessException(EnumBusinessError.UNKNOWN_ERROR, "下单失败");
            }
            return null;
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            throw new BusinessException(EnumBusinessError.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            throw new BusinessException(EnumBusinessError.UNKNOWN_ERROR);
        }


        return CommonReturnType.create(null);
    }
}
