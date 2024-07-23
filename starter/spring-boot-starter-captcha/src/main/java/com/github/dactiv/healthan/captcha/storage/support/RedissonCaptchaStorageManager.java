package com.github.dactiv.healthan.captcha.storage.support;

import com.github.dactiv.healthan.captcha.CaptchaProperties;
import com.github.dactiv.healthan.captcha.SimpleCaptcha;
import com.github.dactiv.healthan.captcha.storage.CaptchaStorageManager;
import com.github.dactiv.healthan.captcha.token.BuildToken;
import com.github.dactiv.healthan.captcha.token.InterceptToken;
import org.redisson.api.RedissonClient;

/**
 * redisson 验证码存储管理实现
 *
 * @author maurice.chen
 */
public class RedissonCaptchaStorageManager implements CaptchaStorageManager {

    private final RedissonClient redissonClient;

    private final CaptchaProperties captchaProperties;

    public RedissonCaptchaStorageManager(RedissonClient redissonClient,
                                         CaptchaProperties captchaProperties) {
        this.redissonClient = redissonClient;
        this.captchaProperties = captchaProperties;
    }

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
