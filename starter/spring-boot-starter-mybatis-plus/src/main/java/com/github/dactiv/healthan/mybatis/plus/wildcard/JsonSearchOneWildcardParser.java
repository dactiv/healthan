package com.github.dactiv.healthan.mybatis.plus.wildcard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.spring.web.query.Property;
import org.apache.commons.lang3.StringUtils;

/**
 * json 查询的返回第一个匹配项的路径通配符解析器实现
 *
 * @author maurice.chen
 *
 * @param <T>
 */
public class JsonSearchOneWildcardParser<T> extends AbstractJsonFunctionWildcardParser<T> {

    private final static String DEFAULT_WILDCARD_VALUE = "jso";

    private final static String DEFAULT_WILDCARD_NAME = "Json 数据格式 one 查询";

    @Override
    public void structure(Property property, QueryWrapper<T> queryWrapper) {
        LikeWildcardParser.addMatchSymbol(property);
        super.structure(property, queryWrapper);
    }

    /**
     * 获取表达式
     *
     * @param propertyName 属性名称
     * @param index 值索引
     *
     * @return JSON_CONTAINS 表达式
     */
    public String getExpression(String propertyName, Integer index) {

        if (StringUtils.contains(propertyName, Casts.DOT)) {
            String path = StringUtils.substringAfter(propertyName, Casts.DOT);
            String field = StringUtils.substringBefore(propertyName, Casts.DOT);
            return "JSON_SEARCH(" + field + "->'$[*]." + path  + "', 'one', {" + index + "}, '$') IS NOT NULL";
        }

        return "JSON_SEARCH(" + propertyName + ", 'one', {" + index + "}) IS NOT NULL";
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
