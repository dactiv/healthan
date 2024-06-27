package com.github.dactiv.healthan.security.entity.support;

import com.github.dactiv.healthan.security.entity.TypePrincipal;

import java.io.Serializable;

/**
 * 基础用户信息
 *
 * @author maurice.chen
 */
public class SimpleTypePrincipal<T> implements Serializable, TypePrincipal<T> {

    /**
     * 用户类型字段名称
     */
    public static final String TYPE_FIELD_NAME = "type";

    /**
     * 登陆账户字段名称
     */
    public static final String NAME_FIELD_NAME = "name";

    /**
     * 用户 id
     */
    private T id;

    /**
     * 登陆账户
     */
    private String name;

    /**
     * 用户类型
     */
    private String type;

    /**
     * 创建一个新的基础用户信息
     *
     * @param id 用户 id
     * @param name 登陆账号
     * @param type 用户类型
     */
    public SimpleTypePrincipal(T id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    /**
     * 创建一个新的基础用户信息
     */
    public SimpleTypePrincipal() {
    }

    /**
     * 获取用户主键 id
     *
     * @return 用户主键 id
     */
    public T getId() {
        return id;
    }

    /**
     * 设置用户主键 id
     *
     * @param id 用户主键 id
     */
    public void setId(T id) {
        this.id = id;
    }

    /**
     * 获取登陆账户
     *
     * @return 登陆账户
     */
    public String getName() {
        return name;
    }

    /**
     * 设置登陆账户
     *
     * @param name 登陆账户
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置用户类型
     *
     * @param type 用户类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 创建一个新的基础用户信息
     *
     * @param userId 用户 id
     * @param username 登陆账号
     * @param userType 用户类型
     *
     * @return 新的基础用户信息
     */
    public static <T> SimpleTypePrincipal<T> of(T userId, String username, String userType) {
        return new SimpleTypePrincipal<>(userId, username, userType);
    }
}
