package com.github.dactiv.healthan.spring.security.authentication.config;

import com.github.dactiv.healthan.spring.security.authentication.AuthenticationTypeTokenResolver;
import com.github.dactiv.healthan.spring.security.authentication.RequestAuthenticationFilter;
import com.github.dactiv.healthan.spring.security.authentication.UserDetailsService;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

public final class RequestAuthenticationConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractAuthenticationFilterConfigurer<H, RequestAuthenticationConfigurer<H>, RequestAuthenticationFilter> {


    public RequestAuthenticationConfigurer(AuthenticationProperties authenticationProperties,
                                           List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers,
                                           List<UserDetailsService> userDetailsServices) {
        super(
                new RequestAuthenticationFilter(authenticationProperties, authenticationTypeTokenResolvers, userDetailsServices),
                authenticationProperties.getLoginProcessingUrl()
        );

    }

    @Override
    protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
        return new AntPathRequestMatcher(loginProcessingUrl, HttpMethod.POST.name());
    }
}
