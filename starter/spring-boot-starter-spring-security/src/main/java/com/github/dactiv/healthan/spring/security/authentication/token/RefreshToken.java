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
    private ExpiredToken accessToken;

    public RefreshToken() {
    }

    public RefreshToken(ExpiredToken accessToken) {
        this.accessToken = accessToken;
    }

    public RefreshToken(String token, TimeProperties expiresTime, ExpiredToken accessToken) {
        super(token, expiresTime);
        this.accessToken = accessToken;
    }

    public RefreshToken(AccessToken refreshToken, ExpiredToken accessToken) {
        this(refreshToken.getToken(), refreshToken.getExpiresTime(), accessToken);
    }

    public ExpiredToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(ExpiredToken accessToken) {
        this.accessToken = accessToken;
    }
}
