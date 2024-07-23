package com.github.dactiv.healthan.captcha.storage;

import com.github.dactiv.healthan.captcha.BuildToken;
import com.github.dactiv.healthan.captcha.CaptchaStorageManager;
import com.github.dactiv.healthan.captcha.InterceptToken;
import com.github.dactiv.healthan.captcha.SimpleCaptcha;

/**
 * 内存形式的验证码存储管理实现
 *
 * @author maurice.chen
 */
public class InMemoryCaptchaStorageManager implements CaptchaStorageManager {

    @Override
    public void saveBuildToken(BuildToken token) {

    }

    @Override
    public void saveInterceptToken(InterceptToken interceptToken) {

    }

    @Override
    public BuildToken getBuildToken(String token) {
        return null;
    }

    @Override
    public InterceptToken getInterceptToken(String token) {
        return null;
    }

    @Override
    public void deleteBuildToken(BuildToken buildToken) {

    }

    @Override
    public void saveCaptcha(SimpleCaptcha captcha) {

    }

    @Override
    public SimpleCaptcha getCaptcha(InterceptToken interceptToken) {
        return null;
    }

    @Override
    public void deleteCaptcha(InterceptToken token) {

    }
}
