package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.spring.security.authentication.token.AuthenticationSuccessToken;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

public class SimpleAuthenticationTrustResolver extends AuthenticationTrustResolverImpl {

    @Override
    public boolean isAnonymous(Authentication authentication) {
        return super.isAnonymous(authentication);
    }

    @Override
    public boolean isRememberMe(Authentication authentication) {
        if (AuthenticationSuccessToken.class.isAssignableFrom(authentication.getClass())) {
            AuthenticationSuccessToken token = Casts.cast(authentication);
            return token.isRememberMe();
        }
        return super.isRememberMe(authentication);
    }
}
