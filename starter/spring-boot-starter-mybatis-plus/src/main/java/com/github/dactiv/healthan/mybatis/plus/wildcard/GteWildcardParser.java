package com.github.dactiv.healthan.mybatis.plus.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.healthan.spring.web.query.Property;
import com.github.dactiv.healthan.spring.web.query.generator.WildcardParser;

/**
 * 大于等于查询通配符实现
 *
 * @author maurice.chen
 */
public class GteWildcardParser<T> implements WildcardParser<QueryWrapper<T>> {

    private final static String DEFAULT_WILDCARD_VALUE = "gte";

    private final static String DEFAULT_WILDCARD_NAME = "小于等于";

    @Override
    public void structure(Property property, QueryWrapper<T> queryWrapper) {
        queryWrapper.ge(property.getPropertyName(), property.getValue());
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
