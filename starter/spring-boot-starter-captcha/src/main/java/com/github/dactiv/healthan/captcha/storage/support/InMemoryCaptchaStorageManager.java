package com.github.dactiv.healthan.captcha.storage.support;

import com.github.dactiv.healthan.captcha.SimpleCaptcha;
import com.github.dactiv.healthan.captcha.storage.CaptchaStorageManager;
import com.github.dactiv.healthan.captcha.token.BuildToken;
import com.github.dactiv.healthan.captcha.token.InterceptToken;

import java.util.HashMap;
import java.util.Map;

/**
 * 内存形式的验证码存储管理实现
 *
 * @author maurice.chen
 */
public class InMemoryCaptchaStorageManager implements CaptchaStorageManager {
    private static final int DEFAULT_CAPACITY = 1000;

    private static final Map<String, BuildToken> BUILD_TOKEN_CACHE = new HashMap<>(DEFAULT_CAPACITY);

    private static final Map<String, InterceptToken> INTERCEPT_TOKEN_CACHE = new HashMap<>(DEFAULT_CAPACITY);

    private static final Map<String, SimpleCaptcha> SIMPLE_CAPTCHA_CACHE = new HashMap<>(DEFAULT_CAPACITY);

    @Override
    public void saveBuildToken(BuildToken token) {
        BUILD_TOKEN_CACHE.put(token.getToken().getName(), token);
    }

    @Override
    public void saveInterceptToken(InterceptToken interceptToken) {
        INTERCEPT_TOKEN_CACHE.put(interceptToken.getToken().getName(), interceptToken);
    }

    @Override
    public BuildToken getBuildToken(String token) {
        return BUILD_TOKEN_CACHE.get(token);
    }

    @Override
    public InterceptToken getInterceptToken(String token) {
        return INTERCEPT_TOKEN_CACHE.get(token);
    }

    @Override
    public void deleteBuildToken(BuildToken buildToken) {
        BUILD_TOKEN_CACHE.remove(buildToken.getToken().getName());
    }

    @Override
    public void saveCaptcha(SimpleCaptcha captcha, InterceptToken interceptToken) {
        SIMPLE_CAPTCHA_CACHE.put(interceptToken.getToken().getName(), captcha);
    }

    @Override
    public SimpleCaptcha getCaptcha(InterceptToken interceptToken) {
        return SIMPLE_CAPTCHA_CACHE.get(interceptToken.getToken().getName());
    }

    @Override
    public void deleteCaptcha(InterceptToken interceptToken) {
        SIMPLE_CAPTCHA_CACHE.remove(interceptToken.getToken().getName());
    }
}
