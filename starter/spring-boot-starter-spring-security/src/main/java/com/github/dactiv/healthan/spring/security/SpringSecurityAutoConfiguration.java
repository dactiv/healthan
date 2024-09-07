package com.github.dactiv.healthan.spring.security;

import com.github.dactiv.healthan.spring.security.audit.ControllerAuditHandlerInterceptor;
import com.github.dactiv.healthan.spring.security.audit.RequestBodyAttributeAdviceAdapter;
import com.github.dactiv.healthan.spring.security.audit.SecurityAuditEventRepositoryInterceptor;
import com.github.dactiv.healthan.spring.security.audit.config.ControllerAuditProperties;
import com.github.dactiv.healthan.spring.security.authentication.AccessTokenContextRepository;
import com.github.dactiv.healthan.spring.security.authentication.AuthenticationSuccessTokenTrustResolver;
import com.github.dactiv.healthan.spring.security.authentication.TypeSecurityPrincipalService;
import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.cache.support.InMemoryCacheManager;
import com.github.dactiv.healthan.spring.security.authentication.config.*;
import com.github.dactiv.healthan.spring.security.authentication.handler.JsonAuthenticationFailureHandler;
import com.github.dactiv.healthan.spring.security.authentication.handler.JsonAuthenticationFailureResponse;
import com.github.dactiv.healthan.spring.security.authentication.handler.JsonAuthenticationSuccessHandler;
import com.github.dactiv.healthan.spring.security.authentication.handler.JsonAuthenticationSuccessResponse;
import com.github.dactiv.healthan.spring.security.authentication.service.TypeSecurityPrincipalManager;
import com.github.dactiv.healthan.spring.security.controller.TokenController;
import com.github.dactiv.healthan.spring.security.plugin.PluginEndpoint;
import com.github.dactiv.healthan.spring.security.plugin.PluginSourceAuthorizationManager;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.websocket.servlet.UndertowWebSocketServletWebServerCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.stream.Collectors;

/**
 * spring security 重写支持自动配置类
 *
 * @author maurice
 */
@Configuration
@AutoConfigureAfter(RedissonAutoConfiguration.class)
@EnableConfigurationProperties({
        AuthenticationProperties.class,
        AccessTokenProperties.class,
        CaptchaVerificationProperties.class,
        ControllerAuditProperties.class,
        OAuth2Properties.class
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "healthan.authentication.spring.security", value = "enabled", matchIfMissing = true)
public class SpringSecurityAutoConfiguration {

    @Bean
    @ConfigurationProperties("healthan.authentication.plugin")
    public PluginEndpoint pluginEndpoint(ObjectProvider<InfoContributor> infoContributor) {
        return new PluginEndpoint(infoContributor.stream().collect(Collectors.toList()));
    }

    @Bean
    @ConditionalOnProperty(prefix = "healthan.security.audit", name = "enabled", havingValue = "true")
    public ControllerAuditHandlerInterceptor controllerAuditHandlerInterceptor(ControllerAuditProperties controllerAuditProperties) {
        return new ControllerAuditHandlerInterceptor(controllerAuditProperties);
    }

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnProperty(prefix = "healthan.authentication.access-token", value = "enable-controller", havingValue = "true")
    public TokenController accessTokenController(CacheManager cacheManager,
                                          AccessTokenProperties accessTokenProperties) {
        return new TokenController(cacheManager, accessTokenProperties);
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager inMemoryCacheManager() {
        return new InMemoryCacheManager();
    }

    @Bean
    @ConditionalOnMissingBean(AccessTokenContextRepository.class)
    public AccessTokenContextRepository accessTokenContextRepository(AuthenticationProperties properties,
                                                                     AccessTokenProperties accessTokenProperties,
                                                                     CacheManager cacheManager) {

        return new AccessTokenContextRepository(
                cacheManager,
                accessTokenProperties,
                properties
        );
    }

    @Bean
    @ConditionalOnMissingBean(JsonAuthenticationFailureHandler.class)
    public JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler(ObjectProvider<JsonAuthenticationFailureResponse> failureResponse,
                                                                             AuthenticationProperties authenticationProperties) {
        return new JsonAuthenticationFailureHandler(
                failureResponse.orderedStream().collect(Collectors.toList()),
                authenticationProperties
        );
    }

    @Bean
    @ConditionalOnMissingBean(JsonAuthenticationSuccessHandler.class)
    public JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler(ObjectProvider<JsonAuthenticationSuccessResponse> successResponse,
                                                                             AuthenticationProperties properties) {
        return new JsonAuthenticationSuccessHandler(
                successResponse.orderedStream().collect(Collectors.toList()),
                properties
        );
    }

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "healthan.security.audit", name = "enabled", havingValue = "true")
    public static class DefaultWebMvcConfigurer extends UndertowWebSocketServletWebServerCustomizer implements WebMvcConfigurer {

        private final ControllerAuditHandlerInterceptor controllerAuditHandlerInterceptor;

        public DefaultWebMvcConfigurer(ControllerAuditHandlerInterceptor controllerAuditHandlerInterceptor) {
            this.controllerAuditHandlerInterceptor = controllerAuditHandlerInterceptor;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(controllerAuditHandlerInterceptor);
        }
    }

    @Bean
    @ConditionalOnMissingBean(RequestBodyAttributeAdviceAdapter.class)
    public RequestBodyAttributeAdviceAdapter requestBodyAttributeAdviceAdapter() {
        return new RequestBodyAttributeAdviceAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(SecurityAuditEventRepositoryInterceptor.class)
    public SecurityAuditEventRepositoryInterceptor securityAuditEventRepositoryInterceptor(AuthenticationProperties authenticationProperties,
                                                                                           RememberMeProperties rememberMeProperties) {
        return new SecurityAuditEventRepositoryInterceptor(authenticationProperties, rememberMeProperties);
    }

    @Bean
    @ConditionalOnMissingBean(TypeSecurityPrincipalManager.class)
    public TypeSecurityPrincipalManager typeSecurityPrincipalManager(CacheManager cacheManager,
                                                                     ObjectProvider<TypeSecurityPrincipalService> typeSecurityPrincipalServices) {

        return new TypeSecurityPrincipalManager(
                typeSecurityPrincipalServices.stream().collect(Collectors.toList()),
                cacheManager
        );

    }

    @Bean
    public PluginSourceAuthorizationManager pluginSourceAuthorizationManager(AuthenticationProperties authenticationProperties) {
        return new PluginSourceAuthorizationManager(authenticationProperties);
    }

    /*@Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public MethodInterceptor pluginAuthorizationMethodInterceptor(ObjectProvider<SecurityContextHolderStrategy> strategyProvider,
                                                                  PluginSourceAuthorizationManager pluginSourceAuthorizationManager,
                                                                  ObjectProvider<AuthorizationEventPublisher> eventPublisherProvider) {

        Pointcut pointcut = new ComposablePointcut(
                new AnnotationMatchingPointcut(null, Plugin.class, true)
        );

        AuthorizationManagerBeforeMethodInterceptor interceptor = new AuthorizationManagerBeforeMethodInterceptor(
                pointcut,
                pluginSourceAuthorizationManager
        );

        interceptor.setOrder(AuthorizationInterceptorsOrder.PRE_AUTHORIZE.getOrder() - BigDecimal.TEN.intValue());
        strategyProvider.ifAvailable(interceptor::setSecurityContextHolderStrategy);
        eventPublisherProvider.ifAvailable(interceptor::setAuthorizationEventPublisher);

        return interceptor;
    }*/

    @Bean
    public DefaultMethodSecurityExpressionHandler authenticationSuccessTokenTrustResolverExpressionHandler(ObjectProvider<GrantedAuthorityDefaults> defaultsProvider,
                                                                                                           ApplicationContext context) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setTrustResolver(new AuthenticationSuccessTokenTrustResolver());
        defaultsProvider.ifAvailable((d) -> handler.setDefaultRolePrefix(d.getRolePrefix()));
        handler.setApplicationContext(context);
        return handler;
    }
}
