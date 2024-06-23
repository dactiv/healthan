package com.github.dactiv.healthan.spring.security.authentication.config;

import com.github.dactiv.healthan.spring.security.authentication.AuthenticationTypeTokenResolver;
import com.github.dactiv.healthan.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.healthan.spring.security.authentication.UserDetailsService;
import com.github.dactiv.healthan.spring.security.authentication.provider.RequestAuthenticationProvider;
import org.redisson.api.RedissonClient;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

public final class RequestAuthenticationConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractAuthenticationFilterConfigurer<H, RequestAuthenticationConfigurer<H>, RequestAuthenticationFilter> {

    private final RedissonClient redissonClient;

    private final RememberMeProperties rememberMeProperties;

    private final AuthenticationProperties authenticationProperties;

    private final List<UserDetailsService> userDetailsServices;

    public RequestAuthenticationConfigurer(AuthenticationProperties authenticationProperties,
                                           List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers,
                                           List<UserDetailsService> userDetailsServices,
                                           RememberMeProperties rememberMeProperties,
                                           RedissonClient redissonClient) {
        super(
                new RequestAuthenticationFilter(authenticationProperties, authenticationTypeTokenResolvers, userDetailsServices),
                authenticationProperties.getLoginProcessingUrl()
        );
        this.redissonClient = redissonClient;
        this.rememberMeProperties = rememberMeProperties;
        this.authenticationProperties = authenticationProperties;
        this.userDetailsServices = userDetailsServices;

    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl, HttpMethod.POST.name());
    }

    @Override
    public void init(H http) throws Exception {
        super.init(http);
        http.authenticationProvider(new RequestAuthenticationProvider(redissonClient, authenticationProperties, rememberMeProperties, userDetailsServices));
    }
}
