package com.github.dactiv.healthan.mybatis.plus.wildcard;

import com.github.dactiv.healthan.commons.Casts;
import org.apache.commons.lang3.StringUtils;

/**
 * json 格式的包含查询通配符实现
 *
 * @author maurice.chen
 */
public class JsonContainsWildcardParser<T> extends AbstractJsonFunctionWildcardParser<T> {

    private final static String DEFAULT_WILDCARD_VALUE = "jin";

    private final static String DEFAULT_WILDCARD_NAME = "Json 数据格式值包含";

    @Override
    public boolean isSupport(String condition) {
        return DEFAULT_WILDCARD_VALUE.equals(condition);
    }

    @Override
    protected String getExpression(String propertyName, Integer index) {

        if (StringUtils.contains(propertyName, Casts.DOT)) {
            String path = StringUtils.substringAfter(propertyName, Casts.DOT);
            String field = StringUtils.substringBefore(propertyName, Casts.DOT);
            return "JSON_CONTAINS(" + field + "->'$[*]." + path  + "', {" + index + "}, '$')";
        }

        return "JSON_CONTAINS(" + propertyName + ", {" + index + "})";
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
