package com.github.dactiv.healthan.mybatis.plus.config;

import com.github.dactiv.healthan.mybatis.interceptor.audit.AbstractOperationDataTraceResolver;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 操作日志追踪配置
 *
 * @author maurice.chen
 */
@ConfigurationProperties("healthan.mybatis.plus.operation-data-trace")
public class OperationDataTraceProperties {

    public static final String DEFAULT_AUDIT_PREFIX_NAME = "OPERATION_DATA_AUDIT";

    /**
     * 审计前缀名称
     */
    private String auditPrefixName = DEFAULT_AUDIT_PREFIX_NAME;

    /**
     * 日志格式化内容
     */
    private String dateFormat = AbstractOperationDataTraceResolver.DEFAULT_DATE_FORMATTER_PATTERN;

    public OperationDataTraceProperties() {
    }

    public String getAuditPrefixName() {
        return auditPrefixName;
    }

    public void setAuditPrefixName(String auditPrefixName) {
        this.auditPrefixName = auditPrefixName;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}
