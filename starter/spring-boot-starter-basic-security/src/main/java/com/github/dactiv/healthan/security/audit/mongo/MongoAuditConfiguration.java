package com.github.dactiv.healthan.security.audit.mongo;

import com.github.dactiv.healthan.security.AuditConfiguration;
import com.github.dactiv.healthan.security.AuditProperties;
import com.github.dactiv.healthan.security.audit.AuditEventRepositoryInterceptor;
import com.github.dactiv.healthan.security.audit.PluginAuditEventRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.stream.Collectors;

/**
 * Mongo 审计仓库配置
 *
 * @author maurice.chen
 */
@ConditionalOnClass(MongoTemplate.class)
@ConditionalOnMissingBean(AuditEventRepository.class)
@EnableConfigurationProperties(AuditProperties.class)
@Conditional(AuditConfiguration.AuditImportSelectorCondition.class)
@ConditionalOnProperty(prefix = "healthan.authentication.audit", value = "enabled", matchIfMissing = true)
public class MongoAuditConfiguration {

    @Bean
    public PluginAuditEventRepository auditEventRepository(MongoTemplate mongoTemplate,
                                                           ObjectProvider<AuditEventRepositoryInterceptor> interceptors) {

        return new MongoAuditEventRepository(
                interceptors.stream().collect(Collectors.toList()),
                mongoTemplate,
                MongoAuditEventRepository.DEFAULT_COLLECTION_NAME
        );

    }
}