package com.github.dactiv.healthan.spring.security;

import com.github.dactiv.healthan.mybatis.plus.MybatisPlusAutoConfiguration;
import com.github.dactiv.healthan.mybatis.plus.audit.MybatisPlusOperationDataTraceResolver;
import com.github.dactiv.healthan.mybatis.plus.config.OperationDataTraceProperties;
import com.github.dactiv.healthan.spring.security.audit.SecurityPrincipalOperationDataTraceResolver;
import com.github.dactiv.healthan.spring.security.audit.config.ControllerAuditProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfigureBefore(MybatisPlusAutoConfiguration.class)
@ConditionalOnMissingBean(MybatisPlusOperationDataTraceResolver.class)
@ConditionalOnProperty(prefix = "healthan.mybatis.operation-data-trace", value = "enabled", matchIfMissing = true)
public class SecurityOperationDataTraceRepositoryAutoConfiguration {

    @Bean
    public SecurityPrincipalOperationDataTraceResolver principalOperationDataTraceRepository(OperationDataTraceProperties operationDataTraceProperties,
                                                                                             ControllerAuditProperties controllerAuditProperties) {
        return new SecurityPrincipalOperationDataTraceResolver(operationDataTraceProperties, controllerAuditProperties);
    }
}

