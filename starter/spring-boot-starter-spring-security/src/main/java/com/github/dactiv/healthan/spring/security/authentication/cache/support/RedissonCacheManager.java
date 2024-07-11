package com.github.dactiv.healthan.spring.security.authentication.cache.support;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.commons.TimeProperties;
import com.github.dactiv.healthan.commons.exception.ErrorCodeException;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.token.AuthenticationSuccessToken;
import com.github.dactiv.healthan.spring.security.authentication.token.ExpiredToken;
import com.github.dactiv.healthan.spring.security.authentication.token.RefreshToken;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.DigestUtils;

import java.util.Collection;
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
    public SecurityContext getSecurityContext(String type, Object id, CacheProperties accessTokenCache) {
        RBucket<SecurityContext> bucket = getSecurityContextBucket(type, id, accessTokenCache);
        return bucket.get();
    }

    @Override
    public void delaySecurityContext(SecurityContext context, CacheProperties accessTokenCache) {
        TimeProperties timeProperties = accessTokenCache.getExpiresTime();
        if (Objects.isNull(timeProperties)) {
            return ;
        }

        AuthenticationSuccessToken authenticationToken = Casts.cast(context);
        SecurityPrincipal securityPrincipal = Casts.cast(authenticationToken.getPrincipal());

        RBucket<SecurityContext> bucket = getSecurityContextBucket(
                authenticationToken.getPrincipalType(),
                securityPrincipal.getId(),
                accessTokenCache
        );
        bucket.expireAsync(timeProperties.toDuration());
    }

    @Override
    public void saveSecurityContext(SecurityContext context, CacheProperties accessTokenCache) {

        AuthenticationSuccessToken authenticationToken = Casts.cast(context);
        SecurityPrincipal securityPrincipal = Casts.cast(authenticationToken.getPrincipal());
        RBucket<SecurityContext> bucket = getSecurityContextBucket(
                authenticationToken.getPrincipalType(),
                securityPrincipal.getId(),
                accessTokenCache
        );

        TimeProperties timeProperties = accessTokenCache.getExpiresTime();
        if (Objects.isNull(timeProperties)) {
            bucket.setAsync(context);
        } else {
            bucket.setAsync(context, timeProperties.getValue(), timeProperties.getUnit());
        }

    }

    @Override
    public void saveSecurityContextRefreshToken(RefreshToken refreshToken, CacheProperties refreshTokenCache) {
        RBucket<ExpiredToken> refreshBucket = getTokenBucket(
                refreshToken.getToken(),
                refreshTokenCache
        );
        TimeProperties timeProperties = refreshTokenCache.getExpiresTime();
        if (Objects.isNull(timeProperties)) {
            refreshBucket.setAsync(refreshToken);
        } else {
            refreshBucket.setAsync(refreshToken, timeProperties.getValue(), timeProperties.getUnit());
        }
    }

    @Override
    public RestResult<ExpiredToken> getRefreshToken(String refreshToken, CacheProperties refreshTokenCache) {
        RBucket<ExpiredToken> refreshTokenBucket = getTokenBucket(refreshToken, refreshTokenCache);
        if (!refreshTokenBucket.isExists()) {
            return RestResult.ofException(
                    new ErrorCodeException("[" + refreshToken + "] 刷新令牌不存在", ErrorCodeException.CONTENT_NOT_EXIST)
            );
        }

        ExpiredToken refreshTokenValue = refreshTokenBucket.get();
        if (refreshTokenValue.isExpired()) {
            return RestResult.ofException(
                    new ErrorCodeException("[" + refreshToken + "] 刷新令牌已过期", ErrorCodeException.TIMEOUT_CODE)
            );
        }

        return RestResult.ofSuccess(refreshTokenValue);
    }

    public RBucket<ExpiredToken> getTokenBucket(String token, CacheProperties tokenCache) {
        String md5 = DigestUtils.md5DigestAsHex(token.getBytes());
        return redissonClient.getBucket(tokenCache.getName(md5));
    }

    public RBucket<SecurityContext> getSecurityContextBucket(String type,
                                                             Object deviceIdentified,
                                                             CacheProperties accessTokenCache) {
        String key = accessTokenCache.getName(type + CacheProperties.DEFAULT_SEPARATOR + deviceIdentified);
        return redissonClient.getBucket(key, new SerializationCodec());
    }
}
