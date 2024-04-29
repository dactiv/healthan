package com.github.dactiv.healthan.canal;

import com.github.dactiv.healthan.canal.config.CanalAdminProperties;
import com.github.dactiv.healthan.canal.config.CanalNoticeProperties;
import com.github.dactiv.healthan.canal.config.CanalProperties;
import com.github.dactiv.healthan.canal.endpoint.NotifiableTableEndpoint;
import com.github.dactiv.healthan.canal.resolver.CanalRowDataChangeNoticeResolver;
import com.github.dactiv.healthan.canal.resolver.CanalRowDataChangeResolver;
import com.github.dactiv.healthan.canal.resolver.support.HttpCanalRowDataChangeNoticeResolver;
import com.github.dactiv.healthan.canal.resolver.support.SimpleCanalRowDataChangeResolver;
import com.github.dactiv.healthan.canal.service.CanalRowDataChangeNoticeService;
import com.github.dactiv.healthan.canal.service.support.InMemoryCanalRowDataChangeNoticeService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.stream.Collectors;

/**
 * canal 自动配置类
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties({CanalProperties.class, CanalAdminProperties.class, CanalNoticeProperties.class})
@ConditionalOnProperty(prefix = "healthan.canal", value = "enabled", matchIfMissing = true)
public class CanalAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CanalInstanceManager.class)
    @ConditionalOnProperty(prefix = "healthan.canal.admin", value = "enabled", matchIfMissing = true)
    public CanalAdminService canalAdminService(CanalAdminProperties canalAdminProperties,
                                               RestTemplate restTemplate,
                                               CanalInstanceManager canalInstanceManager,
                                               RedissonClient redissonClient) {
        return new CanalAdminService(
                canalAdminProperties,
                restTemplate,
                redissonClient,
                canalInstanceManager
        );
    }

    @Bean
    @ConditionalOnMissingBean(CanalInstanceManager.class)
    public CanalInstanceManager canalInstanceManager(ObjectProvider<CanalRowDataChangeResolver> canalRowDataChangeResolvers,
                                                     CanalProperties canalProperties) {
        return new CanalInstanceManager(
                canalRowDataChangeResolvers.stream().collect(Collectors.toList()),
                canalProperties
        );
    }

    @Bean
    @ConditionalOnMissingBean(HttpCanalRowDataChangeNoticeResolver.class)
    public HttpCanalRowDataChangeNoticeResolver httpCanalRowDataChangeNoticeResolver(RestTemplate restTemplate) {
        return new HttpCanalRowDataChangeNoticeResolver(restTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(CanalRowDataChangeNoticeService.class)
    public CanalRowDataChangeNoticeService InMemoryCanalRowDataChangeNoticeService(ObjectProvider<CanalRowDataChangeNoticeResolver> canalRowDataChangeNoticeResolvers) {
        return new InMemoryCanalRowDataChangeNoticeService(canalRowDataChangeNoticeResolvers.stream().collect(Collectors.toList()));
    }

    @Bean
    public CanalRowDataChangeResolver simpleCanalRowDataChangeResolver(CanalRowDataChangeNoticeService canalRowDataChangeNoticeService) {
        return new SimpleCanalRowDataChangeResolver(canalRowDataChangeNoticeService);
    }

    @Bean
    @ConditionalOnProperty(prefix = "healthan.canal.notice", value = "enabled", matchIfMissing = true)
    public NotifiableTableEndpoint notifiableTableEndpoint(ObjectProvider<InfoContributor> infoContributors,
                                                           DataSource dataSource,
                                                           CanalNoticeProperties noticeProperties) {
        return new NotifiableTableEndpoint(infoContributors.stream().collect(Collectors.toList()), noticeProperties, dataSource);
    }
}
