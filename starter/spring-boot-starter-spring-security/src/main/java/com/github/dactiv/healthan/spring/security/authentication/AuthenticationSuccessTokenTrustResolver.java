package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.spring.security.authentication.token.AuditAuthenticationToken;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

public class AuthenticationSuccessTokenTrustResolver extends AuthenticationTrustResolverImpl {

    @Override
    public boolean isAnonymous(Authentication authentication) {
        return super.isAnonymous(authentication);
    }

    @Override
    public boolean isRememberMe(Authentication authentication) {
        if (AuditAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            AuditAuthenticationToken token = Casts.cast(authentication);
            return token.isRememberMe();
        }
        return super.isRememberMe(authentication);
    }
}
