package com.github.dactiv.healthan.spring.security.authentication.provider;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.TypeSecurityPrincipalService;
import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.RememberMeAuthenticationSuccessToken;
import com.github.dactiv.healthan.spring.security.authentication.token.TypeAuthenticationToken;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 重写记住我供应者，将 cookie 中带有类型用户的信息解析成{@link RememberMeAuthenticationSuccessToken} 响应给 spring security，
 * 其目的是兼容所有认证流程都支持带类型的用户信息。
 *
 * @author maurice.hen
 */
public class TypeRememberMeAuthenticationProvider extends RememberMeAuthenticationProvider {

    private final List<TypeSecurityPrincipalService> typeSecurityPrincipalServices;

    private final AuthenticationProperties authenticationProperties;

    private final CacheManager cacheManager;

    public TypeRememberMeAuthenticationProvider(String key,
                                                AuthenticationProperties authenticationProperties,
                                                CacheManager cacheManager,
                                                List<TypeSecurityPrincipalService> typeSecurityPrincipalServices) {
        super(key);
        this.authenticationProperties = authenticationProperties;
        this.cacheManager = cacheManager;
        this.typeSecurityPrincipalServices = typeSecurityPrincipalServices;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!UserDetails.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
            return super.authenticate(authentication);
        }

        UserDetails userDetails = Casts.cast(authentication.getPrincipal());
        String[] split = StringUtils.splitByWholeSeparator(userDetails.getUsername(), CacheProperties.DEFAULT_SEPARATOR);

        if (ArrayUtils.isEmpty(split) || split.length < 2) {
            String message = messages.getMessage(
                    "RememberMeAuthenticationProvider.formatError",
                    "记住我登录数据出错，格式应该为:<用户类型>:[用户id]:<用户登录信息>， 当前格式为:" + userDetails.getUsername()
            );
            throw new InvalidCookieException(message);
        }

        String message = messages.getMessage(
                "PrincipalAuthenticationProvider.userDetailsServiceNotFound",
                "找不到适用于 " + split[0] + " 的 UserDetailsService 实现"
        );

        TypeAuthenticationToken token = new TypeAuthenticationToken(
                split[0],
                null,
                split.length == 3 ? split[2] : split[1]
        );
        token.setDetails(authentication.getDetails());

        TypeSecurityPrincipalService typeSecurityPrincipalService = typeSecurityPrincipalServices
                .stream()
                .filter(t -> t.getType().contains(split[0])).findFirst()
                .orElseThrow(() -> new InternalAuthenticationServiceException(message));

        String authenticationCacheName = authenticationProperties.getAuthenticationCache().getName(token.getName());
        SecurityPrincipal principal = cacheManager.getSecurityPrincipal(CacheProperties.of(authenticationCacheName));
        if (Objects.isNull(principal)) {
            principal = typeSecurityPrincipalService.getSecurityPrincipal(token);
        }
        if (Objects.isNull(principal)) {
            throw new UsernameNotFoundException(
                    messages.getMessage(
                            "RememberMeAuthenticationProvider.badCredentials",
                            "自动登录获取用户信息失败"
                    )
            );
        }

        String authorizationCacheName = authenticationProperties.getAuthorizationCache().getName(userDetails.getUsername());
        Collection<GrantedAuthority> authorities = cacheManager.getGrantedAuthorities(CacheProperties.of(authorizationCacheName));

        return typeSecurityPrincipalService.createRememberMeAuthenticationSuccessToken(principal, token, authorities);
    }
}
