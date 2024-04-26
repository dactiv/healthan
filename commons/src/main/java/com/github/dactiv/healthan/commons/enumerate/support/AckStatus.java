package com.github.dactiv.healthan.commons.enumerate.support;

import com.github.dactiv.healthan.commons.enumerate.NameValueEnum;

/**
 * 确认应答状态实枚举
 *
 * @author maurice.chen
 */
public enum AckStatus implements NameValueEnum<Integer> {

    /**
     * 确认
     */
    ACKNOWLEDGED("确认", 10),

    /**
     * 拒绝
     */
    REJECT("拒绝", 20),

    /**
     * 忽略
     */
    NEGLECT("忽略", 30),

    /**
     * 未知
     */
    UNKNOWN("未知", 40),

    ;

    AckStatus(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    private final String name;

    private final Integer value;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
