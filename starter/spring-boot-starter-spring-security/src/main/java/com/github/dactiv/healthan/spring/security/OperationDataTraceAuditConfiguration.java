package com.github.dactiv.healthan.spring.security;


import com.github.dactiv.healthan.mybatis.plus.MybatisPlusAutoConfiguration;
import com.github.dactiv.healthan.security.audit.PluginAuditEventRepository;
import com.github.dactiv.healthan.spring.security.audit.ElasticsearchOperationDataTraceRepository;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.util.ArrayList;
import java.util.List;

@Configuration
@AutoConfigureBefore(MybatisPlusAutoConfiguration.class)
@ConditionalOnProperty(prefix = "healthan.mybatis.operation-data-trace", value = "enabled", matchIfMissing = true)
public class OperationDataTraceAuditConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "healthan.authentication.audit", name = "type", havingValue = "elasticsearch")
    ElasticsearchOperationDataTraceRepository elasticsearchOperationDataTraceRepository(ElasticsearchOperations elasticsearchOperations,
                                                                                        SecurityProperties securityProperties) {
        List<String> ignorePrincipals = new ArrayList<>(PluginAuditEventRepository.DEFAULT_IGNORE_PRINCIPALS);
        ignorePrincipals.add(securityProperties.getUser().getName());

        return new ElasticsearchOperationDataTraceRepository(
                ignorePrincipals,
                ElasticsearchOperationDataTraceRepository.DEFAULT_INDEX_NAME,
                elasticsearchOperations
        );
    }
}
