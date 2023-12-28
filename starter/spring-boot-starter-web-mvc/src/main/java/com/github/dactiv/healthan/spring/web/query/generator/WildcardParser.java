package com.github.dactiv.healthan.spring.web.query.generator;

import com.github.dactiv.healthan.commons.enumerate.NameValueEnum;
import com.github.dactiv.healthan.spring.web.query.Property;

/**
 * 通配符解析器
 *
 * @author maurice.chen
 */
public interface WildcardParser<Q> extends NameValueEnum<String> {

    /**
     * 构造 query 对象
     *
     * @param property 属性信息
     * @param query 被构造的 query 对象
     */
    void structure(Property property, Q query);

    /**
     * 是否支持条件
     *
     * @param condition 条件内容
     *
     * @return true 是，否则 false
     */
    boolean isSupport(String condition);
}
