package com.github.dactiv.healthan.security.audit.memory;

import com.github.dactiv.healthan.security.AuditConfiguration;
import com.github.dactiv.healthan.security.AuditProperties;
import com.github.dactiv.healthan.security.audit.AuditEventRepositoryInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import java.util.stream.Collectors;

/**
 * Elasticsearch 审计仓库配置
 *
 * @author maurice.chen
 */
@ConditionalOnMissingBean(AuditEventRepository.class)
@EnableConfigurationProperties(AuditProperties.class)
@Conditional(AuditConfiguration.AuditImportSelectorCondition.class)
@ConditionalOnProperty(prefix = "healthan.authentication.audit", value = "enabled", matchIfMissing = true)
public class CustomInMemoryAuditConfiguration {

    @Bean
    public AuditEventRepository auditEventRepository(ObjectProvider<AuditEventRepositoryInterceptor> interceptors) {
        return new CustomInMemoryAuditEventRepository(
                1000,
                interceptors.stream().collect(Collectors.toList())
        );
    }
}
