package com.github.dactiv.healthan.spring.web.query.condition;

import com.github.dactiv.healthan.commons.enumerate.NameEnum;

/**
 * 条件类型
 *
 * @author maurice.chen
 */
public enum ConditionType implements NameEnum {

    /**
     * 并且条件
     */
    And("并且条件"),
    /**
     * 或者条件
     */
    Or("或者条件");

    ConditionType(String name) {
        this.name = name;
    }

    private final String name;

    @Override
    public String getName() {
        return name;
    }
}
