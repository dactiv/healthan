package com.github.dactiv.healthan.spring.security.entity;

import java.io.Serializable;
import java.util.Map;

public class AuthenticationSuccessDetails implements Serializable {

    private final Object requestDetails;

    private final Map<String, Object> metadata;

    private boolean isRemember;

    public AuthenticationSuccessDetails(Object requestDetails, Map<String, Object> metadata) {
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
