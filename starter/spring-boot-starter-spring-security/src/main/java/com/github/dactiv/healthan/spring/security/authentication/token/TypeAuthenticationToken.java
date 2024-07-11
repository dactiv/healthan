package com.github.dactiv.healthan.spring.security.authentication.token;

import com.github.dactiv.healthan.commons.CacheProperties;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.LinkedHashSet;

/**
 * 带类型的认证 token，用于区分那种类型的用户使用
 *
 * @author maurice.chen
 */
public class TypeAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;

    private final Object credentials;

    private final String type;

    public TypeAuthenticationToken(Object principal, Object credentials, String type) {
        super(new LinkedHashSet<>());
        this.principal = principal;
        this.credentials = credentials;
        this.type = type;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return getType() + CacheProperties.DEFAULT_SEPARATOR + getPrincipal();
    }
}
