package com.github.dactiv.healthan.spring.security.authentication.config;

import java.util.LinkedList;
import java.util.List;

/**
 * 认证配置
 *
 * @author maurice.chen
 */
public class CaptchaVerificationProperties {

    public static final String DEFAULT_CAPTCHA_TYPE_HEADER_NAME = "X-CAPTCHA-TYPE";

    public static final String DEFAULT_CAPTCHA_TYPE_PARAM_NAME = "captchaType";

    private String captchaTypeHeaderName = DEFAULT_CAPTCHA_TYPE_HEADER_NAME;

    private String captchaTypeParamName = DEFAULT_CAPTCHA_TYPE_PARAM_NAME;
    /**
     * 一定要验证码才能使用的链接
     */
    private List<String> verifyUrls = new LinkedList<>();

    public CaptchaVerificationProperties() {
    }

    public List<String> getVerifyUrls() {
        return verifyUrls;
    }

    public void setVerifyUrls(List<String> verifyUrls) {
        this.verifyUrls = verifyUrls;
    }

    public String getCaptchaTypeHeaderName() {
        return captchaTypeHeaderName;
    }

    public void setCaptchaTypeHeaderName(String captchaTypeHeaderName) {
        this.captchaTypeHeaderName = captchaTypeHeaderName;
    }

    public String getCaptchaTypeParamName() {
        return captchaTypeParamName;
    }

    public void setCaptchaTypeParamName(String captchaTypeParamName) {
        this.captchaTypeParamName = captchaTypeParamName;
    }
}
