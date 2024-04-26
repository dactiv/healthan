package com.github.dactiv.healthan.commons.enumerate.support;

import com.github.dactiv.healthan.commons.enumerate.NameValueEnum;

/**
 * 协议类型枚举
 *
 * @author maurice.chen
 */
public enum Protocol implements NameValueEnum<Integer> {

    /**
     * http 协议
     */
    HTTP_OR_HTTPS("http(s)", 10);

    private final String name;

    private final Integer value;

    Protocol(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
