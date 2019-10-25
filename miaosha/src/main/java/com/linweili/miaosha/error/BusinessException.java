package com.linweili.miaosha.error;

/**
 * 包装器业务异常类实现
 */
public class BusinessException extends Exception implements CommonError{

    private CommonError commonError;

    /**
     * 直接接收EnumBusinessError的传参用于构造业务异常
     * @param commonError
     */
    public BusinessException(CommonError commonError){
        super();
        this.commonError = commonError;
    }

    /**
     * 接收自定义errMsg的方式构造业务异常
     * @param commonError
     */
    public BusinessException(CommonError commonError, String errMsg){
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);
    }


    @Override
    public int getErrCode() {
        return this.commonError.getErrCode();
    }

    @Override
    public String getErrMsgs() {
        return this.commonError.getErrMsgs();
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.commonError.setErrMsg(errMsg);
        return this;
    }
}
