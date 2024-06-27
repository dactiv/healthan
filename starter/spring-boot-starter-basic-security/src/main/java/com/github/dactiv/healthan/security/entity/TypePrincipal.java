package com.github.dactiv.healthan.security.entity;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.id.BasicIdentification;

import java.security.Principal;

/**
 * 带用户类型的用户明细实体
 *
 * @param <T> 主键 id 类型
 */
public interface TypePrincipal<T> extends Principal, BasicIdentification<T> {

    /**
     * 获取用户主键 id
     *
     * @return 用户主键 id
     */
    T getId();

    /**
     * 设置用户主键 id
     *
     * @param id 用户主键 id
     */
    void setId(T id);

    /**
     * 获取登陆账户
     *
     * @return 登陆账户
     */
    String getName();

    /**
     * 设置登陆账户
     *
     * @param name 登陆账户
     */
    void setName(String name);

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    String getType();

    /**
     * 设置用户类型
     *
     * @param type 用户类型
     */
    void setType(String type);

    /**
     * 设置用户信息
     *
     * @param userDetails 基础用户信息
     */
    default void setUserDetails(TypePrincipal<T> userDetails) {
        this.setId(userDetails.getId());
        this.setName(userDetails.getName());
        this.setType(userDetails.getType());
    }

    /**
     * 获取唯一值
     *
     * @return 唯一值
     */
    default String toUniqueValue() {
        return getType() + CacheProperties.DEFAULT_SEPARATOR + getId() + CacheProperties.DEFAULT_SEPARATOR + getName();
    }
}
