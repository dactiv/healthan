package com.github.dactiv.healthan.spring.security.audit;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.mybatis.interceptor.audit.OperationDataTraceRecord;
import com.github.dactiv.healthan.security.audit.AuditEventRepositoryInterceptor;
import com.github.dactiv.healthan.spring.security.authentication.RememberMeAuthenticationDetails;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.RememberMeProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.AuthenticationSuccessToken;
import com.github.dactiv.healthan.spring.security.entity.AuthenticationSuccessDetails;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.security.AuthenticationAuditListener;
import org.springframework.util.AntPathMatcher;

/**
 * spring security 审计仓库拦截，用于根据 {@link AuthenticationProperties#getIgnoreAuditTypes()} 和 {@link AuthenticationProperties#getIgnoreAuditTypes()}
 * 过滤具体的审计类型和审计用户使用，如:登录失败，认证失败等审计类型不需要写入审计里。
 * @author maurice.chen
 */
public class SecurityAuditEventRepositoryInterceptor implements AuditEventRepositoryInterceptor {

    private final AuthenticationProperties authenticationProperties;

    private final RememberMeProperties rememberMeProperties;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public SecurityAuditEventRepositoryInterceptor(AuthenticationProperties authenticationProperties,
                                                   RememberMeProperties rememberMeProperties) {
        this.authenticationProperties = authenticationProperties;
        this.rememberMeProperties = rememberMeProperties;
    }

    @Override
    public boolean preAddHandle(AuditEvent auditEvent) {
        if (CollectionUtils.isNotEmpty(authenticationProperties.getIgnoreAuditTypes()) && authenticationProperties.getIgnoreAuditTypes().contains(auditEvent.getType())) {
            return false;
        }

        if (CollectionUtils.isNotEmpty(authenticationProperties.getIgnoreAuditPrincipals()) && authenticationProperties.getIgnoreAuditPrincipals().contains(auditEvent.getPrincipal())) {
            return false;
        }

        if (auditEvent.getType().equals(AuthenticationAuditListener.AUTHENTICATION_SUCCESS)) {
            Object details = auditEvent.getData().get(AuthenticationSuccessToken.DETAILS_KEY);
            if (!AuthenticationSuccessDetails.class.isAssignableFrom(details.getClass())) {
                return AuditEventRepositoryInterceptor.super.preAddHandle(auditEvent);
            }

            AuthenticationSuccessDetails successDetails = Casts.cast(details);
            if (!successDetails.isRemember()) {
                return AuditEventRepositoryInterceptor.super.preAddHandle(auditEvent);
            }

            if (!RememberMeAuthenticationDetails.class.isAssignableFrom(successDetails.getRequestDetails().getClass())) {
                return AuditEventRepositoryInterceptor.super.preAddHandle(auditEvent);
            }

            RememberMeAuthenticationDetails requestAuthenticationDetails = Casts.cast(successDetails.getRequestDetails());
            return antPathMatcher.match(rememberMeProperties.getLoginProcessingUrl(), requestAuthenticationDetails.getRequestUri());
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
