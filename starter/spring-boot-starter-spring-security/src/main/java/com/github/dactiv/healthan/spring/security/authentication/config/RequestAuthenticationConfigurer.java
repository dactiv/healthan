package com.github.dactiv.healthan.spring.security.authentication.config;

import com.github.dactiv.healthan.spring.security.authentication.AuthenticationTypeTokenResolver;
import com.github.dactiv.healthan.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.healthan.spring.security.authentication.TypeSecurityPrincipalService;
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

    private final List<TypeSecurityPrincipalService> typeSecurityPrincipalServices;

    public RequestAuthenticationConfigurer(AuthenticationProperties authenticationProperties,
                                           List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers,
                                           List<TypeSecurityPrincipalService> typeSecurityPrincipalServices,
                                           RedissonClient redissonClient) {
        super(
                new RequestAuthenticationFilter(authenticationProperties, authenticationTypeTokenResolvers, typeSecurityPrincipalServices),
                authenticationProperties.getLoginProcessingUrl()
        );
        this.redissonClient = redissonClient;
        this.typeSecurityPrincipalServices = typeSecurityPrincipalServices;

    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl, HttpMethod.POST.name());
    }

    @Override
    public void init(H http) throws Exception {
        super.init(http);
        http.authenticationProvider(new RequestAuthenticationProvider(redissonClient, typeSecurityPrincipalServices));
    }
}
