package com.github.dactiv.healthan.commons.enumerate.support;

import com.github.dactiv.healthan.commons.enumerate.NameValueEnum;
import org.apache.commons.lang3.BooleanUtils;

/**
 * 是或否枚举
 *
 * @author maurice
 */
public enum YesOrNo implements NameValueEnum<Integer> {

    /**
     * 是
     */
    Yes(1, "是"),
    /**
     * 否
     */
    No(0, "否");

    /**
     * 值
     */
    private final Integer value;

    /**
     * 名称
     */
    private final String name;

    /**
     * 是或否枚举
     *
     * @param value 值
     * @param name  名称
     */
    YesOrNo(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 转型成 boolean 值
     *
     * @return boolean 值
     */
    public boolean toBoolean() {
        return BooleanUtils.toBoolean(getValue());
    }

    public static YesOrNo ofBoolean(boolean value) {
        return value ? Yes : No;
    }
}

