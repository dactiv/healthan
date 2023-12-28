package com.github.dactiv.healthan.mybatis;


import com.github.dactiv.healthan.mybatis.interceptor.audit.OperationDataTraceInterceptor;
import com.github.dactiv.healthan.mybatis.interceptor.audit.OperationDataTraceRepository;
import com.github.dactiv.healthan.mybatis.interceptor.audit.support.InMemoryOperationDataTraceRepository;
import com.github.dactiv.healthan.mybatis.interceptor.json.support.JacksonJsonCollectionPostInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis 自动配置实现
 *
 * @author maurice.chen
 */
@Configuration
@ConditionalOnProperty(prefix = "healthan.mybatis", value = "enabled", matchIfMissing = true)
public class MybatisAutoConfiguration {

    @Bean
    public JacksonJsonCollectionPostInterceptor jacksonCollectionPostInterceptor(){
        return new JacksonJsonCollectionPostInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean(OperationDataTraceRepository.class)
    @ConditionalOnProperty(prefix = "healthan.mybatis.operation-data-trace", value = "enabled", matchIfMissing = true)
    public InMemoryOperationDataTraceRepository inMemoryOperationDataTraceRepository() {
        return new InMemoryOperationDataTraceRepository();
    }

    @Bean
    @ConditionalOnProperty(prefix = "healthan.mybatis.operation-data-trace", value = "enabled", matchIfMissing = true)
    public OperationDataTraceInterceptor operationDataTraceInterceptor(OperationDataTraceRepository operationDataTraceRepository) {
        return new OperationDataTraceInterceptor(operationDataTraceRepository);
    }
}
