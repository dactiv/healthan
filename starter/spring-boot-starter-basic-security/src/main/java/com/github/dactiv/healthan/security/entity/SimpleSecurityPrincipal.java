package com.github.dactiv.healthan.security.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.healthan.security.enumerate.UserStatus;

import java.util.LinkedHashMap;
import java.util.Map;

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

    private String type;

    private Map<String, Object> metadata = new LinkedHashMap<>();

    public SimpleSecurityPrincipal() {
    }

    public SimpleSecurityPrincipal(Object id,
                                   Object credentials,
                                   String username,
                                   String type) {
        this(id, credentials, username, type, UserStatus.Enabled);
    }

    public SimpleSecurityPrincipal(Object id,
                                   Object credentials,
                                   String username,
                                   String type,
                                   UserStatus status) {
        this(id,credentials, username, status, type, new LinkedHashMap<>());
    }

    public SimpleSecurityPrincipal(Object id,
                                   Object credentials,
                                   String username,
                                   UserStatus status,
                                   String type,
                                   Map<String, Object> metadata) {
        this.id = id;
        this.credentials = credentials;
        this.username = username;
        this.status = status;
        this.type = type;
        this.metadata = metadata;
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
    public String getType() {
        return type;
    }

    @Override
    public boolean isNonExpired() {
        return false;
    }

    @Override
    public boolean isNonLocked() {
        return !UserStatus.Lock.equals(status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isDisabled() {
        return UserStatus.Disabled.equals(status);
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
