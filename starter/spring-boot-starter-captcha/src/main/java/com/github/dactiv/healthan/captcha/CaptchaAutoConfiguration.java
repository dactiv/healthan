package com.github.dactiv.healthan.captcha;


import com.github.dactiv.healthan.captcha.controller.CaptchaController;
import com.github.dactiv.healthan.captcha.filter.CaptchaVerificationFilter;
import com.github.dactiv.healthan.captcha.filter.CaptchaVerificationInterceptor;
import com.github.dactiv.healthan.captcha.filter.CaptchaVerificationService;
import com.github.dactiv.healthan.captcha.filter.support.TianaiCaptchaVerificationService;
import com.github.dactiv.healthan.captcha.intercept.Interceptor;
import com.github.dactiv.healthan.captcha.intercept.support.DelegateCaptchaInterceptor;
import com.github.dactiv.healthan.captcha.storage.CaptchaStorageManager;
import com.github.dactiv.healthan.captcha.storage.support.InMemoryCaptchaStorageManager;
import com.github.dactiv.healthan.captcha.tianai.TianaiCaptchaService;
import com.github.dactiv.healthan.captcha.tianai.config.TianaiCaptchaProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.Validator;

import java.util.stream.Collectors;

/**
 * 验证码自动配置
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties({CaptchaProperties.class, TianaiCaptchaProperties.class})
@ConditionalOnProperty(prefix = "healthan.captcha", value = "enabled", matchIfMissing = true)
public class CaptchaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Interceptor.class)
    public Interceptor interceptor(DelegateCaptchaService delegateCaptchaService) {
        return new DelegateCaptchaInterceptor(delegateCaptchaService);
    }

    @Bean
    @ConditionalOnMissingBean(DelegateCaptchaService.class)
    public DelegateCaptchaService delegateCaptchaService(ObjectProvider<CaptchaService> captchaServices) {
        return  new DelegateCaptchaService(captchaServices.stream().collect(Collectors.toList()));
    }

    @Bean
    @ConditionalOnProperty(prefix = "healthan.captcha.controller", value = "enabled", matchIfMissing = true)
    public CaptchaController captchaController(@Lazy Interceptor interceptor,
                                               CaptchaProperties captchaProperties,
                                               TianaiCaptchaService captchaService,
                                               ResourceLoader resourceLoader,
                                               DelegateCaptchaService delegateCaptchaService) {
        return new CaptchaController(delegateCaptchaService, interceptor, resourceLoader, captchaProperties, captchaService);
    }

    @Bean
    @ConditionalOnMissingBean(TianaiCaptchaService.class)
    public TianaiCaptchaService tianaiCaptchaService(CaptchaProperties captchaProperties,
                                                     @Qualifier("mvcValidator") Validator validator,
                                                     @Lazy Interceptor interceptor,
                                                     CaptchaStorageManager captchaStorageManager,
                                                     TianaiCaptchaProperties tianaiCaptchaProperties) {
        return new TianaiCaptchaService(captchaProperties, validator, interceptor, captchaStorageManager, tianaiCaptchaProperties);
    }

    @Bean
    @ConditionalOnMissingBean(CaptchaStorageManager.class)
    public CaptchaStorageManager captchaStorageManager() {
        return new InMemoryCaptchaStorageManager();
    }

    @Bean
    @ConditionalOnMissingBean(TianaiCaptchaVerificationService.class)
    public TianaiCaptchaVerificationService tianaiCaptchaVerificationService(TianaiCaptchaService tianaiCaptchaService) {
        return new TianaiCaptchaVerificationService(tianaiCaptchaService);
    }

    @Bean
    public FilterRegistrationBean<CaptchaVerificationFilter> captchaVerificationFilter(CaptchaProperties captchaProperties,
                                                                                       ObjectProvider<CaptchaVerificationService> captchaVerificationServices,
                                                                                       ObjectProvider<CaptchaVerificationInterceptor> captchaVerificationInterceptors) {
        CaptchaVerificationFilter captchaVerificationFilter = new CaptchaVerificationFilter(
                captchaProperties,
                captchaVerificationServices.stream().collect(Collectors.toList()),
                captchaVerificationInterceptors.stream().collect(Collectors.toList())
        );

        FilterRegistrationBean<CaptchaVerificationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(captchaVerificationFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registrationBean;
    }

}
