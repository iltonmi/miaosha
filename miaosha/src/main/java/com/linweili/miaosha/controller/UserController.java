package com.linweili.miaosha.controller;

import com.linweili.miaosha.controller.viewobject.UserVO;
import com.linweili.miaosha.dataobject.UserDO;
import com.linweili.miaosha.error.BusinessException;
import com.linweili.miaosha.error.EnumBusinessError;
import com.linweili.miaosha.response.CommonReturnType;
import com.linweili.miaosha.service.impl.UserServiceImpl;
import com.linweili.miaosha.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", origins = "*")
public class UserController extends BaseController{

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    //用户登录接口
    @RequestMapping(value = "/login", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORM})
    @ResponseBody
    public CommonReturnType login(@RequestParam("telephone") String telephone,
                                  @RequestParam("password") String password)
            throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if (StringUtils.isEmpty(telephone) || StringUtils.isEmpty(password)){
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //用户登陆服务,校验用户登录是否合法
         UserModel userModel = userService.validateLogin(telephone, this.encodeByMD5(password));

        //上一步未抛出异常，将登陆凭证加入到用户成功的session内
        this.httpServletRequest.getSession().setAttribute("LOGIN",true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER",userModel);

        return CommonReturnType.create(null);
    }

    //用户注册接口
    @RequestMapping(value = "/register", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORM})
    @ResponseBody
    public CommonReturnType register(@RequestParam("telephone") String telephone,
                                     @RequestParam("otpCode") String otpCode,
                                     @RequestParam("name") String name,
                                     @RequestParam("gender") Integer gender,
                                     @RequestParam("age") Integer age,
                                     @RequestParam("password") String password)
            throws BusinessException ,NoSuchAlgorithmException, UnsupportedEncodingException{
        //验证手机号和对应的otpCode相符合
       String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telephone);
        if (!com.alibaba.druid.util.StringUtils.equals(otpCode, inSessionOtpCode)){
            throw new BusinessException(EnumBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不符");
        }
        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setTelephone(telephone);
        userModel.setName(name);
        userModel.setGender(Byte.valueOf(String.valueOf(gender.intValue())));
        userModel.setAge(age);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.encodeByMD5(password));
        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String encodeByMD5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定一个计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        Base64.Encoder base64Encoder = Base64.getEncoder();
        //加密字符串
        String newStr = base64Encoder.encodeToString(md5.digest(str.getBytes(StandardCharsets.UTF_8)));
        return newStr;
    }

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
        Boolean isLogin = (Boolean) this.httpServletRequest.getSession().getAttribute("LOGIN");
        if (isLogin == null || !isLogin.booleanValue()) {
            throw new BusinessException(EnumBusinessError.USER_NOT_LOGIN, "用户未登录");
        }
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
