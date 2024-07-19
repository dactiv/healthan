package com.github.dactiv.healthan.security.entity.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.security.enumerate.UserStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * 简单的安全用户实现
 *
 * @author maurice.chen
 */
public class SimpleSecurityPrincipal implements SecurityPrincipal {

    private Object id;

    @JsonIgnore
    private Object credentials;

    private String username;

    @JsonIgnore
    private UserStatus status;

    public SimpleSecurityPrincipal() {
    }

    public SimpleSecurityPrincipal(String splitString, Object credentials, UserStatus status) {
        String[] split = StringUtils.splitByWholeSeparator(splitString, CacheProperties.DEFAULT_SEPARATOR);
        Assert.isTrue(split.length == 2, "分割字符串错误，格式应该为: <用户 id>:<用户登录信息>");
        this.id = split[0];
        this.username = split[1];
        this.credentials = credentials;
        this.status = status;
    }

    public SimpleSecurityPrincipal(Object id,
                                   Object credentials,
                                   String username) {
        this(id, credentials, username, UserStatus.Enabled);
    }

    public SimpleSecurityPrincipal(Object id,
                                   Object credentials,
                                   String username,
                                   UserStatus status) {
        this.id = id;
        this.credentials = credentials;
        this.username = username;
        this.status = status;
    }

    @Override
    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    @Override
    @JsonIgnore
    public boolean isNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isNonLocked() {
        return !UserStatus.Lock.equals(status);
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isDisabled() {
        return UserStatus.Disabled.equals(status);
    }
}
