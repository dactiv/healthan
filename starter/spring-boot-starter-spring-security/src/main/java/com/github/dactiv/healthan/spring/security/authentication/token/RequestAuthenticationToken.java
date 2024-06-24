package com.github.dactiv.healthan.spring.security.authentication.token;

import com.github.dactiv.healthan.commons.CacheProperties;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashSet;


/**
 * 请求认证 token
 *
 * @author maurice
 */
public class RequestAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 8070060147431763553L;

    private final MultiValueMap<String, String> parameterMap;

    private final Object principal;

    private final Object credentials;

    private final String type;

    private final boolean rememberMe;

    public RequestAuthenticationToken(MultiValueMap<String, String> parameterMap,
                                      Object principal,
                                      Object credentials,
                                      String type,
                                      boolean rememberMe) {
        super(new LinkedHashSet<>());
        this.parameterMap = parameterMap;
        this.principal = principal;
        this.credentials = credentials;
        this.rememberMe = rememberMe;
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

    public MultiValueMap<String, String> getParameterMap() {
        return parameterMap;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return getType() + CacheProperties.DEFAULT_SEPARATOR + getPrincipal();
    }
}
