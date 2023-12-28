package com.github.dactiv.healthan.mybatis.plus.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.healthan.commons.exception.SystemException;
import com.github.dactiv.healthan.spring.web.query.Property;
import com.github.dactiv.healthan.spring.web.query.generator.WildcardParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 范围查询通配符实现
 *
 * @author maurice.chen
 */
public class BetweenWildcardParser<T> implements WildcardParser<QueryWrapper<T>> {

    private final static String DEFAULT_WILDCARD_VALUE = "between";

    private final static String DEFAULT_WILDCARD_NAME = "开始以";

    @Override
    public void structure(Property property, QueryWrapper<T> queryWrapper) {

        if (!Iterable.class.isAssignableFrom(property.getValue().getClass())) {
            throw new SystemException("Between 查询的参数值的数组必须大于 1 位");
        }

        Iterable<?> iterable = (Iterable<?>) property.getValue();
        List<Object> values = new ArrayList<>();

        iterable.forEach(values::add);

        if (values.size() < 2) {
            throw new SystemException("Between 查询必须参数值的数组大小为 2 位");
        }

        queryWrapper.between(property.getPropertyName(), values.iterator().next(), values.get(values.size() - 1));
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
