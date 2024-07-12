package com.github.dactiv.healthan.security.audit.memory;

import com.github.dactiv.healthan.security.audit.AuditEventRepositoryInterceptor;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;

import java.util.List;

public class CustomInMemoryAuditEventRepository extends InMemoryAuditEventRepository {

    private final List<AuditEventRepositoryInterceptor> interceptors;

    public CustomInMemoryAuditEventRepository(int capacity, List<AuditEventRepositoryInterceptor> interceptors) {
        super(capacity);
        this.interceptors = interceptors;
    }

    @Override
    public void add(AuditEvent event) {
        for (AuditEventRepositoryInterceptor interceptor : interceptors) {
            if (interceptor.preAddHandle(event)) {
                return ;
            }
        }

        super.add(event);

        interceptors.forEach(i -> i.postAddHandle(event));
    }
}
