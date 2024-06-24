package com.github.dactiv.healthan.spring.security.authentication.provider;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.UserDetailsService;
import com.github.dactiv.healthan.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.healthan.spring.security.authentication.token.SimpleAuthenticationToken;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class RequestAuthenticationProvider implements AuthenticationManager, AuthenticationProvider, MessageSourceAware, InitializingBean {

    public static final Logger LOGGER = LoggerFactory.getLogger(RequestAuthenticationProvider.class);

    /**
     * 认证缓存块名称
     */
    public static String DEFAULT_AUTHENTICATION_KEY_NAME = "spring:security:authentication:";

    /**
     * 授权缓存块名称
     */
    public static String DEFAULT_AUTHORIZATION_KEY_NAME = "spring:security:authorization:";

    /**
     * 国际化信息
     */
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    /**
     * 用户明细符合集合
     */
    private List<UserDetailsService> userDetailsServices;

    /**
     * redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * 隐藏找不到用户异常，用登陆账户或密码错误异常
     */
    private boolean hideUserNotFoundExceptions = true;

    /**
     * 当前用户认证供应者实现
     *
     * @param userDetailsServices 账户认证的用户明细服务集合
     */
    public RequestAuthenticationProvider(RedissonClient redissonClient,
                                         List<UserDetailsService> userDetailsServices) {
        this.userDetailsServices = userDetailsServices;
        this.redissonClient = redissonClient;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // 获取 token
        RequestAuthenticationToken requestAuthenticationToken = Casts.cast(authentication);

        try {
            /*if (authentication instanceof RequestAuthenticationToken) {
                RequestAuthenticationToken requestAuthenticationToken = Casts.cast(authentication);
                // 开始授权，如果失败抛出异常
                userDetails = doPrincipalAuthenticate(requestAuthenticationToken);
            } else if (authentication instanceof RememberMeAuthenticationToken) {
                RememberMeAuthenticationToken rememberMeAuthenticationToken = Casts.cast(authentication);
                userDetails = doRememberMeAuthentication(rememberMeAuthenticationToken);
            } else {
                String error = messages.getMessage(
                        "PrincipalAuthenticationProvider.authenticateNotFound",
                        "找不到适用于 " + token.getType() + " 的 UserDetailsService 实现"
                );
                throw new AuthenticationServiceException(error);
            }*/
            SecurityPrincipal userDetails = doPrincipalAuthenticate(requestAuthenticationToken);
            return createSuccessAuthentication(userDetails, requestAuthenticationToken);
        } catch (Exception e) {
            LOGGER.error("用户认证发生异常", e);
            throw new AuthenticationServiceException(e.getMessage());
        }
    }

    /*protected SecurityPrincipal doRememberMeAuthentication(RememberMeAuthenticationToken token) {

        String key = rememberMeProperties.getCache().getName(token.getName());
        RBucket<RememberMeToken> bucket = redissonClient.getBucket(key);
        RememberMeToken redisObject = bucket.get();

        if (Objects.isNull(redisObject)) {
            String error = messages.getMessage(
                    "PrincipalAuthenticationProvider.rememberMeCredentialsExpired",
                    "记住我凭证已超时"
            );
            throw new CredentialsExpiredException(error);
        }

        // 如果两个 token 不相等，报错
        if (!redisObject.equals(token.toRememberToken())) {
            String error = messages.getMessage(
                    "PrincipalAuthenticationProvider.rememberMeBadCredentials",
                    "记住我凭证错误"
            );
            throw new RememberMeAuthenticationException(error);
        }
        CacheProperties authenticationCache = null;
        if (Objects.nonNull(authenticationProperties.getAuthenticationCache())) {
            authenticationCache = CacheProperties.of(
                    authenticationProperties.getAuthenticationCache().getName(token.getName()),
                    authenticationProperties.getAuthenticationCache().getExpiresTime()
            );
        }
        SecurityPrincipal userDetails = null;
        // 如果启用认证缓存，从认证缓存里获取用户
        if (Objects.nonNull(authenticationCache)) {
            RBucket<SecurityPrincipal> userDetailsBucket = redissonClient.getBucket(authenticationCache.getName());
            userDetails = userDetailsBucket.get();
        }

        if (Objects.isNull(userDetails)) {
            Optional<UserDetailsService> optional = getUserDetailsService(token);
            if (optional.isPresent()) {
                userDetails = optional.get().getRememberMeUserDetails(token);
            }
        }

        if (Objects.isNull(userDetails)) {
            String error = messages.getMessage(
                    "PrincipalAuthenticationProvider.rememberMeUserDetailsNotFound",
                    "记住我凭证错误"
            );
            throw new RememberMeAuthenticationException(error);
        }

        return userDetails;
    }*/

    protected SecurityPrincipal doPrincipalAuthenticate(RequestAuthenticationToken token) {

        SecurityPrincipal userDetails = null;

        Optional<UserDetailsService> optional = getUserDetailsService(token);

        String message = messages.getMessage(
                "PrincipalAuthenticationProvider.userDetailsServiceNotFound",
                "找不到适用于 " + token.getType() + " 的 UserDetailsService 实现"
        );

        UserDetailsService userDetailsService = optional.orElseThrow(() -> new AuthenticationServiceException(message));
        CacheProperties authenticationCache = userDetailsService.getAuthenticationCache(token);

        // 如果启用认证缓存，从认证缓存里获取用户
        if (Objects.nonNull(authenticationCache)) {
            RBucket<SecurityPrincipal> bucket = redissonClient.getBucket(authenticationCache.getName());
            userDetails = bucket.get();
        }

        try {

            //如果在缓存中找不到用户，调用 UserDetailsService 的 getAuthenticationUserDetails 方法获取当前用户
            if (Objects.isNull(userDetails)) {
                userDetails = userDetailsService.getSecurityPrincipal(token);
            }

            checkUserDetails(userDetails);

            String presentedPassword = token.getCredentials().toString();
            if (!userDetailsService.matchesPassword(presentedPassword, token, userDetails)) {
                throw new BadCredentialsException(messages.getMessage(
                        "PrincipalAuthenticationProvider.badCredentials",
                        "用户名或密码错误"));
            }
        } catch (Exception e) {
            // 如果 hideUserNotFoundExceptions true 并且是 UsernameNotFoundException 异常，抛出 用户名密码错误异常
            if (UsernameNotFoundException.class.isAssignableFrom(e.getClass()) && hideUserNotFoundExceptions) {
                throw new BadCredentialsException(messages.getMessage(
                        "PrincipalAuthenticationProvider.badCredentials",
                        "用户名或密码错误"));
            } else if (e instanceof AuthenticationException) {
                throw e;
            } else {
                throw new InternalAuthenticationServiceException(e.getMessage(), e);
            }
        }

        // 如果启用认证缓存，存储用户信息到缓存里
        if (Objects.nonNull(authenticationCache)) {
            RBucket<SecurityPrincipal> bucket = redissonClient.getBucket(authenticationCache.getName());
            bucket.setAsync(userDetails);
            if (Objects.nonNull(authenticationCache.getExpiresTime())) {
                bucket.expireAsync(authenticationCache.getExpiresTime().toDuration());
            }
            userDetailsService.onAuthenticationCache(userDetails, bucket);
        }

        return userDetails;
    }

    /**
     * 检查用户明细是否正确，如果错误，抛出 {@link AuthenticationException} 的异常
     *
     * @param userDetails 用户明细
     */
    protected void checkUserDetails(SecurityPrincipal userDetails) {

        // 如果获取不到用户，并且 hideUserNotFoundExceptions 等于 true 时，抛次用户名或密码错误异常，否则抛出找不到用户异常
        if (Objects.isNull(userDetails)) {
            if (hideUserNotFoundExceptions) {
                throw new BadCredentialsException(messages.getMessage(
                        "PrincipalAuthenticationProvider.badCredentials",
                        "用户名或密码错误"));
            } else {
                throw new UsernameNotFoundException(messages.getMessage(
                        "PrincipalAuthenticationProvider.usernameNotFound",
                        "找不到用户信息"));
            }
        }

        // 如果用户被锁定，抛出用户被锁定异常
        if (!userDetails.isNonLocked()) {

            throw new LockedException(messages.getMessage(
                    "PrincipalAuthenticationProvider.locked",
                    "用户已经被锁定"));
        }

        // 如果用户被禁用，抛出用户被禁用异常
        if (userDetails.isDisabled()) {

            throw new DisabledException(messages.getMessage(
                    "PrincipalAuthenticationProvider.disabled",
                    "用户已被禁用"));
        }

        // 如果用户账户已过期，抛出用户账户已过期异常
        if (!userDetails.isNonExpired()) {

            throw new AccountExpiredException(messages.getMessage(
                    "PrincipalAuthenticationProvider.expired",
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
    public SimpleAuthenticationToken createSuccessAuthentication(SecurityPrincipal principal,
                                                                 RequestAuthenticationToken token) {

        // 通过 token 获取对应 type 实现的 UserDetailsService
        Optional<UserDetailsService> optional = getUserDetailsService(token);

        if (!optional.isPresent()) {
            return new SimpleAuthenticationToken(principal, token);
        }

        UserDetailsService userDetailsService = optional.get();
        Collection<? extends GrantedAuthority> grantedAuthorities;

        // 如果启用缓存获取缓存得用户授权信息，否则获取 userDetailsService.getPrincipalAuthorities 的实际数据
        CacheProperties authorizationCache = userDetailsService.getAuthorizationCache(token, principal);
        if (Objects.nonNull(authorizationCache)) {
            grantedAuthorities = redissonClient.getList(authorizationCache.getName(token.getName()));
        } else {
            // 获取用户授权信息
            grantedAuthorities = userDetailsService.getPrincipalAuthorities(principal);
        }

        return userDetailsService.createSuccessAuthentication(principal, token, grantedAuthorities);
    }

    /**
     * 获取账户认证的用户明细服务
     *
     * @param token 当前用户认证 token
     * @return 用户明细服务
     */
    public Optional<UserDetailsService> getUserDetailsService(RequestAuthenticationToken token) {

        return userDetailsServices
                .stream()
                .filter(uds -> uds.getType().contains(token.getType()))
                .findFirst();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SimpleAuthenticationToken.class.isAssignableFrom(authentication);
    }


    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(userDetailsServices, "至少要一个" + UserDetailsService.class.getName() + "接口的实现");
    }

    /**
     * 获取账户认证的用户明细服务集合
     *
     * @return 账户认证的用户明细服务集合
     */
    public List<UserDetailsService> getUserDetailsServices() {
        return userDetailsServices;
    }

    /**
     * 设置账户认证的用户明细服务集合
     *
     * @param userDetailsServices 账户认证的用户明细服务集合
     */
    public void setUserDetailsServices(List<UserDetailsService> userDetailsServices) {
        this.userDetailsServices = userDetailsServices;
    }

    /**
     * 是否隐藏找不到用户异常
     *
     * @return true 是，否则 false
     */
    public boolean isHideUserNotFoundExceptions() {
        return hideUserNotFoundExceptions;
    }

    /**
     * 设置是否隐藏找不到用户异常
     *
     * @param hideUserNotFoundExceptions true 是，否则 false
     */
    public void setHideUserNotFoundExceptions(boolean hideUserNotFoundExceptions) {
        this.hideUserNotFoundExceptions = hideUserNotFoundExceptions;
    }

}
