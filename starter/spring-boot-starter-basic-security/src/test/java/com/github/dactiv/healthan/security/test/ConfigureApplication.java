package com.github.dactiv.healthan.security.test;

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

import java.util.ArrayList;
import java.util.List;

@EnableConfigurationProperties(SecurityProperties.class)
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ConfigureApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigureApplication.class, args);
    }

    @Bean
    public ElasticsearchAuditEventRepository elasticsearchAuditEventRepository(ElasticsearchOperations elasticsearchOperations,
                                                                               SecurityProperties securityProperties) {
        List<String> ignorePrincipals = new ArrayList<>(PluginAuditEventRepository.DEFAULT_IGNORE_PRINCIPALS);
        ignorePrincipals.add(securityProperties.getUser().getName());

        return new ElasticsearchAuditEventRepository(
                elasticsearchOperations,
                "ix_test_audit_event",
                ignorePrincipals
        );
    }

    @Bean
    public PluginAuditEventRepository auditEventRepository(MongoTemplate mongoTemplate,
                                                           SecurityProperties securityProperties) {

        List<String> ignorePrincipals = new ArrayList<>(PluginAuditEventRepository.DEFAULT_IGNORE_PRINCIPALS);
        ignorePrincipals.add(securityProperties.getUser().getName());

        return new MongoAuditEventRepository(
                mongoTemplate,
                "col_test_audit_event",
                ignorePrincipals
        );

    }
}
