package com.github.dactiv.healthan.spring.security.entity.support;

import com.github.dactiv.healthan.spring.security.authentication.token.ExpiredToken;
import com.github.dactiv.healthan.spring.security.entity.AccessTokenDetails;
import com.github.dactiv.healthan.spring.security.entity.AuthenticationSuccessDetails;

import java.util.Map;

/**
 * 访问令牌认证名称实现
 *
 * @author maurice.chen
 */
public class AccessTokenAuthenticationDetails extends AuthenticationSuccessDetails implements AccessTokenDetails {

    private ExpiredToken token;

    public AccessTokenAuthenticationDetails(Object requestDetails,
                                            Map<String, Object> metadata,
                                            ExpiredToken token) {
        super(requestDetails, metadata);
        this.token = token;
    }

    @Override
    public ExpiredToken getToken() {
        return token;
    }

    @Override
    public void setToken(ExpiredToken token) {
        this.token = token;
    }
}
