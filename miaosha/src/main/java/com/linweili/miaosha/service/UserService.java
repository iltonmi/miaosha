package com.linweili.miaosha.service;

import com.linweili.miaosha.dataobject.UserDO;
import com.linweili.miaosha.error.BusinessException;
import com.linweili.miaosha.service.model.UserModel;

public interface UserService {
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;
    UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException;
}
