package com.github.dactiv.healthan.security.test;

import com.github.dactiv.healthan.security.AuditProperties;
import com.github.dactiv.healthan.security.audit.PluginAuditEventRepository;
import com.github.dactiv.healthan.security.audit.elasticsearch.ElasticsearchAuditEventRepository;
import com.github.dactiv.healthan.security.audit.mongo.MongoAuditEventRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

@EnableConfigurationProperties(SecurityProperties.class)
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ConfigureApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigureApplication.class, args);
    }

    @Bean
    public ElasticsearchAuditEventRepository elasticsearchAuditEventRepository(ElasticsearchOperations elasticsearchOperations,
                                                                               AuditProperties auditProperties) {

        return new ElasticsearchAuditEventRepository(
                auditProperties,
                elasticsearchOperations,
                "ix_test_audit_event"
        );
    }

    @Bean
    public PluginAuditEventRepository auditEventRepository(MongoTemplate mongoTemplate,
                                                           AuditProperties auditProperties) {

        return new MongoAuditEventRepository(
                auditProperties,
                mongoTemplate,
                "col_test_audit_event"
        );

    }
}
