package com.github.dactiv.healthan.spring.security.authentication.token;

import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;

/**
 * 简单的用户认证 token
 *
 * @author maurice.chen
 */
public class SimpleAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 3747271533448473641L;

    /**
     * 当前用户
     */
    private final SecurityPrincipal principal;

    /**
     * 是否记住我认证
     */
    private final boolean rememberMe;

    /**
     * 最后登录时间
     */
    private final Date lastAuthenticationTime;

    /**
     * 当前用户认证 token
     *
     * @param principal   当前用户
     * @param authorities 授权信息
     */
    public SimpleAuthenticationToken(SecurityPrincipal principal,
                                     boolean rememberMe,
                                     Collection<? extends GrantedAuthority> authorities,
                                     Date lastAuthenticationTime) {
        super(authorities);
        this.principal = principal;
        this.rememberMe = rememberMe;
        this.lastAuthenticationTime = lastAuthenticationTime;
    }

    public SimpleAuthenticationToken(SecurityPrincipal principal,
                                     RequestAuthenticationToken token) {
        this(principal, token, new LinkedHashSet<>());
    }

    public SimpleAuthenticationToken(SecurityPrincipal principal,
                                     RequestAuthenticationToken token,
                                     Collection<? extends GrantedAuthority> grantedAuthorities) {
        this(principal, token.isRememberMe(), grantedAuthorities, new Date());
    }

    @Override
    public Object getCredentials() {
        return principal.getCredentials();
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return principal.getName();
    }

    /**
     * 获取是否记住我认证
     *
     * @return 是否记住我认证
     */
    public boolean isRememberMe() {
        return rememberMe;
    }

    /**
     * 获取最后登录时间
     *
     * @return 最后登录时间
     */
    public Date getLastAuthenticationTime() {
        return lastAuthenticationTime;
    }
}
