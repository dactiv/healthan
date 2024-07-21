package com.github.dactiv.healthan.spring.security.entity;

import com.github.dactiv.healthan.mybatis.plus.audit.EntityIdOperationDataTraceRecord;

import java.util.Date;

/**
 * 用户明细操作数据留痕记录
 *
 * @author maurice.chen
 */
public class UserDetailsOperationDataTraceRecord extends EntityIdOperationDataTraceRecord {

    
    private static final long serialVersionUID = 3211452737634539720L;

    public static final String AUDIT_TYPE_FIELD_NAME = "auditType";

    /**
     * 关联业务 id
     */
    private String traceId;

    /**
     * 目标类型
     */
    private String controllerAuditType;

    public UserDetailsOperationDataTraceRecord() {
    }

    public UserDetailsOperationDataTraceRecord(String id, Date creationTime) {
        super(id, creationTime);
    }


    /**
     * 获取关联业务 id
     *
     * @return 关联业务 id
     */
    public String getTraceId() {
        return traceId;
    }

    /**
     * 设置关联业务 id
     *
     * @param traceId 关联业务 id
     */
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    /**
     * 获取审计类型
     *
     * @return 审计类型
     */
    public String getControllerAuditType() {
        return controllerAuditType;
    }

    /**
     * 设置审计类型
     *
     * @param controllerAuditType 审计类型
     */
    public void setControllerAuditType(String controllerAuditType) {
        this.controllerAuditType = controllerAuditType;
    }
}
