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

@Controller("user")
@RequestMapping("/user")
public class UserController extends BaseController{

    @Autowired
    UserServiceImpl userService;

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
