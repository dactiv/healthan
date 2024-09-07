package com.github.dactiv.healthan.security.audit;

import org.springframework.boot.actuate.audit.AuditEvent;

import java.io.Serial;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 审计事件实体类
 *
 * @author maurice
 */
public class IdAuditEvent extends AuditEvent {

    @Serial
    private static final long serialVersionUID = 8633684304971875621L;

    public static final String PRINCIPAL_FIELD_NAME = "principal";

    public static final String TYPE_FIELD_NAME = "type";

    /**
     * 主键 id
     */
    private final String  id;

    public IdAuditEvent(AuditEvent auditEvent) {
        this(UUID.randomUUID().toString(), auditEvent.getTimestamp(), auditEvent.getPrincipal(), auditEvent.getType(), auditEvent.getData());
    }

    public IdAuditEvent(String principal, String type, Map<String, Object> data) {
        this(UUID.randomUUID().toString(), Instant.now(), principal, type, data);
    }

    public IdAuditEvent(String id, Instant timestamp, String principal, String type, Map<String, Object> data) {
        super(timestamp, principal, type, data);
        this.id = id;
    }

    /**
     * 获取主键 id
     *
     * @return 主键 id
     */
    public String getId() {
        return id;
    }

}
