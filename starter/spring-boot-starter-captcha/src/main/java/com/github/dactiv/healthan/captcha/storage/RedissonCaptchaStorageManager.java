package com.github.dactiv.healthan.captcha.storage;

import com.github.dactiv.healthan.captcha.BuildToken;
import com.github.dactiv.healthan.captcha.CaptchaStorageManager;
import com.github.dactiv.healthan.captcha.InterceptToken;
import com.github.dactiv.healthan.captcha.SimpleCaptcha;
import org.redisson.api.RedissonClient;

public class RedissonCaptchaStorageManager implements CaptchaStorageManager {

    private final RedissonClient redissonClient;

    public RedissonCaptchaStorageManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
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
