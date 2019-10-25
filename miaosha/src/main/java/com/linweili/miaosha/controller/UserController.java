package com.linweili.miaosha.controller;

import com.linweili.miaosha.controller.viewobject.UserVO;
import com.linweili.miaosha.service.impl.UserServiceImpl;
import com.linweili.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller("user")
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserServiceImpl userService;

    @RequestMapping("/get")
    @ResponseBody
    public UserVO getUser(@RequestParam("id") Integer id){
        //调用service服务获取对应ID的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);
        //将核心领域模型对象转化为可供UI使用的view object
        return convertFromModel(userModel);
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
