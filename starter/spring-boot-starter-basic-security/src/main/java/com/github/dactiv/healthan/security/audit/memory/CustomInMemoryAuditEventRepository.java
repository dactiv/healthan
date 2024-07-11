package com.github.dactiv.healthan.security.audit.memory;

import com.github.dactiv.healthan.security.AuditProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;

public class CustomInMemoryAuditEventRepository extends InMemoryAuditEventRepository {
    private final AuditProperties auditProperties;

    public CustomInMemoryAuditEventRepository(int capacity, AuditProperties auditProperties) {
        super(capacity);
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

        super.add(event);
    }
}
