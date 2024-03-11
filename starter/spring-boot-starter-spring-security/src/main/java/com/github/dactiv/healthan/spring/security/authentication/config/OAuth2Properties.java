package com.github.dactiv.healthan.spring.security.authentication.config;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.TimeProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * oauth2 配置信息
 *
 * @author maurice.chen
 */
public class OAuth2Properties {

    /**
     * 默认的授权页面 url
     */
    public static final String DEFAULT_AUTHORIZE_PAGE_URI = "/oauth2/authorize";

    /**
     * 默认的注销令牌 url
     */
    public static final String DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI = "/oauth2/revoke";

    /**
     * 默认的令牌 url
     */
    public static final String DEFAULT_TOKEN_ENDPOINT_URI = "/oauth2/token";

    /**
     * 默认的用户信息 url
     */
    public static final String DEFAULT_OIDC_USER_INFO_ENDPOINT_URI = "/userinfo";

    /**
     * 默认确认授权敏感信息 url
     */
    public static final String DEFAULT_CONSENT_PAGE_URI = "/oauth2/consent";

    /**
     * 确认授权页面 url
     */
    private String consentPageUri = DEFAULT_CONSENT_PAGE_URI;

    /**
     * 用户信息 url
     */
    private String oidcUserInfoEndpointUri = DEFAULT_OIDC_USER_INFO_ENDPOINT_URI;

    /**
     * 令牌 url
     */
    private String tokenEndpointUri = DEFAULT_TOKEN_ENDPOINT_URI;

    /**
     * 注销令牌 url
     */
    private String tokenRevocationEndpointUri = DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI;

    /**
     * 授权页面 url
     */
    private String authorizePageUri = DEFAULT_AUTHORIZE_PAGE_URI;

    /**
     * 授权缓存配置
     */
    private CacheProperties authorizationCache = CacheProperties.of(
            "healthan:uni-portal:authentication:oauth2:authorization:",
            TimeProperties.of(5, TimeUnit.MINUTES)
    );

    /**
     * jwt 公共密钥
     */
    private String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjpYgoT9kO3NOZvfi8KTI3mcX1gExeZg8u5/KoydHCuvZW2avT8f8p+16WVPJcS5V0vIL1gttZR8sVbE4TeQJLjGPbYZrPOIFVLN5ySfp4/u+kKjWi4drEindW12To42h+8ZUDQ7L7WBhDX8SCnI55k3A92CLJ5xL0s3MKCEYm6x2B6Plo29siyCbvxrw3lDN36S0YtHhTOpOkjspbhzfy7tTUQTq73UuJf62WQMJ4ev1IyEdD+6OIDHnAWp3c1WZ5cgrERHWqhLmcXpVq0QJYDWFF9UKTZEvml++HEiSuulCe2ZNs8/11a6Bkf1EQJ4HVVN4i1qHNr3C/Jap+xDtkQIDAQAB";

    /**
     * jwt 私有密钥
     */
    private String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCOliChP2Q7c05m9+LwpMjeZxfWATF5mDy7n8qjJ0cK69lbZq9Px/yn7XpZU8lxLlXS8gvWC21lHyxVsThN5AkuMY9thms84gVUs3nJJ+nj+76QqNaLh2sSKd1bXZOjjaH7xlQNDsvtYGENfxIKcjnmTcD3YIsnnEvSzcwoIRibrHYHo+Wjb2yLIJu/GvDeUM3fpLRi0eFM6k6SOyluHN/Lu1NRBOrvdS4l/rZZAwnh6/UjIR0P7o4gMecBandzVZnlyCsREdaqEuZxelWrRAlgNYUX1QpNkS+aX74cSJK66UJ7Zk2zz/XVroGR/URAngdVU3iLWoc2vcL8lqn7EO2RAgMBAAECggEAOPIgagXlRBETGPCbhDxrtNc7n86uMb/pUY3J4ktHhkcXcfeawKRAddjUamSaKUe2Ix9kNsItmJtQm5DByenu+LH2dGmg3pnuQxvC4eG8+b6LJz8nIGafDowFyf2ff0yLiwB0hRqJu24olLlRPTbNhPs8vA+kw9caOUDT7pr5NwKacDPn7X9YxSa5/i5rBYcmZTtWeBZ3GXjZfgaV4G8pkZijbETrie+4KQi51EFuk3N/3v43m9vOiLMQBZbGR1LumvwR2Y56qon+GpPGLNeRMA3eyNrgnXI+tEtYkBXnNZhIM9rkCdJZFWEAYj4IUwxI6K6ZBC2nPk4eHuBgzIxVgQKBgQDSy5X3gOCdmXZtx7vmZqunb9EiUeoyKyqBkIgtmQgfTuDr6fBwbo5RthQGLdcccp82sXs8lLvt/eGLP1fRqouglGgu0vaC+HXgUPm99wr7xOKRwIJ5Q/pAS3t91V5csNxG6zz+TEz8kpEfxFGQhihs5tIXvJsamRJ/V+m0wKnF+QKBgQCtKfTt8fl0Wa15Z91lT57uLKy+ft67odZ2iWVTcBhP4ogAWCa7L0eDU+Bo9J9A2wEMRh979FjiPyG8j6LB+KnV0JN7eTzVWzlLLFGPRFqPwJ4E3GvsXdADc32QnwjP3/+p0JD8kw0vhzUIumsQz3tf0PZ0wK0y3v2iE3Jp8SJqWQKBgQCUsubUlMZehn5DjO1g57ZZRAi8dBqIT2kJwwI9YEGZjFQgN4PUXDjrLU1M2pNvTvA/bc3oe2diyICNcR6rGeqrWWVw+oLI+yp0FctHFXlbB5VoievATZLAPj8cEiMhseWB5bm+DecVGPNk/GEpWWo1AFLeSb8EcUzaJhMH3g0lSQKBgCJtoxrj7zro0Yq/0c7gw7KLA1VkmBgqFx++NX0fXlYTrgKThC/XOJqtxIJZkIgugsjT8FKOxFVHRAffbvat3+Z477mu4x9wYbXGe/jGGFNYcpJ42KRICqGFBsQIOpJJ8OTsaMcu3YJDmZHArqTK+7aTL00LOeRRXZAp2aein7sRAoGAcKw2DTZPx3NRh7SUffIGWfTdSmOGRN9rlPC0j68HKyxoSv5T4nMDkY69HHud6OyABYfiF0r2Ir68AqTZ/gBjbxOCgJB/1Q+6GUUpSPg12d1L08c15nmfPQjdYuzgueF/yCRy8QTwbT7lXces9I+KMquwxeewl4hnzx3eo9QOtYs=";

    /**
     * jwt 密钥 id
     */
    private String keyId = "7181d52e-6f98-4a57-ba32-b9b190267bb2";

    /**
     * 忽略当前用户的属性内容
     */
    private Map<String, List<String>> ignorePrincipalPropertiesMap = new LinkedHashMap<>();

    public OAuth2Properties() {
    }

    public String getConsentPageUri() {
        return consentPageUri;
    }

    public void setConsentPageUri(String consentPageUri) {
        this.consentPageUri = consentPageUri;
    }

    public CacheProperties getAuthorizationCache() {
        return authorizationCache;
    }

    public void setAuthorizationCache(CacheProperties authorizationCache) {
        this.authorizationCache = authorizationCache;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public Map<String, List<String>> getIgnorePrincipalPropertiesMap() {
        return ignorePrincipalPropertiesMap;
    }

    public void setIgnorePrincipalPropertiesMap(Map<String, List<String>> ignorePrincipalPropertiesMap) {
        this.ignorePrincipalPropertiesMap = ignorePrincipalPropertiesMap;
    }

    public String getOidcUserInfoEndpointUri() {
        return oidcUserInfoEndpointUri;
    }

    public void setOidcUserInfoEndpointUri(String oidcUserInfoEndpointUri) {
        this.oidcUserInfoEndpointUri = oidcUserInfoEndpointUri;
    }

    public String getTokenEndpointUri() {
        return tokenEndpointUri;
    }

    public void setTokenEndpointUri(String tokenEndpointUri) {
        this.tokenEndpointUri = tokenEndpointUri;
    }

    public String getTokenRevocationEndpointUri() {
        return tokenRevocationEndpointUri;
    }

    public void setTokenRevocationEndpointUri(String tokenRevocationEndpointUri) {
        this.tokenRevocationEndpointUri = tokenRevocationEndpointUri;
    }

    public String getAuthorizePageUri() {
        return authorizePageUri;
    }

    public void setAuthorizePageUri(String authorizePageUri) {
        this.authorizePageUri = authorizePageUri;
    }

    public List<AntPathRequestMatcher> getOauth2Urls() {

        List<AntPathRequestMatcher> antPathRequestMatchers = new ArrayList<>();

        if (StringUtils.isNotEmpty(getOidcUserInfoEndpointUri())) {
            antPathRequestMatchers.add(new AntPathRequestMatcher(getOidcUserInfoEndpointUri()));
        }

        if (StringUtils.isNotEmpty(getTokenRevocationEndpointUri())) {
            antPathRequestMatchers.add(new AntPathRequestMatcher(getTokenRevocationEndpointUri()));
        }

        if (StringUtils.isNotEmpty(getTokenEndpointUri())) {
            antPathRequestMatchers.add(new AntPathRequestMatcher(getTokenEndpointUri()));
        }

        if (StringUtils.isNotEmpty(getAuthorizePageUri())) {
            antPathRequestMatchers.add(new AntPathRequestMatcher(getAuthorizePageUri()));
        }

        return antPathRequestMatchers;
    }
}
