package com.github.dactiv.healthan.security.audit;

import org.springframework.boot.actuate.audit.AuditEvent;

import java.util.List;

public abstract class AbstractExtendAuditEventRepository implements ExtendAuditEventRepository {

    private final List<AuditEventRepositoryInterceptor> interceptors;

    public AbstractExtendAuditEventRepository(List<AuditEventRepositoryInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public void add(AuditEvent event) {

        for (AuditEventRepositoryInterceptor interceptor : interceptors) {
            if (!interceptor.preAddHandle(event)) {
                return ;
            }
        }

        doAdd(event);

        interceptors.forEach(i -> i.postAddHandle(event));
    }

    protected abstract void doAdd(AuditEvent event);

}
