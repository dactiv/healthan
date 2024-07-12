package com.github.dactiv.healthan.spring.security.audit;

import com.github.dactiv.healthan.mybatis.interceptor.audit.OperationDataTraceRecord;
import com.github.dactiv.healthan.security.audit.AuditEventRepositoryInterceptor;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.actuate.audit.AuditEvent;

/**
 * spring security 审计仓库拦截，用于根据 {@link AuthenticationProperties#getIgnoreAuditTypes()} 和 {@link AuthenticationProperties#getIgnoreAuditTypes()}
 * 过滤具体的审计类型和审计用户使用，如:登录失败，认证失败等审计类型不需要写入审计里。
 * @author maurice.chen
 */
public class SecurityAuditEventRepositoryInterceptor implements AuditEventRepositoryInterceptor {

    private final AuthenticationProperties authenticationProperties;

    public SecurityAuditEventRepositoryInterceptor(AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

    @Override
    public boolean preAddHandle(AuditEvent auditEvent) {
        if (CollectionUtils.isNotEmpty(authenticationProperties.getIgnoreAuditTypes())) {
            return authenticationProperties.getIgnoreAuditTypes().contains(auditEvent.getType());
        }

        if (CollectionUtils.isNotEmpty(authenticationProperties.getIgnoreAuditPrincipals())) {
            return authenticationProperties.getIgnoreAuditPrincipals().contains(auditEvent.getPrincipal());
        }

        return AuditEventRepositoryInterceptor.super.preAddHandle(auditEvent);
    }

    public boolean preAddOperationDataTraceRecordHandle(OperationDataTraceRecord record) {
        if (CollectionUtils.isEmpty(authenticationProperties.getIgnoreOperationDataTracePrincipals())) {
            return true;
        }

        return !authenticationProperties.getIgnoreOperationDataTracePrincipals().contains(record.getPrincipal().toString());
    }

    public void postAddOperationDataTraceRecordHandle(OperationDataTraceRecord record) {

    }
}
