package com.github.dactiv.healthan.spring.security.audit;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.security.audit.AuditEventRepositoryInterceptor;
import com.github.dactiv.healthan.spring.security.audit.config.AuditDetailsSource;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.RememberMeProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.AuditAuthenticationToken;
import com.github.dactiv.healthan.spring.security.entity.AuditAuthenticationSuccessDetails;
import com.github.dactiv.healthan.spring.security.entity.RememberMeAuthenticationDetails;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.util.AntPathMatcher;

/**
 * spring security 审计仓库拦截，用于根据 {@link AuthenticationProperties#getIgnoreAuditTypes()} 和 {@link AuthenticationProperties#getIgnoreAuditTypes()}
 * 过滤具体的审计类型和审计用户使用，如:登录失败，认证失败等审计类型不需要写入审计里。
 *
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

        Object details = auditEvent.getData().get(AuditAuthenticationToken.DETAILS_KEY);
        if (!AuditDetailsSource.class.isAssignableFrom(details.getClass())) {
            return false;
        }

        if (AuditAuthenticationSuccessDetails.class.isAssignableFrom(details.getClass())) {
            AuditAuthenticationSuccessDetails auditDetails = Casts.cast(details);
            return postAuditAuthenticationSuccessDetails(auditDetails, auditEvent);
        }

        return AuditEventRepositoryInterceptor.super.preAddHandle(auditEvent);
    }

    private boolean postAuditAuthenticationSuccessDetails(AuditAuthenticationSuccessDetails auditDetails, AuditEvent auditEvent) {
        if (!auditDetails.isRemember()) {
            return AuditEventRepositoryInterceptor.super.preAddHandle(auditEvent);
        }

        if (!RememberMeAuthenticationDetails.class.isAssignableFrom(auditDetails.getRequestDetails().getClass())) {
            return AuditEventRepositoryInterceptor.super.preAddHandle(auditEvent);
        }

        RememberMeAuthenticationDetails requestAuthenticationDetails = Casts.cast(auditDetails.getRequestDetails());
        return antPathMatcher.match(rememberMeProperties.getLoginProcessingUrl(), requestAuthenticationDetails.getRequestUri());
    }

}
