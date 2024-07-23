package com.github.dactiv.healthan.captcha;

import com.github.dactiv.healthan.captcha.storage.CaptchaStorageManager;
import com.github.dactiv.healthan.captcha.storage.support.RedissonCaptchaStorageManager;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RedissonAutoConfiguration.class)
@ConditionalOnProperty(prefix = "healthan.captcha", value = "enabled", matchIfMissing = true)
public class RedissonStorageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CaptchaStorageManager.class)
    public RedissonCaptchaStorageManager captchaStorageManager(RedissonClient redissonClient,
                                                               CaptchaProperties captchaProperties) {
        return new RedissonCaptchaStorageManager(redissonClient, captchaProperties);
    }
}
