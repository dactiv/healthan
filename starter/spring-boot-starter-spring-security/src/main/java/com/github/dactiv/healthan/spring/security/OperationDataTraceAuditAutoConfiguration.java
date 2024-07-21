package com.github.dactiv.healthan.spring.security;


import com.github.dactiv.healthan.mybatis.plus.MybatisPlusAutoConfiguration;
import com.github.dactiv.healthan.spring.security.audit.ElasticsearchOperationDataTraceRepository;
import com.github.dactiv.healthan.spring.security.audit.SecurityAuditEventRepositoryInterceptor;
import com.github.dactiv.healthan.spring.security.audit.config.ControllerAuditProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@Configuration
@AutoConfigureBefore(MybatisPlusAutoConfiguration.class)
@ConditionalOnProperty(prefix = "healthan.mybatis.operation-data-trace", value = "enabled", matchIfMissing = true)
public class OperationDataTraceAuditAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "healthan.security.audit", name = "type", havingValue = "elasticsearch")
    ElasticsearchOperationDataTraceRepository elasticsearchOperationDataTraceRepository(ElasticsearchOperations elasticsearchOperations,
                                                                                        ControllerAuditProperties controllerAuditProperties,
                                                                                        SecurityAuditEventRepositoryInterceptor interceptor) {

        return new ElasticsearchOperationDataTraceRepository(
                ElasticsearchOperationDataTraceRepository.DEFAULT_INDEX_NAME,
                interceptor,
                controllerAuditProperties,
                elasticsearchOperations
        );
    }
}
