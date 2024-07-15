package com.github.dactiv.healthan.spring.security.authentication.service;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.spring.security.authentication.config.RememberMeProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.AuthenticationSuccessToken;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

public class TypeTokenBasedRememberMeServices extends TokenBasedRememberMeServices {

    public TypeTokenBasedRememberMeServices(RememberMeProperties rememberMeProperties, UserDetailsService userDetailsService) {
        super(rememberMeProperties.getKey(), userDetailsService);
        setAlwaysRemember(rememberMeProperties.isAlways());
        setParameter(rememberMeProperties.getParamName());
        setCookieName(rememberMeProperties.getCookieName());
        if (StringUtils.isNotEmpty(rememberMeProperties.getDomain())) {
            setCookieDomain(rememberMeProperties.getDomain());
        }
        setUseSecureCookie(rememberMeProperties.isUseSecureCookie());
        setTokenValiditySeconds(rememberMeProperties.getTokenValiditySeconds());
    }

    @Override
    protected String retrieveUserName(Authentication authentication) {
        if (authentication instanceof AuthenticationSuccessToken) {
            AuthenticationSuccessToken token = Casts.cast(authentication);
            return token.getName();
        } else {
            return super.retrieveUserName(authentication);
        }
    }
}
