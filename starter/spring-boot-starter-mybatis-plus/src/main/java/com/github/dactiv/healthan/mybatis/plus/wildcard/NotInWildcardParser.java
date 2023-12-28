package com.github.dactiv.healthan.mybatis.plus.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.spring.web.query.Property;
import com.github.dactiv.healthan.spring.web.query.generator.WildcardParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 等于查询通配符实现
 *
 * @author maurice.chen
 */
public class NotInWildcardParser<T> implements WildcardParser<QueryWrapper<T>> {

    private final static String DEFAULT_WILDCARD_VALUE = "nin";

    private final static String DEFAULT_WILDCARD_NAME = "不在列表里 (not in)";

    @Override
    public void structure(Property property, QueryWrapper<T> queryWrapper) {
        if (Iterable.class.isAssignableFrom(property.getValue().getClass())) {

            Iterable<?> iterable = Casts.cast(property.getValue());
            List<Object> values = new ArrayList<>();

            iterable.forEach(values::add);
            queryWrapper.notIn(property.getPropertyName(), values.toArray());
        } else {
            queryWrapper.notIn(property.getPropertyName(), property.getValue());
        }
    }

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_VALUE.equals(condition);
    }

    @Override
    public String getName() {
        return DEFAULT_WILDCARD_NAME;
    }

    @Override
    public String getValue() {
        return DEFAULT_WILDCARD_VALUE;
    }
}
