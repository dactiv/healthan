package com.github.dactiv.healthan.spring.security;

import com.github.dactiv.healthan.mybatis.plus.MybatisPlusAutoConfiguration;
import com.github.dactiv.healthan.mybatis.plus.audit.MybatisPlusOperationDataTraceRepository;
import com.github.dactiv.healthan.mybatis.plus.config.OperationDataTraceProperties;
import com.github.dactiv.healthan.spring.security.audit.SecurityPrincipalOperationDataTraceRepository;
import com.github.dactiv.healthan.spring.security.audit.config.ControllerAuditProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfigureBefore(MybatisPlusAutoConfiguration.class)
@ConditionalOnMissingBean(MybatisPlusOperationDataTraceRepository.class)
@ConditionalOnProperty(prefix = "healthan.mybatis.operation-data-trace", value = "enabled", matchIfMissing = true)
public class SecurityOperationDataTraceRepositoryAutoConfiguration {

    @Bean
    public SecurityPrincipalOperationDataTraceRepository principalOperationDataTraceRepository(OperationDataTraceProperties operationDataTraceProperties,
                                                                                               ControllerAuditProperties controllerAuditProperties) {
        return new SecurityPrincipalOperationDataTraceRepository(operationDataTraceProperties, controllerAuditProperties);
    }
}

