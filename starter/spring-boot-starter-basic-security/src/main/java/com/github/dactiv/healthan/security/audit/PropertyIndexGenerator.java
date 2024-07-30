package com.github.dactiv.healthan.security.audit;

import com.github.dactiv.healthan.commons.ReflectionUtils;
import com.github.dactiv.healthan.security.AuditIndexProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 属性索引生成器实现
 *
 * @author maurice.chen
 */
public class PropertyIndexGenerator implements IndexGenerator {

    private AuditIndexProperties auditIndexProperties;

    public PropertyIndexGenerator() {
    }

    public PropertyIndexGenerator(AuditIndexProperties auditIndexProperties) {
        this.auditIndexProperties = auditIndexProperties;
    }

    @Override
    public String generateIndex(Object object) {

        List<String> result = new LinkedList<>();

        if (StringUtils.isNotBlank(auditIndexProperties.getPrefix())) {
            result.add(auditIndexProperties.getPrefix());
        }

        auditIndexProperties
                .getPropertyNames()
                .stream()
                .map(s -> ReflectionUtils.getReadProperty(object, s))
                .filter(Objects::nonNull)
                .forEach(s -> result.add(s.toString()));

        List<String> append = afterAppend(object);

        if (CollectionUtils.isNotEmpty(append)) {
            result.addAll(append);
        }

        return afterSetting(result);
    }

    protected List<String> afterAppend(Object object) {
        return new LinkedList<>();
    }

    /**
     * 执行后续设置
     *
     * @param result 组装好的属性集合
     *
     * @return 索引值
     */
    protected String afterSetting(List<String> result) {
        return StringUtils.join(result, auditIndexProperties.getSeparator());
    }

    public AuditIndexProperties getAuditIndexProperties() {
        return auditIndexProperties;
    }

    public void setAuditIndexProperties(AuditIndexProperties auditIndexProperties) {
        this.auditIndexProperties = auditIndexProperties;
    }
}
