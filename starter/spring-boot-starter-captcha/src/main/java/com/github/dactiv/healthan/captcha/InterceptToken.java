package com.github.dactiv.healthan.captcha;

import com.github.dactiv.healthan.commons.CacheProperties;

import java.io.Serializable;

/**
 * 拦截 token 用于在验证码生成之前，在做一次验证码校验工作使用
 *
 * @author maurice.chen
 */
public interface InterceptToken extends Serializable, Expired {

    /**
     * 获取唯一识别
     *
     * @return 唯一识别
     */
    String getId();

    /**
     * 获取绑定 token 值
     *
     * @return token
     */
    CacheProperties getToken();

    /**
     * 获取验证码类型
     *
     * @return 类型
     */
    String getType();
}
