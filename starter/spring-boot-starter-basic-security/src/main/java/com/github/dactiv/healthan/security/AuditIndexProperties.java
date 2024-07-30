package com.github.dactiv.healthan.security;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.commons.id.number.NumberIdEntity;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 审计索引配置
 *
 * @author maurice.chen
 */
@ConfigurationProperties("healthan.security.audit.index")
public class AuditIndexProperties implements Serializable {
    public static final String DEFAULT_PATTERN = "yyyy_MM_dd";
    /**
     * 前缀
     */
    private String prefix = "audit_event";

    /**
     * 分隔符
     */
    private String separator = Casts.UNDERSCORE;

    /**
     * 获取属性值得属性名称
     */
    private List<String> propertyNames = new LinkedList<>();

    /**
     * 时间时间格式化内容
     */
    private String pattern = DEFAULT_PATTERN;

    /**
     * 时间属性的名称
     */
    private List<String> datePropertyNames = Arrays.asList(RestResult.DEFAULT_TIMESTAMP_NAME, NumberIdEntity.CREATION_TIME_FIELD_NAME);

    public AuditIndexProperties() {
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public List<String> getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public List<String> getDatePropertyNames() {
        return datePropertyNames;
    }

    public void setDatePropertyNames(List<String> datePropertyNames) {
        this.datePropertyNames = datePropertyNames;
    }
}
