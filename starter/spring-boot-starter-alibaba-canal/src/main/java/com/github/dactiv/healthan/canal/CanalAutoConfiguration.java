package com.github.dactiv.healthan.canal;

import com.github.dactiv.healthan.canal.config.CanalAdminProperties;
import com.github.dactiv.healthan.canal.config.CanalProperties;
import com.github.dactiv.healthan.canal.resolver.CanalRowDataChangeNoticeResolver;
import com.github.dactiv.healthan.canal.resolver.CanalRowDataChangeResolver;
import com.github.dactiv.healthan.canal.resolver.support.HttpCanalRowDataChangeNoticeResolver;
import com.github.dactiv.healthan.canal.service.CanalRowDataChangeNoticeService;
import com.github.dactiv.healthan.canal.service.support.InMemoryCanalRowDataChangeNoticeService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * canal 自动配置类
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties({CanalProperties.class, CanalAdminProperties.class})
@ConditionalOnProperty(prefix = "healthan.canal", value = "enabled", matchIfMissing = true)
public class CanalAutoConfiguration {

    @Bean
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
    @ConditionalOnBean(CanalInstanceManager.class)
    public CanalInstanceManager canalInstanceManager(ObjectProvider<CanalRowDataChangeResolver> canalRowDataChangeResolvers,
                                                     ThreadPoolExecutor buildExecutor,
                                                     CanalProperties canalProperties) {
        return new CanalInstanceManager(
                canalRowDataChangeResolvers.stream().collect(Collectors.toList()),
                buildExecutor, canalProperties
        );
    }

    @Bean
    @ConditionalOnBean(CanalRowDataChangeNoticeService.class)
    public CanalRowDataChangeNoticeService canalRowDataChangeNoticeService(ObjectProvider<CanalRowDataChangeNoticeResolver> canalRowDataChangeNoticeResolvers) {
        return new InMemoryCanalRowDataChangeNoticeService(canalRowDataChangeNoticeResolvers.stream().collect(Collectors.toList()));
    }

    @Bean
    @ConditionalOnBean(HttpCanalRowDataChangeNoticeResolver.class)
    public HttpCanalRowDataChangeNoticeResolver httpCanalRowDataChangeNoticeResolver(RestTemplate restTemplate) {
        return new HttpCanalRowDataChangeNoticeResolver(restTemplate);
    }
}
