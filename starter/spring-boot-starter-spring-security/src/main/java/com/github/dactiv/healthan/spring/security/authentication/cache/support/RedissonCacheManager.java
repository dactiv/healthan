package com.github.dactiv.healthan.spring.security.authentication.cache.support;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.TimeProperties;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.token.RefreshToken;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * redisson 形式的缓存管理实现
 *
 * @author maurice.chen
 */
public class RedissonCacheManager implements CacheManager {

    private final RedissonClient redissonClient;

    public RedissonCacheManager(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public SecurityPrincipal getSecurityPrincipal(CacheProperties authenticationCache) {
        RBucket<SecurityPrincipal> bucket = redissonClient.getBucket(authenticationCache.getName());
        return bucket.get();
    }

    @Override
    public void saveSecurityPrincipal(SecurityPrincipal principal, CacheProperties authenticationCache) {
        RBucket<SecurityPrincipal> bucket = redissonClient.getBucket(authenticationCache.getName());
        TimeProperties time = authenticationCache.getExpiresTime();
        if (Objects.nonNull(time)) {
            bucket.setAsync(principal, time.getValue(), time.getUnit());
        } else {
            bucket.setAsync(principal);
        }
    }

    @Override
    public Collection<GrantedAuthority> getGrantedAuthorities(CacheProperties authorizationCache) {
        RBucket<Collection<GrantedAuthority>> bucket = redissonClient.getBucket(
                authorizationCache.getName(),
                new SerializationCodec()
        );
        return bucket.get();
    }

    @Override
    public void saveGrantedAuthorities(Collection<GrantedAuthority> grantedAuthorities, CacheProperties authorizationCache) {
        RBucket<Collection<GrantedAuthority>> bucket = redissonClient.getBucket(
                authorizationCache.getName(),
                new SerializationCodec()
        );
        TimeProperties time = authorizationCache.getExpiresTime();
        if (Objects.nonNull(time)) {
            bucket.setAsync(grantedAuthorities, time.getValue(), time.getUnit());
        } else {
            bucket.setAsync(grantedAuthorities);
        }
    }

    @Override
    public SecurityContext getSecurityContext(Map<String, Object> plaintextUserDetail, CacheProperties accessTokenCache) {

        return null;
    }


    public RBucket<SecurityContext> getSecurityContextBucket(String type,
                                                             Object deviceIdentified,
                                                             CacheProperties accessTokenCache) {
        String key = getAccessTokenKey(type, deviceIdentified, accessTokenCache);
        return redissonClient.getBucket(key, new SerializationCodec());
    }

    public String getAccessTokenKey(String type, Object deviceIdentified, CacheProperties accessTokenCache) {
        return accessTokenCache.getName(type + CacheProperties.DEFAULT_SEPARATOR + deviceIdentified);
    }

    @Override
    public void delaySecurityContext(SecurityContext context) {

    }

    @Override
    public void saveSecurityContext(SecurityContext context, CacheProperties accessTokenCache) {

    }

    @Override
    public void saveSecurityContextRefreshToken(RefreshToken refreshToken, CacheProperties refreshTokenCache) {

    }
}
