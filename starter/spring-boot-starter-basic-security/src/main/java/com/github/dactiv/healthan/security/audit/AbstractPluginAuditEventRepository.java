package com.github.dactiv.healthan.security.audit;

import com.github.dactiv.healthan.security.AuditProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.actuate.audit.AuditEvent;

public abstract class AbstractPluginAuditEventRepository implements PluginAuditEventRepository{

    protected final AuditProperties auditProperties;

    public AbstractPluginAuditEventRepository(AuditProperties auditProperties) {
        this.auditProperties = auditProperties;
    }

    @Override
    public void add(AuditEvent event) {

        if (CollectionUtils.isNotEmpty(auditProperties.getIgnoreTypes()) && auditProperties.getIgnoreTypes().contains(event.getType())) {
            return ;
        }

        if (CollectionUtils.isNotEmpty(auditProperties.getIgnorePrincipals()) && auditProperties.getIgnorePrincipals().contains(event.getPrincipal())) {
            return ;
        }

        doAdd(event);
    }

    protected abstract void doAdd(AuditEvent event);

}
