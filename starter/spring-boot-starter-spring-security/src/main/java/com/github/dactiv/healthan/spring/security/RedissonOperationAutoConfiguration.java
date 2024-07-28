package com.github.dactiv.healthan.spring.security;

import com.github.dactiv.healthan.spring.security.authentication.RedissonOAuth2AuthorizationService;
import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.cache.support.RedissonCacheManager;
import com.github.dactiv.healthan.spring.security.authentication.config.OAuth2Properties;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson 缓存管理配置
 *
 * @author maurice.chen
 */
@Configuration
@ConditionalOnClass(RedissonAutoConfiguration.class)
@AutoConfigureBefore(OAuth2WebSecurityAutoConfiguration.class)
@ConditionalOnProperty(prefix = "healthan.authentication.spring.security", value = "enabled", matchIfMissing = true)
public class RedissonOperationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public RedissonCacheManager redissonCacheManager(RedissonClient redissonClient) {
        return new RedissonCacheManager(redissonClient);
    }

    @Bean
    @ConditionalOnMissingBean(RedissonOAuth2AuthorizationService.class)
    public RedissonOAuth2AuthorizationService redissonOAuth2AuthorizationService(RedissonClient redissonClient,
                                                                                 OAuth2Properties oAuth2Properties) {
        return new RedissonOAuth2AuthorizationService(redissonClient, oAuth2Properties);
    }
}
