package com.github.dactiv.healthan.commons.enumerate.support;

import com.github.dactiv.healthan.commons.enumerate.NameValueEnum;

/**
 * 启用禁用枚举
 *
 * @author maurice
 */
public enum DisabledOrEnabled implements NameValueEnum<Integer> {

    /**
     * 启用
     */
    Enabled(1, "启用"),
    /**
     * 禁用
     */
    Disabled(0, "禁用");

    /**
     * 启用禁用枚举
     *
     * @param value 值
     * @param name  名称
     */
    DisabledOrEnabled(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * 值
     */
    private final Integer value;

    /**
     * 名称
     */
    private final String name;

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
