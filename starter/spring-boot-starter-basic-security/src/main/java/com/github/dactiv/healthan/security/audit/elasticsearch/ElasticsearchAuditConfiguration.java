package com.github.dactiv.healthan.security.audit.elasticsearch;

import com.github.dactiv.healthan.security.AuditConfiguration;
import com.github.dactiv.healthan.security.AuditIndexProperties;
import com.github.dactiv.healthan.security.KafkaProperties;
import com.github.dactiv.healthan.security.audit.AuditEventRepositoryInterceptor;
import com.github.dactiv.healthan.security.audit.ExtendAuditEventRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;

import java.util.stream.Collectors;

/**
 * Elasticsearch 审计仓库配置
 *
 * @author maurice.chen
 */
@ConditionalOnClass(ElasticsearchOperations.class)
@ConditionalOnMissingBean(AuditEventRepository.class)
@EnableConfigurationProperties({AuditIndexProperties.class, KafkaProperties.class})
@Conditional(AuditConfiguration.AuditImportSelectorCondition.class)
@ConditionalOnProperty(prefix = "healthan.security.audit", value = "enabled", matchIfMissing = true)
public class ElasticsearchAuditConfiguration {

    @Bean
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnMissingBean(ExtendAuditEventRepository.class)
    @ConditionalOnProperty(prefix = "healthan.security.audit.index.kafka", value = "enabled", matchIfMissing = true)
    public ExtendAuditEventRepository kafkaAuditEventRepository(ElasticsearchOperations elasticsearchOperations,
                                                                AuditIndexProperties auditIndexProperties,
                                                                KafkaProperties kafkaProperties,
                                                                ObjectProvider<AuditEventRepositoryInterceptor> interceptors,
                                                                KafkaTemplate<String, Object> kafkaTemplate) {

        Assert.isTrue(StringUtils.isNotEmpty(kafkaProperties.getTopic()), "healthan.security.audit.index.kafka.topic 不能为空");

        return new KafkaElasticsearchAuditEventRepository(
                interceptors.stream().collect(Collectors.toList()),
                elasticsearchOperations,
                auditIndexProperties,
                kafkaTemplate,
                kafkaProperties
        );
    }

    @Bean
    @ConditionalOnMissingBean(ExtendAuditEventRepository.class)
    public ExtendAuditEventRepository auditEventRepository(ElasticsearchOperations elasticsearchOperations,
                                                           AuditIndexProperties auditIndexProperties,
                                                           ObjectProvider<AuditEventRepositoryInterceptor> interceptors) {

        return new ElasticsearchAuditEventRepository(
                interceptors.stream().collect(Collectors.toList()),
                elasticsearchOperations,
                auditIndexProperties
        );

    }
}
