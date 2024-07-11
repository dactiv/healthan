package com.github.dactiv.healthan.spring.security.authentication.token;

import org.springframework.util.MultiValueMap;


/**
 * 请求认证 token
 *
 * @author maurice
 */
public class RequestAuthenticationToken extends TypeAuthenticationToken {

    private static final long serialVersionUID = 8070060147431763553L;

    private final MultiValueMap<String, String> parameterMap;

    public RequestAuthenticationToken(MultiValueMap<String, String> parameterMap,
                                      Object principal,
                                      Object credentials,
                                      String type) {
        super(principal, credentials, type);
        this.parameterMap = parameterMap;
    }

    public MultiValueMap<String, String> getParameterMap() {
        return parameterMap;
    }
}
