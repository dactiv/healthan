package com.github.dactiv.healthan.spring.security;


import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.service.feign.FeignAuthenticationTypeTokenResolver;
import com.github.dactiv.healthan.spring.security.authentication.service.feign.FeignExceptionResultResolver;
import feign.FeignException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * feign 认证配置
 *
 * @author maurice.chen
 */
@Configuration
@ConditionalOnClass(FeignException.class)
@EnableConfigurationProperties(AuthenticationProperties.class)
public class FeignSpringSecurityAutoConfiguration {

    @Bean
    public FeignExceptionResultResolver feignExceptionResultResolver() {
        return new FeignExceptionResultResolver();
    }

    @Bean
    public FeignAuthenticationTypeTokenResolver feignAuthenticationTypeTokenResolver(AuthenticationProperties properties) {
        return new FeignAuthenticationTypeTokenResolver(properties);
    }
}
