package com.github.dactiv.healthan.spring.security.authentication.service;

import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.token.TypeAuthenticationToken;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.LinkedHashSet;
import java.util.Objects;

/**
 * 基于登录用户密码的记住我用户明细服务实现,用于完成原生记住我且根据用户类型构造 {@link UserDetails } 使用
 *
 * @author maurice.chen
 */
public class TypeTokenBasedRememberMeUserDetailsService implements UserDetailsService {

    private final TypeSecurityPrincipalManager typeSecurityPrincipalManager;

    private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    public TypeTokenBasedRememberMeUserDetailsService(TypeSecurityPrincipalManager typeSecurityPrincipalManager) {
        this.typeSecurityPrincipalManager = typeSecurityPrincipalManager;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TypeAuthenticationToken token = typeSecurityPrincipalManager.createTypeAuthenticationToken(
                username,
                null,
                null
        );

        SecurityPrincipal principal = typeSecurityPrincipalManager.getSecurityPrincipal(token);
        if (Objects.isNull(principal)) {
            throw new UsernameNotFoundException(
                    messages.getMessage(
                            "TypeTokenBasedRememberMeUserDetailsService.badCredentials",
                            "自动登录获取用户信息失败"
                    )
            );
        }

        return new User(username, principal.getCredentials().toString(), new LinkedHashSet<>());
    }

    public void setMessages(MessageSourceAccessor messages) {
        this.messages = messages;
    }
}
