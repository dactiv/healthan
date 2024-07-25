package com.github.dactiv.healthan.spring.web.captcha;

import com.github.dactiv.healthan.captcha.CaptchaProperties;
import com.github.dactiv.healthan.captcha.intercept.Interceptor;
import com.github.dactiv.healthan.captcha.storage.CaptchaStorageManager;
import com.github.dactiv.healthan.captcha.tianai.TianaiCaptchaService;
import com.github.dactiv.healthan.captcha.tianai.config.TianaiCaptchaProperties;
import org.springframework.util.AntPathMatcher;
import org.springframework.validation.Validator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 响应 js http 路径的 天爱验证码服务实现
 *
 * @author maurice.chen
 */
public class ControllerTianaiCaptchaService extends TianaiCaptchaService {

    public ControllerTianaiCaptchaService(CaptchaProperties captchaProperties,
                                          Validator validator,
                                          Interceptor interceptor,
                                          CaptchaStorageManager captchaStorageManager,
                                          TianaiCaptchaProperties tianaiCaptchaProperties) {
        super(captchaProperties, validator, interceptor, captchaStorageManager, tianaiCaptchaProperties);
    }

    @Override
    protected Map<String, Object> createGenerateArgs() {
        Map<String, Object> result = new LinkedHashMap<>();
        String url = getTianaiCaptchaProperties().getApiBaseUrl()
                + AntPathMatcher.DEFAULT_PATH_SEPARATOR
                + CaptchaController.CONTROLLER_NAME
                + AntPathMatcher.DEFAULT_PATH_SEPARATOR
                + TianaiCaptchaProperties.JS_CONTROLLER;

        result.put(TianaiCaptchaProperties.JS_URL_KEY, url);

        return result;
    }
}
