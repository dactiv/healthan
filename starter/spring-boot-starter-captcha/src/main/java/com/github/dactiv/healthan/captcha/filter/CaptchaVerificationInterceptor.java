package com.github.dactiv.healthan.captcha.filter;

import com.github.dactiv.healthan.commons.RestResult;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 验证码校验拦截器，用于在需要验证码校验的链接时，是否根据某些条件不需要校验验证码或一些阶段性处理使用
 *
 * @author maurice.chen
 */
public interface CaptchaVerificationInterceptor {

    /**
     * 校验验证码之前触发此方法
     *
     * @param request http servlet request
     *
     * @return true 不需要校验验证码，否则 false，默认为 false
     */
    default boolean preVerify(HttpServletRequest request) {
        return false;
    }

    /**
     * 校验验证码之后触发此方法
     *
     * @param request http servlet request
     */
    default void postVerify(HttpServletRequest request) {

    }

    /**
     * 校验异常时触发此方法
     *
     * @param request http servlet request
     * @param result 校验结果
     * @param e 异常信息
     */
    default void exceptionVerify(HttpServletRequest request, RestResult<Map<String, Object>> result, Exception e) {

    }
}
