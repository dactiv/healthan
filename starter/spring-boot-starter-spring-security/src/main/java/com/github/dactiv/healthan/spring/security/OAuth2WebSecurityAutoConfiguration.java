package com.github.dactiv.healthan.spring.security;


import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.crypto.algorithm.Base64;
import com.github.dactiv.healthan.crypto.algorithm.cipher.RsaCipherService;
import com.github.dactiv.healthan.spring.security.authentication.RedissonOAuth2AuthorizationService;
import com.github.dactiv.healthan.spring.security.authentication.adapter.OAuth2AuthorizationConfigurerAdapter;
import com.github.dactiv.healthan.spring.security.authentication.adapter.OAuth2WebSecurityConfigurerAfterAdapter;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.OAuth2Properties;
import com.github.dactiv.healthan.spring.security.authentication.handler.*;
import com.github.dactiv.healthan.spring.security.authentication.oidc.OidcUserInfoAuthenticationMapper;
import com.github.dactiv.healthan.spring.security.authentication.oidc.OidcUserInfoAuthenticationResolver;
import com.github.dactiv.healthan.spring.security.authentication.service.TypeSecurityPrincipalManager;
import com.github.dactiv.healthan.spring.web.result.error.ErrorResultResolver;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.stream.Collectors;

@Configuration
@AutoConfigureBefore(SpringSecurityAutoConfiguration.class)
@EnableConfigurationProperties(AuthenticationProperties.class)
@ConditionalOnClass({OAuth2AuthorizationServerConfigurer.class, SpringSecurityAutoConfiguration.class})
@ConditionalOnProperty(prefix = "healthan.authentication.spring.security.oauth2", value = "enabled", matchIfMissing = true)
public class OAuth2WebSecurityAutoConfiguration {

    private static final RsaCipherService cipherService = new RsaCipherService();

    /**
     * jwk source 配置
     *
     * @return JWKSource
     */
    @Bean
    @ConditionalOnMissingBean(JWKSource.class)
    public JWKSource<SecurityContext> jwkSource(OAuth2Properties oAuth2Properties) {
        PublicKey publicKey = cipherService.getPublicKey(Base64.decode(oAuth2Properties.getPublicKey()));
        PrivateKey privateKey = cipherService.getPrivateKey(Base64.decode(oAuth2Properties.getPrivateKey()));

        RSAPublicKey rsaPublicKey = Casts.cast(publicKey);
        RSAPrivateKey rsaPrivateKey = Casts.cast(privateKey);

        RSAKey rsaKey = new RSAKey.Builder(rsaPublicKey)
                .privateKey(rsaPrivateKey)
                .keyID(oAuth2Properties.getKeyId())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    @ConditionalOnMissingBean(JsonAuthenticationSuccessHandler.class)
    public JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler(ObjectProvider<JsonAuthenticationSuccessResponse> successResponse,
                                                                             AuthenticationProperties properties,
                                                                             OAuth2Properties oAuth2Properties) {

        return new JsonAuthenticationSuccessHandler(
                successResponse.orderedStream().collect(Collectors.toList()),
                properties,
                oAuth2Properties.getOauth2Urls()
        );
    }

    @Bean
    @ConditionalOnMissingBean(JsonAuthenticationFailureHandler.class)
    public JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler(ObjectProvider<JsonAuthenticationFailureResponse> failureHandlers,
                                                                             AuthenticationProperties properties,
                                                                             OAuth2Properties oAuth2Properties) {

        return new JsonAuthenticationFailureHandler(
                failureHandlers.orderedStream().collect(Collectors.toList()),
                properties,
                oAuth2Properties.getOauth2Urls()
        );
    }

    @Bean
    @ConditionalOnMissingBean(IgnoreAuthenticationSuccessDataResponse.class)
    public IgnoreAuthenticationSuccessDataResponse ignoreAuthenticationSuccessDataResponse(AuthenticationProperties properties) {
        return new IgnoreAuthenticationSuccessDataResponse(properties);
    }

    /**
     * 配置jwt解析器
     *
     * @param jwkSource jwk源
     * @return JwtDecoder
     */
    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * 添加认证服务器配置，设置jwt签发者、默认端点请求地址等
     *
     * @return AuthorizationServerSettings
     */
    @Bean
    @ConditionalOnMissingBean(AuthorizationServerSettings.class)
    public AuthorizationServerSettings authorizationServerSettings(OAuth2Properties oAuth2Properties) {
        AuthorizationServerSettings.Builder builder = AuthorizationServerSettings
                .builder()
                .authorizationEndpoint(oAuth2Properties.getAuthorizeEndpoint())
                .deviceAuthorizationEndpoint(oAuth2Properties.getDeviceAuthorizationEndpoint())
                .deviceVerificationEndpoint(oAuth2Properties.getDeviceVerificationEndpoint())
                .tokenEndpoint(oAuth2Properties.getTokenEndpoint())
                .jwkSetEndpoint(oAuth2Properties.getJwkSetEndpoint())
                .tokenRevocationEndpoint(oAuth2Properties.getTokenRevocationEndpoint())
                .tokenIntrospectionEndpoint(oAuth2Properties.getTokenIntrospectionEndpoint())
                .oidcClientRegistrationEndpoint(oAuth2Properties.getOidcClientRegistrationEndpoint())
                .oidcUserInfoEndpoint(oAuth2Properties.getOidcUserInfoEndpoint())
                .oidcLogoutEndpoint(oAuth2Properties.getOidcLogoutEndpoint());

        if (StringUtils.isNotEmpty(oAuth2Properties.getIssuer())) {
            builder.issuer(oAuth2Properties.getIssuer());
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(OidcUserInfoAuthenticationMapper.class)
    public OidcUserInfoAuthenticationMapper oidcUserInfoAuthenticationMapper(ObjectProvider<OidcUserInfoAuthenticationResolver> oidcUserInfoAuthenticationResolvers){
        return new OidcUserInfoAuthenticationMapper(oidcUserInfoAuthenticationResolvers.orderedStream().collect(Collectors.toList()));
    }

    @Bean
    @ConditionalOnMissingBean(OAuth2AuthorizationService.class)
    public InMemoryOAuth2AuthorizationService inMemoryOAuth2AuthorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    @ConditionalOnMissingBean(OAuth2WebSecurityConfigurerAfterAdapter.class)
    public OAuth2WebSecurityConfigurerAfterAdapter oAuth2WebSecurityConfigurerAfterAdapter(JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler,
                                                                                           JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler,
                                                                                           OidcUserInfoAuthenticationMapper oidcUserInfoAuthenticationMapper,
                                                                                           ObjectProvider<OAuth2AuthorizationConfigurerAdapter> oAuth2AuthorizationConfigurerAdapters,
                                                                                           ObjectProvider<ErrorResultResolver> resultResolvers,
                                                                                           RedissonOAuth2AuthorizationService authenticationProvider,
                                                                                           TypeSecurityPrincipalManager typeSecurityPrincipalManager) {
        return new OAuth2WebSecurityConfigurerAfterAdapter(
                jsonAuthenticationFailureHandler,
                jsonAuthenticationSuccessHandler,
                oidcUserInfoAuthenticationMapper,
                oAuth2AuthorizationConfigurerAdapters.orderedStream().collect(Collectors.toList()),
                resultResolvers.orderedStream().collect(Collectors.toList()),
                authenticationProvider,
                typeSecurityPrincipalManager
        );
    }
}
