package com.github.dactiv.healthan.spring.security.authentication.token;

import com.github.dactiv.healthan.commons.TimeProperties;

/**
 * 刷新 token
 *
 * @author maurice.chen
 */
public class RefreshToken extends AccessToken {

    /**
     * 对应的访问 token
     */
    private AccessToken accessToken;

    public RefreshToken() {
    }

    public RefreshToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    public RefreshToken(String token, TimeProperties expiresTime, AccessToken accessToken) {
        super(token, expiresTime);
        this.accessToken = accessToken;
    }

    public RefreshToken(AccessToken refreshToken, AccessToken accessToken) {
        this(refreshToken.getToken(), refreshToken.getExpiresTime(), accessToken);
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }
}
