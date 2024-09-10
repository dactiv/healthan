package com.github.dactiv.healthan.spring.security.entity;

import com.github.dactiv.healthan.spring.security.audit.config.AuditDetailsSource;

import java.io.Serializable;
import java.util.Map;

/**
 * 审计的认证明细
 *
 * @author maurice.chen
 */
public class AuditAuthenticationSuccessDetails implements Serializable, AuditDetailsSource {

    private final Object requestDetails;

    private final Map<String, Object> metadata;

    private boolean isRemember;

    public AuditAuthenticationSuccessDetails(Object requestDetails, Map<String, Object> metadata) {
        this.requestDetails = requestDetails;
        this.metadata = metadata;
    }

    public Object getRequestDetails() {
        return requestDetails;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public boolean isRemember() {
        return isRemember;
    }

    public void setRemember(boolean remember) {
        isRemember = remember;
    }

}
