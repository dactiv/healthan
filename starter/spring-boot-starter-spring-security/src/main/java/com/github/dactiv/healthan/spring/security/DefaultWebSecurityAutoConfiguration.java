package com.github.dactiv.healthan.spring.security;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.spring.security.authentication.*;
import com.github.dactiv.healthan.spring.security.authentication.adapter.WebSecurityConfigurerAfterAdapter;
import com.github.dactiv.healthan.spring.security.authentication.cache.CacheManager;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.RememberMeProperties;
import com.github.dactiv.healthan.spring.security.authentication.provider.TypeRememberMeAuthenticationProvider;
import com.github.dactiv.healthan.spring.security.plugin.PluginSourceTypeVoter;
import com.github.dactiv.healthan.spring.web.result.error.ErrorResultResolver;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * spring security 配置实现
 *
 * @author maurice.chen
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties({AuthenticationProperties.class, RememberMeProperties.class})
public class DefaultWebSecurityAutoConfiguration extends GlobalMethodSecurityConfiguration {

    private final AccessTokenContextRepository accessTokenContextRepository;

    private final AuthenticationProperties authenticationProperties;

    private final RememberMeProperties rememberMeProperties;

    private final List<WebSecurityConfigurerAfterAdapter> webSecurityConfigurerAfterAdapters;

    private final List<ErrorResultResolver> resultResolvers;

    private final List<TypeSecurityPrincipalService> typeSecurityPrincipalServices;

    private final CacheManager cacheManager;

    public DefaultWebSecurityAutoConfiguration(AccessTokenContextRepository accessTokenContextRepository,
                                               AuthenticationProperties authenticationProperties,
                                               RememberMeProperties rememberMeProperties,
                                               CacheManager cacheManager,
                                               ObjectProvider<ErrorResultResolver> errorResultResolvers,
                                               ObjectProvider<TypeSecurityPrincipalService> userDetailsServices,
                                               ObjectProvider<WebSecurityConfigurerAfterAdapter> webSecurityConfigurerAfterAdapter) {
        this.accessTokenContextRepository = accessTokenContextRepository;
        this.authenticationProperties = authenticationProperties;
        this.rememberMeProperties = rememberMeProperties;
        this.cacheManager = cacheManager;
        this.typeSecurityPrincipalServices = userDetailsServices.stream().collect(Collectors.toList());
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

        if (rememberMeProperties.isEnabled()) {
            PersistentTokenRepository tokenRepository = httpSecurity.getSharedObject(PersistentTokenRepository.class);
            httpSecurity
                    .rememberMe()
                    .userDetailsService(new RememberMeUserDetailsService())
                    .alwaysRemember(rememberMeProperties.isAlways())
                    .rememberMeCookieName(rememberMeProperties.getCookieName())
                    .tokenRepository(Objects.isNull(tokenRepository) ? new InMemoryTokenRepositoryImpl() : tokenRepository)
                    .tokenValiditySeconds(rememberMeProperties.getTokenValiditySeconds())
                    .rememberMeCookieDomain(rememberMeProperties.getDomain())
                    .rememberMeParameter(rememberMeProperties.getParamName())
                    .useSecureCookie(rememberMeProperties.isUseSecureCookie())
                    .key(rememberMeProperties.getKey());

            AuthenticationProvider authenticationProvider = new TypeRememberMeAuthenticationProvider(
                    rememberMeProperties.getKey(),
                    authenticationProperties,
                    cacheManager,
                    typeSecurityPrincipalServices
            );
            httpSecurity.authenticationProvider(authenticationProvider);
        }

        if (CollectionUtils.isNotEmpty(webSecurityConfigurerAfterAdapters)) {
            for (WebSecurityConfigurerAfterAdapter a : webSecurityConfigurerAfterAdapters) {
                a.configure(httpSecurity);
            }
        }

        httpSecurity.addFilterBefore(new IpAuthenticationFilter(this.authenticationProperties), UsernamePasswordAuthenticationFilter.class);

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

    @Bean
    @ConditionalOnMissingBean(AuthenticationTrustResolver.class)
    public AuthenticationTrustResolver authenticationTrustResolver() {
        return new SimpleAuthenticationTrustResolver();
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
