package com.github.dactiv.healthan.security.entity;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.id.BasicIdentification;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 安全用户接口，拥有描述一个完整登录后的用户。
 *
 * @author maurice.chen
 */
public interface SecurityPrincipal extends Principal, BasicIdentification<Object> {

    /**
     * 获取账户唯一识别
     *
     * @return 唯一识别
     */
    Object getId();

    /**
     * 获取凭证
     *
     * @return 凭证
     */
    Object getCredentials();

    /**
     * 获取登录帐号
     *
     * @return 登录帐号
     */
    String getUsername();

    /**
     * 获取账户类型
     *
     * @return 账户类型
     */
    String getType();

    /**
     * 判断账号是否过期
     *
     * @return true 是，否则 false
     */
    boolean isNonExpired();

    /**
     * 判断账号是否未锁定
     *
     * @return true 是，否则 false
     */
    boolean isNonLocked();

    /**
     * 判断密码是否过期
     *
     * @return true 是，否则 false
     */
    boolean isCredentialsNonExpired();

    /**
     * 判断账户是否启用
     *
     * @return true 是，否则 false
     */
    boolean isDisabled();

    /**
     * 获取账户画像
     *
     * @return 账户画像
     */
    default Map<String, Object> getMetadata() {
        return new LinkedHashMap<>();
    }

    /**
     * 获取用户基本信息
     *
     * @return 用户基本信息
     *
     */
    default <T> TypeUserDetails<T> toTypeUserDetails() {
        return new BasicUserDetails<>(Casts.cast(getId()), getUsername(), getType());
    }

    @Override
    default String getName() {
        return getType() + CacheProperties.DEFAULT_SEPARATOR + getId() + CacheProperties.DEFAULT_SEPARATOR + getUsername();
    }

}
