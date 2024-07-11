package com.github.dactiv.healthan.spring.security.authentication.provider;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.FormLoginAuthenticationDetails;
import com.github.dactiv.healthan.spring.security.authentication.TypeSecurityPrincipalService;
import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.AuthenticationSuccessToken;
import com.github.dactiv.healthan.spring.security.authentication.token.RequestAuthenticationToken;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 请求认证供应者实现
 *
 * @author maurice.chen
 */
public class SecurityPrincipalAuthenticationProvider implements AuthenticationManager, AuthenticationProvider, MessageSourceAware, InitializingBean {

    /**
     * 认证缓存块名称
     */
    public static String DEFAULT_AUTHENTICATION_KEY_NAME = "healthan:spring:security:authentication:";

    /**
     * 授权缓存块名称
     */
    public static String DEFAULT_AUTHORIZATION_KEY_NAME = "healthan:spring:security:authorization:";

    /**
     * 国际化信息
     */
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    /**
     * 用户明细符合集合
     */
    private final List<TypeSecurityPrincipalService> typeSecurityPrincipalServices;

    /**
     * 缓存管理
     */
    private final CacheManager cacheManager;

    private final AuthenticationProperties authenticationProperties;


    public SecurityPrincipalAuthenticationProvider(CacheManager cacheManager,
                                                   AuthenticationProperties authenticationProperties,
                                                   List<TypeSecurityPrincipalService> typeSecurityPrincipalServices) {
        this.authenticationProperties = authenticationProperties;
        this.typeSecurityPrincipalServices = typeSecurityPrincipalServices;
        this.cacheManager = cacheManager;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 获取 token
        UsernamePasswordAuthenticationToken token = Casts.cast(authentication);
        if (!FormLoginAuthenticationDetails.class.isAssignableFrom(token.getDetails().getClass())) {
            return null;
        }

        FormLoginAuthenticationDetails details = Casts.cast(token.getDetails());
        RequestAuthenticationToken authenticationToken = new RequestAuthenticationToken(details, token);

        try {
            SecurityPrincipal userDetails = doPrincipalAuthenticate(authenticationToken);
            return createSuccessAuthentication(userDetails, authenticationToken);
        } catch (Exception e) {
            if (e instanceof AuthenticationException) {
                throw e;
            }
            throw new AuthenticationServiceException(e.getMessage());
        }
    }

    protected SecurityPrincipal doPrincipalAuthenticate(RequestAuthenticationToken token) {

        SecurityPrincipal principal = null;

        Optional<TypeSecurityPrincipalService> optional = getUserDetailsService(token);

        String message = messages.getMessage(
                "SecurityPrincipalAuthenticationProvider.userDetailsServiceNotFound",
                "找不到适用于 " + token.getType() + " 的 UserDetailsService 实现"
        );

        TypeSecurityPrincipalService typeSecurityPrincipalService = optional.orElseThrow(() -> new AuthenticationServiceException(message));
        CacheProperties authenticationCache = typeSecurityPrincipalService.getAuthenticationCache(token);

        // 如果启用认证缓存，从认证缓存里获取用户
        if (Objects.nonNull(authenticationCache)) {
            principal = cacheManager.getSecurityPrincipal(authenticationCache);
        }

        try {

            //如果在缓存中找不到用户，调用 UserDetailsService 的 getAuthenticationUserDetails 方法获取当前用户
            if (Objects.isNull(principal)) {
                principal = typeSecurityPrincipalService.getSecurityPrincipal(token);
            }

            checkUserDetails(principal);

            String presentedPassword = token.getCredentials().toString();
            if (!typeSecurityPrincipalService.matchesPassword(presentedPassword, token, principal)) {
                throw new BadCredentialsException(messages.getMessage(
                        "SecurityPrincipalAuthenticationProvider.badCredentials",
                        "用户名或密码错误"));
            }
        } catch (Exception e) {
            // 如果 hideUserNotFoundExceptions true 并且是 UsernameNotFoundException 异常，抛出 用户名密码错误异常
            if (UsernameNotFoundException.class.isAssignableFrom(e.getClass()) && authenticationProperties.isHideUserNotFoundExceptions()) {
                throw new BadCredentialsException(messages.getMessage(
                        "SecurityPrincipalAuthenticationProvider.badCredentials",
                        "用户名或密码错误"));
            } else if (e instanceof AuthenticationException) {
                throw e;
            } else {
                throw new InternalAuthenticationServiceException(e.getMessage(), e);
            }
        }

        // 如果启用认证缓存，存储用户信息到缓存里
        if (Objects.nonNull(authenticationCache)) {
            cacheManager.saveSecurityPrincipal(principal, authenticationCache);
        }

        return principal;
    }

    /**
     * 检查用户明细是否正确，如果错误，抛出 {@link AuthenticationException} 的异常
     *
     * @param userDetails 用户明细
     */
    protected void checkUserDetails(SecurityPrincipal userDetails) {

        // 如果获取不到用户，并且 hideUserNotFoundExceptions 等于 true 时，抛次用户名或密码错误异常，否则抛出找不到用户异常
        if (Objects.isNull(userDetails)) {
            if (authenticationProperties.isHideUserNotFoundExceptions()) {
                throw new BadCredentialsException(messages.getMessage(
                        "SecurityPrincipalAuthenticationProvider.badCredentials",
                        "用户名或密码错误"));
            } else {
                throw new UsernameNotFoundException(messages.getMessage(
                        "SecurityPrincipalAuthenticationProvider.usernameNotFound",
                        "找不到用户信息"));
            }
        }

        // 如果用户被锁定，抛出用户被锁定异常
        if (!userDetails.isNonLocked()) {

            throw new LockedException(messages.getMessage(
                    "SecurityPrincipalAuthenticationProvider.locked",
                    "用户已经被锁定"));
        }

        // 如果用户被禁用，抛出用户被禁用异常
        if (userDetails.isDisabled()) {

            throw new DisabledException(messages.getMessage(
                    "SecurityPrincipalAuthenticationProvider.disabled",
                    "用户已被禁用"));
        }

        // 如果用户账户已过期，抛出用户账户已过期异常
        if (!userDetails.isNonExpired()) {

            throw new AccountExpiredException(messages.getMessage(
                    "SecurityPrincipalAuthenticationProvider.expired",
                    "用户账户已过期"));
        }
    }

    /**
     * 创建认证信息
     *
     * @param principal 当前用户
     * @param token     当前认真 token
     * @return spring security 认证信息
     */
    public AuthenticationSuccessToken createSuccessAuthentication(SecurityPrincipal principal,
                                                                  RequestAuthenticationToken token) {

        // 通过 token 获取对应 type 实现的 UserDetailsService
        Optional<TypeSecurityPrincipalService> optional = getUserDetailsService(token);

        if (!optional.isPresent()) {
            return new AuthenticationSuccessToken(principal, token);
        }

        TypeSecurityPrincipalService typeSecurityPrincipalService = optional.get();
        Collection<GrantedAuthority> grantedAuthorities;

        // 如果启用缓存获取缓存得用户授权信息，否则获取 userDetailsService.getPrincipalAuthorities 的实际数据
        CacheProperties authorizationCache = typeSecurityPrincipalService.getAuthorizationCache(token, principal);
        if (Objects.nonNull(authorizationCache)) {
            grantedAuthorities = getCacheGrantedAuthorities(principal, token, authorizationCache, typeSecurityPrincipalService);
        } else {
            // 获取用户授权信息
            grantedAuthorities = typeSecurityPrincipalService.getPrincipalAuthorities(token, principal);
        }

        return typeSecurityPrincipalService.createSuccessAuthentication(principal, token, grantedAuthorities);
    }

    private Collection<GrantedAuthority> getCacheGrantedAuthorities(SecurityPrincipal principal,
                                                                    RequestAuthenticationToken token,
                                                                    CacheProperties authorizationCache,
                                                                    TypeSecurityPrincipalService typeSecurityPrincipalService) {
        Collection<GrantedAuthority> grantedAuthorities;
        Collection<GrantedAuthority> cacheGrantedAuthorities = cacheManager.getGrantedAuthorities(authorizationCache);
        if (CollectionUtils.isEmpty(cacheGrantedAuthorities)) {
            grantedAuthorities = typeSecurityPrincipalService.getPrincipalAuthorities(token, principal);
        } else {
            grantedAuthorities = cacheGrantedAuthorities;
        }

        if (CollectionUtils.isNotEmpty(grantedAuthorities)) {
            cacheManager.saveGrantedAuthorities(grantedAuthorities, authorizationCache);
        }

        return grantedAuthorities;
    }

    /**
     * 获取账户认证的用户明细服务
     *
     * @param token 当前用户认证 token
     * @return 用户明细服务
     */
    public Optional<TypeSecurityPrincipalService> getUserDetailsService(RequestAuthenticationToken token) {
        return typeSecurityPrincipalServices
                .stream()
                .filter(uds -> uds.getType().contains(token.getType()))
                .findFirst();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }


    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(typeSecurityPrincipalServices, "至少要一个" + TypeSecurityPrincipalService.class.getName() + "接口的实现");
    }

}
