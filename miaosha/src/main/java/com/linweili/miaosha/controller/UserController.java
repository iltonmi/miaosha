package com.linweili.miaosha.controller;

import com.linweili.miaosha.controller.viewobject.UserVO;
import com.linweili.miaosha.error.BusinessException;
import com.linweili.miaosha.error.EnumBusinessError;
import com.linweili.miaosha.response.CommonReturnType;
import com.linweili.miaosha.service.impl.UserServiceImpl;
import com.linweili.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin
public class UserController extends BaseController{

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    //用户获取otp短信接口
    @RequestMapping(value = "/getotp", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORM})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam("telephone") String telephone){
        //需要按照固定规则生成otp验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        randomInt %= 100000;
        String otpCode = String.valueOf(randomInt);

        //将otp验证码同用户对应手机号关联,使用http session的方式绑定他的手机号与otpCode
        httpServletRequest.getSession().setAttribute(telephone, otpCode);

        //将otp验证码通过短信通道发送给用户，省略
        System.out.println("telephone = " + telephone + " & otpCode = " + otpCode);

        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam("id") Integer id) throws BusinessException {
        //调用service服务获取对应ID的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);

        if (userModel == null){
            throw new BusinessException(EnumBusinessError.USER_NOT_EXIST);
        }

        //将核心领域模型对象转化为可供UI使用的view object-=n h
        UserVO userVO = convertFromModel(userModel);

        //返回通用对象
        return CommonReturnType.create(userVO);
    }

    private UserVO convertFromModel(UserModel userModel){
        if (userModel == null){
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }
}
