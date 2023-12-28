package com.github.dactiv.healthan.spring.security.authentication.config;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.TimeProperties;
import com.github.dactiv.healthan.crypto.CipherAlgorithmService;

/**
 * 访问 token 配置
 *
 * @author maurice.chen
 */
public class AccessTokenProperties {

    public final static String DEFAULT_ACCESS_TOKEN_HEADER_NAME = "X-ACCESS-TOKEN";

    public final static String DEFAULT_REFRESH_TOKEN_HEADER_NAME = "X-REFRESH-TOKEN";

    public final static String DEFAULT_ACCESS_TOKEN_PARAM_NAME = "accessToken";

    public final static String DEFAULT_REFRESH_TOKEN_PARAM_NAME = "refreshToken";

    /**
     * 默认存储在 redis 的 security context key 名称
     */
    public final static String DEFAULT_SPRING_SECURITY_CONTEXT_KEY = "spring:security:context:access-token:";

    /**
     * 加解密算法名称
     */
    private String cipherAlgorithmName = CipherAlgorithmService.AES_ALGORITHM;

    /**
     * 加解密密钥
     */
    private String key;

    /**
     * 缓存配置
     */
    private CacheProperties cache = CacheProperties.of(DEFAULT_SPRING_SECURITY_CONTEXT_KEY, TimeProperties.ofDay(1));

    /**
     * 访问 token 头名称
     */
    private String accessTokenHeaderName = DEFAULT_ACCESS_TOKEN_HEADER_NAME;

    /**
     * 访问 token 参数名称
     */
    private String accessTokenParamName = DEFAULT_ACCESS_TOKEN_PARAM_NAME;

    /**
     * 刷新 token 头名称
     */
    private String refreshTokenHeaderName = DEFAULT_REFRESH_TOKEN_HEADER_NAME;

    /**
     * 刷新 token 参数名称
     */
    private String refreshTokenParamName = DEFAULT_REFRESH_TOKEN_PARAM_NAME;

    public AccessTokenProperties() {
    }

    public String getCipherAlgorithmName() {
        return cipherAlgorithmName;
    }

    public void setCipherAlgorithmName(String cipherAlgorithmName) {
        this.cipherAlgorithmName = cipherAlgorithmName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public CacheProperties getCache() {
        return cache;
    }

    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }

    public String getAccessTokenHeaderName() {
        return accessTokenHeaderName;
    }

    public void setAccessTokenHeaderName(String accessTokenHeaderName) {
        this.accessTokenHeaderName = accessTokenHeaderName;
    }

    public String getAccessTokenParamName() {
        return accessTokenParamName;
    }

    public void setAccessTokenParamName(String accessTokenParamName) {
        this.accessTokenParamName = accessTokenParamName;
    }

    public String getRefreshTokenHeaderName() {
        return refreshTokenHeaderName;
    }

    public void setRefreshTokenHeaderName(String refreshTokenHeaderName) {
        this.refreshTokenHeaderName = refreshTokenHeaderName;
    }

    public String getRefreshTokenParamName() {
        return refreshTokenParamName;
    }

    public void setRefreshTokenParamName(String refreshTokenParamName) {
        this.refreshTokenParamName = refreshTokenParamName;
    }
}
