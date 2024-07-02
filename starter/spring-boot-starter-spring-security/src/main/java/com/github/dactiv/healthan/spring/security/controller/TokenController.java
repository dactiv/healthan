package com.github.dactiv.healthan.spring.security.controller;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.commons.exception.ErrorCodeException;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.config.AccessTokenProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.AccessToken;
import com.github.dactiv.healthan.spring.security.authentication.token.ExpiredToken;
import com.github.dactiv.healthan.spring.security.authentication.token.RefreshToken;
import com.github.dactiv.healthan.spring.security.authentication.token.SimpleAuthenticationToken;
import com.github.dactiv.healthan.spring.security.entity.AccessTokenDetails;
import com.github.dactiv.healthan.spring.security.entity.AuthenticationSuccessDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.Assert;
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

    private final CacheManager cacheManager;

    private final AccessTokenProperties accessTokenProperties;

    public TokenController(CacheManager cacheManager,
                           AccessTokenProperties accessTokenProperties) {
        this.cacheManager = cacheManager;
        this.accessTokenProperties = accessTokenProperties;
    }

    @PostMapping("refreshAccessToken")
    public RestResult<Map<String, Object>> refreshAccessToken(@RequestParam String refreshToken,
                                                                   @CurrentSecurityContext SecurityContext securityContext) {

        Assert.isTrue(
                SimpleAuthenticationToken.class.isAssignableFrom(securityContext.getAuthentication().getClass()),
                "当前用户非安全用户明细"
        );

        RestResult<ExpiredToken> validResult = cacheManager.getRefreshToken(
                refreshToken,
                accessTokenProperties.getRefreshTokenCache()
        );
        if (!validResult.isSuccess()) {
            return RestResult.of(validResult.getMessage(), validResult.getStatus(), validResult.getExecuteCode());
        }

        SimpleAuthenticationToken authenticationToken = Casts.cast(securityContext.getAuthentication());

        Object details = authenticationToken.getDetails();
        if (!AccessTokenDetails.class.isAssignableFrom(details.getClass())) {
            return RestResult.ofException(
                    new ErrorCodeException("当前认证明细非访问令牌明细",ErrorCodeException.BED_REQUEST_CODE)
            );
        }

        AccessTokenDetails accessTokenDetails = Casts.cast(details);
        if (RefreshToken.class.isAssignableFrom(accessTokenDetails.getToken().getClass())) {
            return RestResult.ofException(
                    new ErrorCodeException("当前认证明细非刷新令牌明细",ErrorCodeException.BED_REQUEST_CODE)
            );
        }

        RefreshToken authenticationRefreshToken = Casts.cast(accessTokenDetails.getToken());
        RefreshToken refreshTokenValue = Casts.cast(validResult.getData());
        Assert.isTrue(
                StringUtils.equals(refreshTokenValue.getToken(), authenticationRefreshToken.getToken()),
                "刷新令牌匹配不正确"
        );

        SecurityPrincipal principal = Casts.cast(authenticationToken.getPrincipal());
        SecurityContext context = cacheManager.getSecurityContext(
                authenticationToken.getPrincipalType(),
                principal.getId(),
                accessTokenProperties.getAccessTokenCache()
        );
        if (Objects.isNull(context)) {
            return RestResult.ofException(
                    new ErrorCodeException("找不到令牌对应的用户明细", ErrorCodeException.CONTENT_NOT_EXIST)
            );
        }

        cacheManager.delaySecurityContext(context, accessTokenProperties.getAccessTokenCache());

        refreshTokenValue.setCreationTime(new Date());
        refreshTokenValue.getAccessToken().setCreationTime(new Date());
        cacheManager.saveSecurityContextRefreshToken(refreshTokenValue, accessTokenProperties.getRefreshTokenCache());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put(accessTokenProperties.getRefreshTokenParamName(), Casts.of(refreshTokenValue, AccessToken.class));
        result.put(accessTokenProperties.getAccessTokenParamName(), refreshTokenValue.getAccessToken());

        if (AuthenticationSuccessDetails.class.isAssignableFrom(accessTokenDetails.getClass())) {
            AuthenticationSuccessDetails successDetails = Casts.cast(accessTokenDetails);
            successDetails.getMetadata().putAll(result);
        }

        return RestResult.ofSuccess("延期 [" + refreshTokenValue.getAccessToken().getToken() + "] 令牌成功", result);
    }

}
