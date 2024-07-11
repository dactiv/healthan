package com.github.dactiv.healthan.spring.security.authentication.token;

import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Date;

/**
 * 记住我形式的认证成功 token
 *
 * @author maurice.chen
 */
public class RememberMeAuthenticationSuccessToken extends AuthenticationSuccessToken {

    public RememberMeAuthenticationSuccessToken(SecurityPrincipal principal,
                                                String principalType,
                                                Collection<? extends GrantedAuthority> authorities,
                                                Date lastAuthenticationTime) {
        super(principal, principalType, authorities, lastAuthenticationTime);
    }

    public RememberMeAuthenticationSuccessToken(SecurityPrincipal principal,
                                                TypeAuthenticationToken token) {
        super(principal, token);
    }

    public RememberMeAuthenticationSuccessToken(SecurityPrincipal principal,
                                                TypeAuthenticationToken token,
                                                Collection<? extends GrantedAuthority> grantedAuthorities) {
        super(principal, token, grantedAuthorities);
    }
}
