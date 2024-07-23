package com.github.dactiv.healthan.spring.security;

import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.cache.support.RedissonCacheManager;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RedissonAutoConfiguration.class)
@ConditionalOnProperty(prefix = "healthan.authentication.spring.security", value = "enabled", matchIfMissing = true)
public class RedissonCacheManagerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public RedissonCacheManager redissonCacheManager(RedissonClient redissonClient) {
        return new RedissonCacheManager(redissonClient);
    }
}
