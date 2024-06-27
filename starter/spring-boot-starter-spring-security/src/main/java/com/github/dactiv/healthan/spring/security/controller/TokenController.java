package com.github.dactiv.healthan.spring.security.controller;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.commons.TimeProperties;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.AccessTokenContextRepository;
import com.github.dactiv.healthan.spring.security.authentication.config.AccessTokenProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.RefreshToken;
import com.github.dactiv.healthan.spring.security.authentication.token.SimpleAuthenticationToken;
import com.github.dactiv.healthan.spring.security.entity.AccessTokenDetails;
import com.github.dactiv.healthan.spring.security.entity.AuthenticationSuccessDetails;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * token 终端
 *
 * @author maurice.chen
 */
@RestController
public class TokenController {

    private final AccessTokenContextRepository accessTokenContextRepository;

    private final AccessTokenProperties accessTokenProperties;

    private final RedissonClient redissonClient;

    public TokenController(AccessTokenContextRepository accessTokenContextRepository,
                           RedissonClient redissonClient,
                           AccessTokenProperties accessTokenProperties) {
        this.accessTokenContextRepository = accessTokenContextRepository;
        this.redissonClient = redissonClient;
        this.accessTokenProperties = accessTokenProperties;
    }

    @PostMapping("refreshAccessToken")
    public RestResult<Map<String, Object>> refreshAccessToken(@RequestParam String refreshToken,
                                                              @CurrentSecurityContext SecurityContext securityContext) {

        Assert.isTrue(
                SimpleAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass()),
                "当前用户非安全用户明细"
        );

        String refreshMd5 = DigestUtils.md5DigestAsHex(refreshToken.getBytes());
        String refresh = accessTokenProperties.getRefreshTokenCache().getName(refreshMd5);

        RBucket<RefreshToken> refreshTokenBucket = redissonClient.getBucket(refresh);
        Assert.isTrue(refreshTokenBucket.isExists(), "[" + refreshToken + "] 刷新令牌已过期");

        RefreshToken refreshTokenValue = refreshTokenBucket.get();
        Assert.isTrue(!refreshTokenValue.isExpired(), "[" + refreshToken + "] 刷新令牌已过期");

        SimpleAuthenticationToken authenticationToken = Casts.cast(securityContext.getAuthentication());

        Object details = authenticationToken.getDetails();
        Assert.isTrue(
                AccessTokenDetails.class.isAssignableFrom(details.getClass()),
                "当前认证明细非访问令牌明细"
        );

        AccessTokenDetails accessTokenDetails = Casts.cast(details);
        Assert.isTrue(
                RefreshToken.class.isAssignableFrom(accessTokenDetails.getToken().getClass()),
                "当前认证明细非刷新令牌明细"
        );
        RefreshToken authenticationRefreshToken = Casts.cast(accessTokenDetails.getToken());
        Assert.isTrue(
                StringUtils.equals(refreshTokenValue.getToken(), authenticationRefreshToken.getToken()),
                "刷新令牌匹配不正确"
        );

        SecurityPrincipal principal = Casts.cast(authenticationToken.getPrincipal());
        RBucket<SecurityContext> securityContextBucket = accessTokenContextRepository.getSecurityContextBucket(
                authenticationToken.getPrincipalType(),
                principal.getId()
        );
        Assert.isTrue(securityContextBucket.isExists(), "找不到令牌对应的用户明细");

        securityContextBucket.expireAsync(refreshTokenValue.getAccessToken().getExpiresTime().toDuration());

        refreshTokenValue.setCreationTime(new Date());
        refreshTokenValue.getAccessToken().setCreationTime(new Date());

        TimeProperties timeProperties = refreshTokenValue.getExpiresTime();
        if (Objects.nonNull(timeProperties)) {
            refreshTokenBucket.setAsync(refreshTokenValue, timeProperties.getValue(), timeProperties.getUnit());
        } else {
            refreshTokenBucket.setAsync(refreshTokenValue);
        }

        Map<String, Object> result = new LinkedHashMap<>();

        result.put(accessTokenProperties.getRefreshTokenParamName(), refreshTokenValue.getToken());
        result.put(accessTokenProperties.getAccessTokenParamName(), refreshTokenValue.getAccessToken().getToken());

        if (AuthenticationSuccessDetails.class.isAssignableFrom(accessTokenDetails.getClass())) {
            AuthenticationSuccessDetails successDetails = Casts.cast(accessTokenDetails);
            successDetails.getMetadata().putAll(result);
        }

        return RestResult.ofSuccess("延期 [" + refreshTokenValue.getAccessToken().getToken() + "] 令牌成功", result);
    }

}
