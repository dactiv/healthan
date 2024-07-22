package com.github.dactiv.healthan.mybatis.plus.audit;

import com.github.dactiv.healthan.mybatis.interceptor.audit.OperationDataTraceRecord;

import java.util.Date;

/**
 * 带实体 id 的操作数据留痕记录
 *
 * @author maurice.chen
 */
public class EntityIdOperationDataTraceRecord extends OperationDataTraceRecord {
    
    private static final long serialVersionUID = 7972904701408013089L;

    /**
     * 实体 id
     */
    private Object entityId;

    public EntityIdOperationDataTraceRecord() {
        super();
    }

    public EntityIdOperationDataTraceRecord(Date creationTime) {
        super(creationTime);
    }

    /**
     * 获取实体 id
     *
     * @return 实体 id
     */
    public Object getEntityId() {
        return entityId;
    }

    /**
     * 设置实体 id
     *
     * @param entityId 实体 id
     */
    public void setEntityId(Object entityId) {
        this.entityId = entityId;
    }
}
