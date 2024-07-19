package com.github.dactiv.healthan.spring.security.authentication.provider;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.service.TypeSecurityPrincipalManager;
import com.github.dactiv.healthan.spring.security.authentication.token.TypeAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Objects;

/**
 * 重写记住我供应者，将 cookie 中带有类型用户的信息解析成 {@link com.github.dactiv.healthan.spring.security.authentication.token.AuthenticationSuccessToken} 响应给 spring security，
 * 其目的是兼容所有认证流程都支持带类型的用户信息。
 *
 * @author maurice.hen
 */
public class TypeRememberMeAuthenticationProvider extends RememberMeAuthenticationProvider {

    private final TypeSecurityPrincipalManager typeSecurityPrincipalManager;

    public TypeRememberMeAuthenticationProvider(String key,
                                                TypeSecurityPrincipalManager typeSecurityPrincipalManager) {
        super(key);
        this.typeSecurityPrincipalManager = typeSecurityPrincipalManager;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!UserDetails.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
            return super.authenticate(authentication);
        }

        UserDetails userDetails = Casts.cast(authentication.getPrincipal());

        TypeAuthenticationToken token = typeSecurityPrincipalManager.createTypeAuthenticationToken(
                userDetails.getUsername(),
                authentication.getDetails(),
                userDetails.getPassword()
        );
        SecurityPrincipal principal = typeSecurityPrincipalManager.getSecurityPrincipal(token);
        if (Objects.isNull(principal)) {
            throw new UsernameNotFoundException(
                    messages.getMessage(
                            "TypeRememberMeAuthenticationProvider.badCredentials",
                            "自动登录获取用户信息失败"
                    )
            );
        }

        Collection<GrantedAuthority> authorities = typeSecurityPrincipalManager.getSecurityPrincipalGrantedAuthorities(token, principal);

        return typeSecurityPrincipalManager
                .getTypeSecurityPrincipalService(token.getType())
                .createRememberMeAuthenticationSuccessToken(principal, token, authorities);
    }
}
