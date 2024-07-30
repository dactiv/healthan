package com.github.dactiv.healthan.security.audit;

import org.springframework.boot.actuate.audit.AuditEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 审计事件实体类
 *
 * @author maurice
 */
public class IdAuditEvent extends AuditEvent {

    
    private static final long serialVersionUID = 8633684304971875621L;

    public static final String PRINCIPAL_FIELD_NAME = "principal";

    public static final String TYPE_FIELD_NAME = "type";

    /**
     * 主键 id
     */
    private String id;

    public IdAuditEvent(AuditEvent auditEvent) {
        super(auditEvent.getTimestamp(), auditEvent.getPrincipal(), auditEvent.getType(), auditEvent.getData());
        this.id = UUID.randomUUID().toString();
    }

    public IdAuditEvent(String principal, String type, Map<String, Object> data) {
        super(Instant.now(), principal, type, data);
        this.id = UUID.randomUUID().toString();
    }

    public IdAuditEvent(Instant timestamp, String principal, String type, Map<String, Object> data) {
        super(timestamp, principal, type, data);
        this.id = UUID.randomUUID().toString();
    }

    /**
     * 获取主键 id
     *
     * @return 主键 id
     */
    public String getId() {
        return id;
    }

    /**
     * 设置主键 id
     *
     * @param id 主键 id
     */
    public void setId(String id) {
        this.id = id;
    }

}
