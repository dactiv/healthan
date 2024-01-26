package com.github.dactiv.healthan.spring.security;

import com.github.dactiv.healthan.captcha.CaptchaConfiguration;
import com.github.dactiv.healthan.captcha.CaptchaProperties;
import com.github.dactiv.healthan.captcha.DelegateCaptchaService;
import com.github.dactiv.healthan.captcha.intercept.Interceptor;
import com.github.dactiv.healthan.spring.security.captcha.CaptchaExtendController;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * 验证码扩展配置
 *
 * @author maurice.chen
 */
@Configuration
@AutoConfigureAfter(CaptchaConfiguration.class)
@EnableConfigurationProperties(CaptchaProperties.class)
@ConditionalOnProperty(prefix = "healthan.captcha", value = "enabled", matchIfMissing = true)
public class CaptchaExtendAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "healthan.captcha.controller", value = "enabled", matchIfMissing = true)
    public CaptchaExtendController captchaExtendController(@Lazy Interceptor interceptor,
                                                           CaptchaProperties captchaProperties,
                                                           DelegateCaptchaService delegateCaptchaService) {
        return new CaptchaExtendController(interceptor, captchaProperties, delegateCaptchaService);
    }
}
