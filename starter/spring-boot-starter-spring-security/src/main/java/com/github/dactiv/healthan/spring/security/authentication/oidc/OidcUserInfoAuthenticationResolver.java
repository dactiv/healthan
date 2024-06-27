package com.github.dactiv.healthan.spring.security.authentication.oidc;

import com.github.dactiv.healthan.spring.security.authentication.token.SimpleAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

import java.util.Map;

/**
 * oidc 用户信息认证解析器实现
 *
 * @author maurice.chen
 */
public interface OidcUserInfoAuthenticationResolver {

    /**
     * 是否支持当前用户类型
     *
     * @param type 用户类型
     *
     * @return true 是，否则 false
     */
    boolean isSupport(String type);

    /**
     * 映射 oidc Claims
     * @param oAuth2Authorization 授权信息
     * @param claims 当前 claims
     * @param authenticationToken 认证信息
     *
     * @return oidc 用户信息
     */
    OidcUserInfo mappingOidcUserInfoClaims(OAuth2Authorization oAuth2Authorization,
                                           Map<String, Object> claims,
                                           SimpleAuthenticationToken authenticationToken);
}
