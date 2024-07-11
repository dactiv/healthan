package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.TypeAuthenticationToken;

/**
 * 抽象的用户明细服务实现，实现创建认证 token，等部分公用功能
 *
 * @author maurice.chen
 *
 */
public abstract class AbstractTypeSecurityPrincipalService implements TypeSecurityPrincipalService {

    private AuthenticationProperties authenticationProperties;

    public AbstractTypeSecurityPrincipalService() {
    }

    public void setAuthenticationProperties(AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

    /**
     * 获取配置信息
     *
     * @return 配置信息
     */
    public AuthenticationProperties getAuthenticationProperties() {
        return authenticationProperties;
    }

    @Override
    public CacheProperties getAuthorizationCache(TypeAuthenticationToken token, SecurityPrincipal principal) {
        String suffix = token.getType() + CacheProperties.DEFAULT_SEPARATOR + principal.getName();
        return CacheProperties.of(
                authenticationProperties.getAuthorizationCache().getName(suffix),
                authenticationProperties.getAuthorizationCache().getExpiresTime()
        ) ;
    }

    @Override
    public CacheProperties getAuthenticationCache(TypeAuthenticationToken token) {
        return CacheProperties.of(
                authenticationProperties.getAuthenticationCache().getName(token.getName()),
                authenticationProperties.getAuthenticationCache().getExpiresTime()
        ) ;
    }
}
