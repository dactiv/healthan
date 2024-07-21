package com.github.dactiv.healthan.mybatis.plus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("healthan.mybatis.plus.operation-data-trace")
public class OperationDataTraceProperties {

    public static final String DEFAULT_AUDIT_PREFIX_NAME = "OPERATION_DATA_AUDIT";

    private String auditPrefixName = DEFAULT_AUDIT_PREFIX_NAME;

    public OperationDataTraceProperties() {
    }

    public String getAuditPrefixName() {
        return auditPrefixName;
    }

    public void setAuditPrefixName(String auditPrefixName) {
        this.auditPrefixName = auditPrefixName;
    }
}
