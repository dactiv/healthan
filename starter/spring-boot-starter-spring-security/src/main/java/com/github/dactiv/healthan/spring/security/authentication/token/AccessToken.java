package com.github.dactiv.healthan.spring.security.authentication.token;

import com.github.dactiv.healthan.commons.TimeProperties;

import java.util.Date;

/**
 * 认证访问 token
 *
 * @author maurice.chen
 */
public class AccessToken implements ExpiredToken {

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * token 值
     */
    private String token;

    /**
     * 超时时间
     */
    private TimeProperties expiresTime;

    public AccessToken() {
    }

    public AccessToken(String token, TimeProperties expiresTime) {
        this.token = token;
        this.expiresTime = expiresTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public TimeProperties getExpiresTime() {
        return expiresTime;
    }

    public void setExpiresTime(TimeProperties expiresTime) {
        this.expiresTime = expiresTime;
    }
}
