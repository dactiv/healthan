package com.github.dactiv.healthan.spring.security.authentication.adapter;


import com.github.dactiv.healthan.spring.security.authentication.RestResultAuthenticationEntryPoint;
import com.github.dactiv.healthan.spring.security.authentication.config.OAuth2Properties;
import com.github.dactiv.healthan.spring.security.authentication.handler.JsonAuthenticationFailureHandler;
import com.github.dactiv.healthan.spring.security.authentication.handler.JsonAuthenticationSuccessHandler;
import com.github.dactiv.healthan.spring.security.authentication.oidc.OidcUserInfoAuthenticationMapper;
import com.github.dactiv.healthan.spring.web.result.error.ErrorResultResolver;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.*;

import java.util.List;

/**
 * oauth 2 配置内容
 *
 * @author maurice.chen
 */
public class OAuth2WebSecurityConfigurerAfterAdapter implements WebSecurityConfigurerAfterAdapter {

    private final JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler;

    private final JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler;

    private final List<ErrorResultResolver> resultResolvers;

    private final OidcUserInfoAuthenticationMapper oidcUserInfoAuthenticationMapper;

    private final List<OAuth2AuthorizationConfigurerAdapter> oAuth2AuthorizationConfigurerAdapters;

    private final OAuth2Properties oAuth2Properties;

    public OAuth2WebSecurityConfigurerAfterAdapter(JsonAuthenticationFailureHandler jsonAuthenticationFailureHandler,
                                                   JsonAuthenticationSuccessHandler jsonAuthenticationSuccessHandler,
                                                   OidcUserInfoAuthenticationMapper oidcUserInfoAuthenticationMapper,
                                                   List<OAuth2AuthorizationConfigurerAdapter> oAuth2AuthorizationConfigurerAdapters,
                                                   List<ErrorResultResolver> resultResolvers,
                                                   OAuth2Properties oAuth2Properties) {
        this.jsonAuthenticationFailureHandler = jsonAuthenticationFailureHandler;
        this.jsonAuthenticationSuccessHandler = jsonAuthenticationSuccessHandler;
        this.oidcUserInfoAuthenticationMapper = oidcUserInfoAuthenticationMapper;
        this.oAuth2AuthorizationConfigurerAdapters = oAuth2AuthorizationConfigurerAdapters;
        this.resultResolvers = resultResolvers;
        this.oAuth2Properties = oAuth2Properties;
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .with(
                        new OAuth2AuthorizationServerConfigurer(),
                        o -> o
                                .oidc(this::configOidc)
                                .clientAuthentication(this::configClientAuthentication)
                                .authorizationEndpoint(this::configAuthorizationEndpoint)
                                .tokenEndpoint(this::configTokenEndpoint))
                .oauth2ResourceServer(this::configResourceServer);
    }

    private void configResourceServer(OAuth2ResourceServerConfigurer<HttpSecurity> resourceServer) {
        resourceServer
                .jwt(Customizer.withDefaults())
                .authenticationEntryPoint(new RestResultAuthenticationEntryPoint(resultResolvers));

        if (CollectionUtils.isNotEmpty(oAuth2AuthorizationConfigurerAdapters)) {
            oAuth2AuthorizationConfigurerAdapters.forEach(o -> o.configResourceServer(resourceServer));
        }
    }

    private void configClientAuthentication(OAuth2ClientAuthenticationConfigurer clientAuthentication) {
        clientAuthentication.errorResponseHandler(jsonAuthenticationFailureHandler);

        if (CollectionUtils.isNotEmpty(oAuth2AuthorizationConfigurerAdapters)) {
            oAuth2AuthorizationConfigurerAdapters.forEach(o -> o.configClientAuthentication(clientAuthentication));
        }
    }

    private void configTokenEndpoint(OAuth2TokenEndpointConfigurer tokenEndpoint) {
        tokenEndpoint
                .accessTokenResponseHandler(jsonAuthenticationSuccessHandler)
                .errorResponseHandler(jsonAuthenticationFailureHandler);

        if (CollectionUtils.isNotEmpty(oAuth2AuthorizationConfigurerAdapters)) {
            oAuth2AuthorizationConfigurerAdapters.forEach(o -> o.configTokenEndpoint(tokenEndpoint));
        }
    }

    private void configOidc(OidcConfigurer oidc) {
        oidc.userInfoEndpoint(endpoint -> endpoint.userInfoMapper(oidcUserInfoAuthenticationMapper).userInfoResponseHandler(jsonAuthenticationSuccessHandler).errorResponseHandler(jsonAuthenticationFailureHandler));

        if (CollectionUtils.isNotEmpty(oAuth2AuthorizationConfigurerAdapters)) {
            oAuth2AuthorizationConfigurerAdapters.forEach(o -> o.configOidc(oidc));
        }
    }

    private void configAuthorizationEndpoint(OAuth2AuthorizationEndpointConfigurer authorizationEndpoint) {
        authorizationEndpoint
                .consentPage(oAuth2Properties.getConsentPageUri())
                .authorizationResponseHandler(jsonAuthenticationSuccessHandler)
                .errorResponseHandler(jsonAuthenticationFailureHandler);

        if (CollectionUtils.isNotEmpty(oAuth2AuthorizationConfigurerAdapters)) {
            oAuth2AuthorizationConfigurerAdapters.forEach(o -> o.configAuthorizationEndpoint(authorizationEndpoint));
        }
    }

}
