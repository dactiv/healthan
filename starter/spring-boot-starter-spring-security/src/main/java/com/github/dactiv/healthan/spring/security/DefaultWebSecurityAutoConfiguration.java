package com.github.dactiv.healthan.spring.security;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.spring.security.authentication.*;
import com.github.dactiv.healthan.spring.security.authentication.adapter.WebSecurityConfigurerAfterAdapter;
import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.RequestAuthenticationConfigurer;
import com.github.dactiv.healthan.spring.security.plugin.PluginSourceTypeVoter;
import com.github.dactiv.healthan.spring.web.result.error.ErrorResultResolver;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.access.vote.ConsensusBased;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * spring security 配置实现
 *
 * @author maurice.chen
 */
@Configuration
@EnableConfigurationProperties(AuthenticationProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class DefaultWebSecurityAutoConfiguration {

    private final AccessTokenContextRepository accessTokenContextRepository;

    private final AuthenticationProperties authenticationProperties;

    private final List<WebSecurityConfigurerAfterAdapter> webSecurityConfigurerAfterAdapters;

    private final List<ErrorResultResolver> resultResolvers;

    private final List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers;

    private final List<TypeSecurityPrincipalService> typeSecurityPrincipalServices;

    private final AuthenticationFailureHandler authenticationFailureHandler;

    private final AuthenticationSuccessHandler authenticationSuccessHandler;

    private final SecurityContextRepository securityContextRepository;

    private final CacheManager cacheManager;

    public DefaultWebSecurityAutoConfiguration(AccessTokenContextRepository accessTokenContextRepository,
                                               AuthenticationProperties authenticationProperties,
                                               AuthenticationFailureHandler authenticationFailureHandler,
                                               AuthenticationSuccessHandler authenticationSuccessHandler,
                                               CacheManager cacheManager,
                                               SecurityContextRepository securityContextRepository,
                                               ObjectProvider<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers,
                                               ObjectProvider<ErrorResultResolver> errorResultResolvers,
                                               ObjectProvider<TypeSecurityPrincipalService> userDetailsServices,
                                               ObjectProvider<WebSecurityConfigurerAfterAdapter> webSecurityConfigurerAfterAdapter) {
        this.accessTokenContextRepository = accessTokenContextRepository;
        this.authenticationProperties = authenticationProperties;
        this.authenticationFailureHandler = authenticationFailureHandler;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.securityContextRepository = securityContextRepository;
        this.cacheManager = cacheManager;
        this.typeSecurityPrincipalServices = userDetailsServices.stream().collect(Collectors.toList());
        this.authenticationTypeTokenResolvers = authenticationTypeTokenResolvers.stream().collect(Collectors.toList());
        this.webSecurityConfigurerAfterAdapters = webSecurityConfigurerAfterAdapter.stream().collect(Collectors.toList());
        this.resultResolvers = errorResultResolvers.stream().collect(Collectors.toList());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests()
                .antMatchers(authenticationProperties.getPermitUriAntMatchers().toArray(new String[0]))
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .apply(
                        new RequestAuthenticationConfigurer<>(
                                authenticationProperties,
                                authenticationTypeTokenResolvers,
                                typeSecurityPrincipalServices,
                                cacheManager
                        )
                )
                .securityContextRepository(securityContextRepository)
                .failureHandler(authenticationFailureHandler)
                .successHandler(authenticationSuccessHandler)
                .and()
                .httpBasic()
                .disable()
                .formLogin()
                .disable()
                .logout()
                .disable()
                .rememberMe()
                .disable()
                .exceptionHandling()
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    RestResult<?> result = RestResult.of(accessDeniedException.getMessage(), HttpStatus.FORBIDDEN.value(), String.valueOf(HttpStatus.FORBIDDEN.value()));
                    response.getWriter().write(Casts.writeValueAsString(result));
                })
                .authenticationEntryPoint(new RestResultAuthenticationEntryPoint(resultResolvers))
                .and()
                .cors()
                .and()
                .csrf()
                .disable()
                .requestCache()
                .disable()
                .securityContext()
                .securityContextRepository(accessTokenContextRepository);

        if (CollectionUtils.isNotEmpty(webSecurityConfigurerAfterAdapters)) {
            for (WebSecurityConfigurerAfterAdapter a : webSecurityConfigurerAfterAdapters) {
                a.configure(httpSecurity);
            }
        }

        httpSecurity.addFilterBefore(new IpAuthenticationFilter(this.authenticationProperties), RequestAuthenticationFilter.class);

        addConsensusBasedToMethodSecurityInterceptor(httpSecurity, authenticationProperties);
        return httpSecurity.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            if (CollectionUtils.isNotEmpty(webSecurityConfigurerAfterAdapters)) {
                for (WebSecurityConfigurerAfterAdapter a : webSecurityConfigurerAfterAdapters) {
                    a.configure(web);
                }
            }
        };
    }

    /**
     * 添加 ConsensusBased 访问管理器到方法拦截器中
     *
     * @param http http security
     */
    public static void addConsensusBasedToMethodSecurityInterceptor(HttpSecurity http,
                                                                    AuthenticationProperties properties) {
        try {
            MethodSecurityInterceptor methodSecurityInterceptor = http
                    .getSharedObject(ApplicationContext.class)
                    .getBean(MethodSecurityInterceptor.class);

            AccessDecisionManager accessDecisionManager = methodSecurityInterceptor.getAccessDecisionManager();

            if (AbstractAccessDecisionManager.class.isAssignableFrom(accessDecisionManager.getClass())) {

                AbstractAccessDecisionManager adm = (AbstractAccessDecisionManager) accessDecisionManager;
                adm.getDecisionVoters().add(new PluginSourceTypeVoter());

                ConsensusBased consensusBased = new ConsensusBased(adm.getDecisionVoters());
                consensusBased.setAllowIfEqualGrantedDeniedDecisions(properties.isAllowIfEqualGrantedDeniedDecisions());

                methodSecurityInterceptor.setAccessDecisionManager(consensusBased);
            }
        } catch (Exception ignored) {

        }
    }

    /*@Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static Advisor pluginAuthorizationMethodInterceptor(ObjectProvider<SecurityContextHolderStrategy> strategyProvider,
                                                        ObjectProvider<AuthorizationEventPublisher> eventPublisherProvider) {

        AuthorizationManagerBeforeMethodInterceptor interceptor = new AuthorizationManagerBeforeMethodInterceptor(
                Pointcuts.union(new AnnotationMatchingPointcut(null, Plugin.class, true),
                        new AnnotationMatchingPointcut(Plugin.class, true)), new PluginSourceAuthorizationManager());
        interceptor.setOrder(AuthorizationInterceptorsOrder.PRE_AUTHORIZE.getOrder() + 1);

        strategyProvider.ifAvailable(interceptor::setSecurityContextHolderStrategy);
        eventPublisherProvider.ifAvailable(interceptor::setAuthorizationEventPublisher);

        return interceptor;
    }*/

}
