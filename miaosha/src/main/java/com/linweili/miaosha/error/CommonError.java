package com.linweili.miaosha.error;

public interface CommonError {
    public int getErrCode();
    public String getErrMsgs();
    public CommonError setErrMsg(String errMsg);
}
