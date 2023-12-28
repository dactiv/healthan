package com.github.dactiv.healthan.commons.enumerate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.dactiv.healthan.commons.jackson.deserializer.NameEnumDeserializer;

/**
 * 带有名称的枚举接口
 *
 * @author maurice.chen
 */
@JsonDeserialize(using = NameEnumDeserializer.class)
public interface NameEnum {

    String FIELD_NAME = "name";

    /**
     * 获取名称
     *
     * @return 名称
     */
    String getName();
}

