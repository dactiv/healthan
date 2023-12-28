package com.github.dactiv.healthan.security.enumerate;

import com.github.dactiv.healthan.commons.enumerate.NameEnum;

import java.util.Arrays;
import java.util.List;

/**
 * 资源类型枚举
 *
 * @author maurice
 */
public enum ResourceType implements NameEnum {

    /**
     * 根目录
     */
    Root("根目录"),

    /**
     * 子目录
     */
    Directory("子目录"),

    /**
     * 菜单类型
     */
    Menu("菜单类型"),
    /**
     * 安全类型
     */
    Security("安全类型");

    /**
     * 资源类型枚举
     *
     * @param name 名称
     */
    ResourceType(String name) {
        this.name = name;
    }

    /**
     * 名称
     */
    private final String name;

    @Override
    public String getName() {
        return name;
    }

    public static final List<ResourceType> DEFAULT_TYPE = Arrays.asList(Root, Directory);
}

