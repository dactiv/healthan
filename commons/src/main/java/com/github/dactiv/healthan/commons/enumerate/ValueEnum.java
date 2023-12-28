package com.github.dactiv.healthan.commons.enumerate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.dactiv.healthan.commons.jackson.deserializer.ValueEnumDeserializer;

/**
 * 带有值得枚举接口
 *
 * @param <V> 值类型
 */
@JsonDeserialize(using = ValueEnumDeserializer.class)
public interface ValueEnum<V> {

    String METHOD_NAME = "getValue";

    String FIELD_NAME = "value";

    /**
     * 获取值
     *
     * @return 值
     */
    V getValue();

}
