package com.github.dactiv.healthan.spring.security.authentication.provider;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.TypeSecurityPrincipalService;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.service.TypeSecurityPrincipalManager;
import com.github.dactiv.healthan.spring.security.authentication.token.AuditAuthenticationToken;
import com.github.dactiv.healthan.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.healthan.spring.security.entity.AuditAuthenticationDetails;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Objects;

/**
 * 请求认证供应者实现
 *
 * @author maurice.chen
 */
public class SecurityPrincipalAuthenticationProvider implements AuthenticationManager, AuthenticationProvider, MessageSourceAware {

    /**
     * 认证缓存块名称
     */
    @Deprecated
    public static String DEFAULT_AUTHENTICATION_KEY_NAME = "healthan:spring:security:authentication:";

    /**
     * 授权缓存块名称
     */
    @Deprecated
    public static String DEFAULT_AUTHORIZATION_KEY_NAME = "healthan:spring:security:authorization:";

    /**
     * 国际化信息
     */
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private final TypeSecurityPrincipalManager typeSecurityPrincipalManager;

    private final AuthenticationProperties authenticationProperties;

    public SecurityPrincipalAuthenticationProvider(AuthenticationProperties authenticationProperties,
                                                   TypeSecurityPrincipalManager typeSecurityPrincipalManager) {
        this.authenticationProperties = authenticationProperties;
        this.typeSecurityPrincipalManager = typeSecurityPrincipalManager;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 获取 token
        UsernamePasswordAuthenticationToken token = Casts.cast(authentication);
        if (!AuditAuthenticationDetails.class.isAssignableFrom(token.getDetails().getClass())) {
            return null;
        }

        AuditAuthenticationDetails details = Casts.cast(token.getDetails());
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

        try {

            SecurityPrincipal principal = typeSecurityPrincipalManager.getSecurityPrincipal(token);

            checkUserDetails(principal);

            String presentedPassword = token.getCredentials().toString();
            if (!typeSecurityPrincipalManager.matchesSecurityPrincipalPassword(presentedPassword, token, principal)) {
                throw new BadCredentialsException(messages.getMessage(
                        "SecurityPrincipalAuthenticationProvider.badCredentials",
                        "用户名或密码错误"));
            }

            typeSecurityPrincipalManager.saveSecurityPrincipalCache(token, principal);

            return principal;
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
    public AuditAuthenticationToken createSuccessAuthentication(SecurityPrincipal principal,
                                                                RequestAuthenticationToken token) {
        try {

            TypeSecurityPrincipalService typeSecurityPrincipalService = typeSecurityPrincipalManager.getTypeSecurityPrincipalService(token.getType());
            Collection<GrantedAuthority> grantedAuthorities = typeSecurityPrincipalManager.getSecurityPrincipalGrantedAuthorities(token, principal);

            return typeSecurityPrincipalService.createSuccessAuthentication(principal, token, grantedAuthorities);
        } catch (AuthenticationServiceException typeSecurityPrincipalService) {
            return new AuditAuthenticationToken(principal, token);
        }

    }


    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }


    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

}
