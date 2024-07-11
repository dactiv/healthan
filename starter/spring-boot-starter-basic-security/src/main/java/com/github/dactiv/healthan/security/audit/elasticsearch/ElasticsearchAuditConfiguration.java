package com.github.dactiv.healthan.security.audit.elasticsearch;

import com.github.dactiv.healthan.security.AuditConfiguration;
import com.github.dactiv.healthan.security.AuditProperties;
import com.github.dactiv.healthan.security.audit.PluginAuditEventRepository;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

/**
 * Elasticsearch 审计仓库配置
 *
 * @author maurice.chen
 */
@ConditionalOnClass(ElasticsearchOperations.class)
@ConditionalOnMissingBean(AuditEventRepository.class)
@EnableConfigurationProperties(AuditProperties.class)
@Conditional(AuditConfiguration.AuditImportSelectorCondition.class)
@ConditionalOnProperty(prefix = "healthan.authentication.audit", value = "enabled", matchIfMissing = true)
public class ElasticsearchAuditConfiguration {

    @Bean
    public PluginAuditEventRepository auditEventRepository(ElasticsearchOperations elasticsearchOperations,
                                                           AuditProperties auditProperties) {

        return new ElasticsearchAuditEventRepository(
                auditProperties,
                elasticsearchOperations,
                ElasticsearchAuditEventRepository.DEFAULT_INDEX_NAME
        );

    }
}
