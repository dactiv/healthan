package com.github.dactiv.healthan.spring.security.authentication.config;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.TimeProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * oauth2 配置信息
 *
 * @author maurice.chen
 */
@ConfigurationProperties("healthan.authentication.oauth2")
public class OAuth2Properties {

    /**
     * 默认的授权终端
     */
    public static final String DEFAULT_AUTHORIZE_ENDPOINT = "/oauth2/authorize";

    /**
     * 默认的注销令牌终端
     */
    public static final String DEFAULT_TOKEN_REVOCATION_ENDPOINT = "/oauth2/revoke";

    /**
     * 默认的令牌终端
     */
    public static final String DEFAULT_TOKEN_ENDPOINT = "/oauth2/token";

    /**
     * 默认的用户信息终端
     */
    public static final String DEFAULT_OIDC_USER_INFO_ENDPOINT = "/oauth2/oidc/userinfo";

    /**
     * 默认确认授权敏感信息页面
     */
    public static final String DEFAULT_CONSENT_PAGE_URI = "/oauth2/consent";

    /**
     * 默认设备认证终端
     */
    public static final String DEFAULT_DEVICE_AUTHORIZATION_ENDPOINT = "/oauth2/device/authorization";

    /**
     * 默认设备校验终端
     */
    public static final String DEFAULT_DEVICE_VERIFICATION_ENDPOINT = "/oauth2/device/verification";

    /**
     * 默认jwks 终端
     */
    public static final String DEFAULT_JWK_SET_ENDPOINT = "/oauth2/jwks";

    /**
     * 默认访问令牌元数据终端
     */
    public static final String DEFAULT_TOKEN_INTROSPECTION_ENDPOINT = "/oauth2/introspect";

    /**
     * 默认客户端注册终端
     */
    public static final String DEFAULT_OIDC_CLIENT_REGISTRATION_ENDPOINT = "/oauth2/oidc/client/register";

    /**
     * 默认 oidc 注销终端
     */
    public static final String DEFAULT_OIDC_LOGOUT_ENDPOINT = "/oauth2/oidc/logout";

    /**
     * 确认授权页面
     */
    private String consentPage = DEFAULT_CONSENT_PAGE_URI;

    /**
     * oidc 用户信息终端
     */
    private String oidcUserInfoEndpoint = DEFAULT_OIDC_USER_INFO_ENDPOINT;

    /**
     * 令牌终端
     */
    private String tokenEndpoint = DEFAULT_TOKEN_ENDPOINT;

    /**
     * 注销令牌终端
     */
    private String tokenRevocationEndpoint = DEFAULT_TOKEN_REVOCATION_ENDPOINT;

    /**
     * 授权页面终端
     */
    private String authorizeEndpoint = DEFAULT_AUTHORIZE_ENDPOINT;

    /**
     * 设备授权终端
     */
    private String deviceAuthorizationEndpoint = DEFAULT_DEVICE_AUTHORIZATION_ENDPOINT;

    /**
     * 设备校验终端
     */
    private String deviceVerificationEndpoint = DEFAULT_DEVICE_VERIFICATION_ENDPOINT;

    /**
     * jwk 终端
     */
    private String jwkSetEndpoint = DEFAULT_JWK_SET_ENDPOINT;

    /**
     * 访问令牌元数据终端
     */
    private String tokenIntrospectionEndpoint = DEFAULT_TOKEN_INTROSPECTION_ENDPOINT;

    /**
     * 默认 oidc 客户端注册终端
     */
    private String oidcClientRegistrationEndpoint = DEFAULT_OIDC_CLIENT_REGISTRATION_ENDPOINT;

    /**
     * 默认 oidc 注销终端
     */
    private String oidcLogoutEndpoint = DEFAULT_OIDC_LOGOUT_ENDPOINT;

    /**
     * 问题描述链接
     */
    private String issuer = StringUtils.EMPTY;

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

    public OAuth2Properties() {
    }

    public String getConsentPage() {
        return consentPage;
    }

    public void setConsentPage(String consentPage) {
        this.consentPage = consentPage;
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

    public String getOidcUserInfoEndpoint() {
        return oidcUserInfoEndpoint;
    }

    public void setOidcUserInfoEndpoint(String oidcUserInfoEndpoint) {
        this.oidcUserInfoEndpoint = oidcUserInfoEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getTokenRevocationEndpoint() {
        return tokenRevocationEndpoint;
    }

    public void setTokenRevocationEndpoint(String tokenRevocationEndpoint) {
        this.tokenRevocationEndpoint = tokenRevocationEndpoint;
    }

    public String getAuthorizeEndpoint() {
        return authorizeEndpoint;
    }

    public void setAuthorizeEndpoint(String authorizeEndpoint) {
        this.authorizeEndpoint = authorizeEndpoint;
    }

    public String getDeviceAuthorizationEndpoint() {
        return deviceAuthorizationEndpoint;
    }

    public void setDeviceAuthorizationEndpoint(String deviceAuthorizationEndpoint) {
        this.deviceAuthorizationEndpoint = deviceAuthorizationEndpoint;
    }

    public String getDeviceVerificationEndpoint() {
        return deviceVerificationEndpoint;
    }

    public void setDeviceVerificationEndpoint(String deviceVerificationEndpoint) {
        this.deviceVerificationEndpoint = deviceVerificationEndpoint;
    }

    public String getJwkSetEndpoint() {
        return jwkSetEndpoint;
    }

    public void setJwkSetEndpoint(String jwkSetEndpoint) {
        this.jwkSetEndpoint = jwkSetEndpoint;
    }

    public String getTokenIntrospectionEndpoint() {
        return tokenIntrospectionEndpoint;
    }

    public void setTokenIntrospectionEndpoint(String tokenIntrospectionEndpoint) {
        this.tokenIntrospectionEndpoint = tokenIntrospectionEndpoint;
    }

    public String getOidcClientRegistrationEndpoint() {
        return oidcClientRegistrationEndpoint;
    }

    public void setOidcClientRegistrationEndpoint(String oidcClientRegistrationEndpoint) {
        this.oidcClientRegistrationEndpoint = oidcClientRegistrationEndpoint;
    }

    public String getOidcLogoutEndpoint() {
        return oidcLogoutEndpoint;
    }

    public void setOidcLogoutEndpoint(String oidcLogoutEndpoint) {
        this.oidcLogoutEndpoint = oidcLogoutEndpoint;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public List<AntPathRequestMatcher> getOauth2Urls() {

        List<AntPathRequestMatcher> antPathRequestMatchers = new ArrayList<>();

        if (StringUtils.isNotEmpty(getOidcUserInfoEndpoint())) {
            antPathRequestMatchers.add(new AntPathRequestMatcher(getOidcUserInfoEndpoint()));
        }

        if (StringUtils.isNotEmpty(getTokenRevocationEndpoint())) {
            antPathRequestMatchers.add(new AntPathRequestMatcher(getTokenRevocationEndpoint()));
        }

        if (StringUtils.isNotEmpty(getTokenEndpoint())) {
            antPathRequestMatchers.add(new AntPathRequestMatcher(getTokenEndpoint()));
        }

        if (StringUtils.isNotEmpty(getAuthorizeEndpoint())) {
            antPathRequestMatchers.add(new AntPathRequestMatcher(getAuthorizeEndpoint()));
        }

        return antPathRequestMatchers;
    }
}
