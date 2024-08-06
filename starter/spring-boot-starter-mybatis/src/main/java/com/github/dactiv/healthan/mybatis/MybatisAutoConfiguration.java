package com.github.dactiv.healthan.mybatis;


import com.github.dactiv.healthan.mybatis.interceptor.audit.OperationDataTraceInterceptor;
import com.github.dactiv.healthan.mybatis.interceptor.audit.OperationDataTraceResolver;
import com.github.dactiv.healthan.mybatis.interceptor.json.support.JacksonJsonCollectionPostInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
    @ConditionalOnBean(OperationDataTraceResolver.class)
    @ConditionalOnProperty(prefix = "healthan.mybatis.operation-data-trace", value = "enabled", matchIfMissing = true)
    public OperationDataTraceInterceptor operationDataTraceInterceptor(OperationDataTraceResolver operationDataTraceResolver) {
        return new OperationDataTraceInterceptor(operationDataTraceResolver);
    }
}
