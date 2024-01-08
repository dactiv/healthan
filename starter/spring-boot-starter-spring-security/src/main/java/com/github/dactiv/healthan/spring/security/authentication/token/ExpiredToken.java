package com.github.dactiv.healthan.spring.security.authentication.token;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.healthan.commons.TimeProperties;

import java.io.Serializable;
import java.util.Date;

/**
 * 可过期的 token
 *
 * @author maurice.chen
 */
public interface ExpiredToken extends Serializable {

    /**
     * 获取 token 值
     *
     * @return token 值
     */
    String getToken();

    /**
     * 获取超时时间
     *
     * @return 超时时间
     */
    TimeProperties getExpiresTime();

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    Date getCreationTime();

    /**
     * 是否超时
     *
     * @return true 是，否则 false
     */
    @JsonIgnore
    default boolean isExpired() {
        return new Date(getCreationTime().getTime() + getExpiresInMillis()).before(new Date());
    }

    default long getExpiresInMillis() {
        return getExpiresTime().toMillis();
    }

    default Date getExpiresInDateTime() {
        return new Date(System.currentTimeMillis() + getExpiresTime().toMillis());
    }
}
