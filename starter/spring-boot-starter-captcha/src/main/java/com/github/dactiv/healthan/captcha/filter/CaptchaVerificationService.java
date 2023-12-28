package com.github.dactiv.healthan.captcha.filter;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 验证码校验服务
 *
 * @author maurice.chen
 */
public interface CaptchaVerificationService {

    /**
     * 获取验证码类型
     *
     * @return 验证码类型
     */
    List<String> getType();

    /**
     * 验证数据
     *
     * @param request http servlet request
     */
    void verify(HttpServletRequest request);

}
