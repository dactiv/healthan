package com.github.dactiv.healthan.spring.security;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.security.entity.RoleAuthority;
import com.github.dactiv.healthan.security.plugin.Plugin;
import com.github.dactiv.healthan.spring.security.authentication.*;
import com.github.dactiv.healthan.spring.security.authentication.adapter.WebSecurityConfigurerAfterAdapter;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.RememberMeProperties;
import com.github.dactiv.healthan.spring.security.authentication.provider.TypeRememberMeAuthenticationProvider;
import com.github.dactiv.healthan.spring.security.authentication.service.PersistentTokenRememberMeUserDetailsService;
import com.github.dactiv.healthan.spring.security.authentication.service.TypeSecurityPrincipalManager;
import com.github.dactiv.healthan.spring.security.authentication.service.TypeTokenBasedRememberMeServices;
import com.github.dactiv.healthan.spring.security.plugin.PluginSourceAuthorizationManager;
import com.github.dactiv.healthan.spring.web.result.error.ErrorResultResolver;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.Pointcuts;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.method.AuthorizationInterceptorsOrder;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * spring security 配置实现
 *
 * @author maurice.chen
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties({AuthenticationProperties.class, RememberMeProperties.class})
public class DefaultWebSecurityAutoConfiguration {

    private final AccessTokenContextRepository accessTokenContextRepository;

    private final AuthenticationProperties authenticationProperties;

    private final RememberMeProperties rememberMeProperties;

    private final List<WebSecurityConfigurerAfterAdapter> webSecurityConfigurerAfterAdapters;

    private final List<ErrorResultResolver> resultResolvers;

    private final TypeSecurityPrincipalManager typeSecurityPrincipalManager;

    public DefaultWebSecurityAutoConfiguration(AccessTokenContextRepository accessTokenContextRepository,
                                               AuthenticationProperties authenticationProperties,
                                               RememberMeProperties rememberMeProperties,
                                               TypeSecurityPrincipalManager typeSecurityPrincipalManager,
                                               ObjectProvider<ErrorResultResolver> errorResultResolvers,
                                               ObjectProvider<WebSecurityConfigurerAfterAdapter> webSecurityConfigurerAfterAdapter) {
        this.accessTokenContextRepository = accessTokenContextRepository;
        this.authenticationProperties = authenticationProperties;
        this.rememberMeProperties = rememberMeProperties;
        this.typeSecurityPrincipalManager = typeSecurityPrincipalManager;
        this.webSecurityConfigurerAfterAdapters = webSecurityConfigurerAfterAdapter.stream().collect(Collectors.toList());
        this.resultResolvers = errorResultResolvers.stream().collect(Collectors.toList());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests(a -> a
                        .requestMatchers(authenticationProperties.getPermitUriAntMatchers().toArray(new String[0]))
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .httpBasic(b -> b.init(httpSecurity))
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)
                .exceptionHandling(c -> c
                        .accessDeniedHandler(this.forbiddenAccessDeniedHandler())
                        .authenticationEntryPoint(new RestResultAuthenticationEntryPoint(resultResolvers))
                )
                .cors(c -> c.configure(httpSecurity))
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .securityContext(s -> s.securityContextRepository(accessTokenContextRepository));

        if (rememberMeProperties.isEnabled()) {
            createRememberMeSetting(httpSecurity);
        }

        if (CollectionUtils.isNotEmpty(webSecurityConfigurerAfterAdapters)) {
            for (WebSecurityConfigurerAfterAdapter a : webSecurityConfigurerAfterAdapters) {
                a.configure(httpSecurity);
            }
        }

        httpSecurity.addFilterBefore(new IpAuthenticationFilter(this.authenticationProperties), UsernamePasswordAuthenticationFilter.class);

        SecurityFilterChain securityFilterChain = httpSecurity.build();

        RememberMeServices rememberMeServices = httpSecurity.getSharedObject(RememberMeServices.class);
        if (Objects.nonNull(rememberMeServices) && AbstractRememberMeServices.class.isAssignableFrom(rememberMeServices.getClass())) {
            AbstractRememberMeServices abstractRememberMeServices = Casts.cast(rememberMeServices);
            abstractRememberMeServices.setAuthenticationDetailsSource(new RememberMeAuthenticationDetailsSource());
        }

        return securityFilterChain;
    }

    private AccessDeniedHandler forbiddenAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            RestResult<?> result = RestResult.of(accessDeniedException.getMessage(), HttpStatus.FORBIDDEN.value(), String.valueOf(HttpStatus.FORBIDDEN.value()));
            response.getWriter().write(Casts.writeValueAsString(result));
        };
    }

    private void createRememberMeSetting(HttpSecurity httpSecurity) throws Exception {
        try {
            PersistentTokenRepository tokenRepository = httpSecurity
                    .getSharedObject(ApplicationContext.class)
                    .getBean(PersistentTokenRepository.class);

            httpSecurity
                    .rememberMe(r -> r
                            .userDetailsService(new PersistentTokenRememberMeUserDetailsService())
                            .alwaysRemember(rememberMeProperties.isAlways())
                            .rememberMeCookieName(rememberMeProperties.getCookieName())
                            .tokenValiditySeconds(rememberMeProperties.getTokenValiditySeconds())
                            .tokenRepository(tokenRepository)
                            .rememberMeCookieDomain(rememberMeProperties.getDomain())
                            .rememberMeParameter(rememberMeProperties.getParamName())
                            .useSecureCookie(rememberMeProperties.isUseSecureCookie())
                            .key(rememberMeProperties.getKey())
                    );
        } catch (Exception e){
            httpSecurity
                    .rememberMe(r -> r
                            .rememberMeServices(new TypeTokenBasedRememberMeServices(rememberMeProperties, typeSecurityPrincipalManager))
                    );
        }
        AuthenticationProvider authenticationProvider = new TypeRememberMeAuthenticationProvider(
                rememberMeProperties.getKey(),
                typeSecurityPrincipalManager
        );
        httpSecurity.authenticationProvider(authenticationProvider);
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

    @Bean
    public InMemoryUserDetailsManager userDetailsService(AuthenticationProperties authenticationProperties,
                                                         PasswordEncoder passwordEncoder) {

        List<UserDetails> userDetails = new LinkedList<>();
        for (SecurityProperties.User user : authenticationProperties.getUsers()) {
            List<SimpleGrantedAuthority> roleAuthorities = user
                    .getRoles()
                    .stream()
                    .map(s -> StringUtils.prependIfMissing(s, RoleAuthority.DEFAULT_ROLE_PREFIX))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            userDetails.add(new User(user.getName(), passwordEncoder.encode(user.getPassword()), roleAuthorities));
        }
        return new InMemoryUserDetailsManager(userDetails);
    }

    @Bean
    public DefaultMethodSecurityExpressionHandler methodSecurityExpressionHandler(ObjectProvider<GrantedAuthorityDefaults> defaultsProvider,
                                                                                  ApplicationContext context) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setTrustResolver(new AuthenticationSuccessTokenTrustResolver());
        defaultsProvider.ifAvailable((d) -> handler.setDefaultRolePrefix(d.getRolePrefix()));
        handler.setApplicationContext(context);
        return handler;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static MethodInterceptor pluginAuthorizationMethodInterceptor(ObjectProvider<SecurityContextHolderStrategy> strategyProvider,
                                                                  ObjectProvider<AuthorizationEventPublisher> eventPublisherProvider) {

        AuthorizationManagerBeforeMethodInterceptor interceptor = new AuthorizationManagerBeforeMethodInterceptor(
                Pointcuts.union(new AnnotationMatchingPointcut(null, Plugin.class, true),
                        new AnnotationMatchingPointcut(Plugin.class, true)), new PluginSourceAuthorizationManager());
        interceptor.setOrder(AuthorizationInterceptorsOrder.PRE_AUTHORIZE.getOrder());

        strategyProvider.ifAvailable(interceptor::setSecurityContextHolderStrategy);
        eventPublisherProvider.ifAvailable(interceptor::setAuthorizationEventPublisher);

        return interceptor;
    }

}
