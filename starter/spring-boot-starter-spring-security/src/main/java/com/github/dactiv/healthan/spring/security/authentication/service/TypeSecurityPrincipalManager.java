package com.github.dactiv.healthan.spring.security.authentication.service;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.TypeSecurityPrincipalService;
import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.healthan.spring.security.authentication.token.TypeAuthenticationToken;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 带类型的安全用户管理
 *
 * @author maurice.chen
 */
public class TypeSecurityPrincipalManager implements InitializingBean {

    /**
     * 认证缓存块名称
     */
    public static String DEFAULT_AUTHENTICATION_KEY_NAME = "healthan:spring:security:authentication:";

    /**
     * 授权缓存块名称
     */
    public static String DEFAULT_AUTHORIZATION_KEY_NAME = "healthan:spring:security:authorization:";

    /**
     * 带类型的安全用户服务集合
     */
    private final List<TypeSecurityPrincipalService> typeSecurityPrincipalServices;

    /**
     * 缓存管理
     */
    private final CacheManager cacheManager;

    /**
     * spring security 国际化访问者
     */
    private final MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    public TypeSecurityPrincipalManager(List<TypeSecurityPrincipalService> typeSecurityPrincipalServices,
                                        CacheManager cacheManager) {
        this.typeSecurityPrincipalServices = typeSecurityPrincipalServices;
        this.cacheManager = cacheManager;
    }

    /**
     * 获取带类型的安全用户服务
     *
     * @param type 类型
     *
     * @return 带类型的安全用户服务
     */
    public TypeSecurityPrincipalService getTypeSecurityPrincipalService(String type) {
        Optional<TypeSecurityPrincipalService> optional = typeSecurityPrincipalServices
                .stream()
                .filter(uds -> uds.getType().contains(type))
                .findFirst();

        String message = messages.getMessage(
                "TypeSecurityPrincipalManager.userDetailsServiceNotFound",
                "找不到适用于 " + type + " 的 TypeSecurityPrincipalService 实现"
        );

        return optional.orElseThrow(() -> new AuthenticationServiceException(message));
    }

    /**
     * 根据带类型的认证 token 获取安全用户信息
     *
     * @param token 带类型的认证 token
     *
     * @return 安全用户信息
     */
    public SecurityPrincipal getSecurityPrincipal(TypeAuthenticationToken token) {
        TypeSecurityPrincipalService typeSecurityPrincipalService = getTypeSecurityPrincipalService(token.getType());
        CacheProperties authenticationCache = typeSecurityPrincipalService.getAuthenticationCache(token);

        SecurityPrincipal principal = null;
        // 如果启用认证缓存，从认证缓存里获取用户
        if (Objects.nonNull(authenticationCache)) {
            principal = cacheManager.getSecurityPrincipal(authenticationCache);
        }

        if (Objects.isNull(principal)) {
            principal = typeSecurityPrincipalService.getSecurityPrincipal(token);
        }
        if (Objects.isNull(principal)) {
            String message = messages.getMessage(
                    "TypeSecurityPrincipalManager.usernameNotFound",
                    "在类型为 " + token.getType() + " 的 TypeSecurityPrincipalService 实现里，找不到名为 [" + token.getPrincipal().toString() + "] 登录账户"
            );
            throw new UsernameNotFoundException(message);
        }

        return principal;
    }

    /**
     * 获取安全用户授权信息
     *
     * @param token 带类型的认证 token
     * @param principal 安全用户信息
     *
     * @return 授权信息
     */
    public Collection<GrantedAuthority> getSecurityPrincipalGrantedAuthorities(TypeAuthenticationToken token,
                                                                               SecurityPrincipal principal) {
        TypeSecurityPrincipalService typeSecurityPrincipalService = getTypeSecurityPrincipalService(token.getType());
        CacheProperties authorizationCache = typeSecurityPrincipalService.getAuthorizationCache(token, principal);

        // 如果启用授权缓存，从授权缓存里获取授权信息
        if (Objects.nonNull(authorizationCache)) {
            Collection<GrantedAuthority> grantedAuthorities = cacheManager.getGrantedAuthorities(authorizationCache);
            if (CollectionUtils.isEmpty(grantedAuthorities)) {
                grantedAuthorities = typeSecurityPrincipalService.getPrincipalGrantedAuthorities(token, principal);
            }

            if (CollectionUtils.isNotEmpty(grantedAuthorities)) {
                CacheProperties cacheProperties = typeSecurityPrincipalService.getAuthorizationCache(token, principal);
                cacheManager.saveGrantedAuthorities(grantedAuthorities, cacheProperties);
            }

            return grantedAuthorities;
        } else {
            return typeSecurityPrincipalService.getPrincipalGrantedAuthorities(token, principal);
        }
    }

    public void saveSecurityPrincipalCache(TypeAuthenticationToken token, SecurityPrincipal principal) {
        TypeSecurityPrincipalService typeSecurityPrincipalService = getTypeSecurityPrincipalService(token.getType());
        CacheProperties authenticationCache = typeSecurityPrincipalService.getAuthenticationCache(token);
        // 如果启用认证缓存，存储用户信息到缓存里
        if (Objects.isNull(authenticationCache)) {
            return ;
        }
        cacheManager.saveSecurityPrincipal(principal, authenticationCache);
    }

    /**
     * 校验安全用户密码
     *
     * @param presentedPassword 提交的密码
     * @param token 请求认证 token
     * @param principal 当前安全用户信息
     *
     * @return ture 验证通过，否则 false
     */
    public boolean matchesSecurityPrincipalPassword(String presentedPassword,
                                                    RequestAuthenticationToken token,
                                                    SecurityPrincipal principal) {
        return getTypeSecurityPrincipalService(token.getType()).matchesPassword(presentedPassword, token, principal);
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(typeSecurityPrincipalServices, "至少要一个" + TypeSecurityPrincipalService.class.getName() + "接口的实现");
    }
}
