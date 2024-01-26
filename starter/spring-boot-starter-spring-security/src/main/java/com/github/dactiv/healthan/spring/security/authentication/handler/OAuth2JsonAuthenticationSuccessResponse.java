package com.github.dactiv.healthan.spring.security.authentication.handler;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.spring.security.authentication.config.AccessTokenProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationToken;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * oauth2 认证成功响应处理
 *
 * @author maurice.chen
 */
public class OAuth2JsonAuthenticationSuccessResponse implements JsonAuthenticationSuccessResponse {

    private final AuthenticationProperties authenticationProperties;

    private final List<Class<?>> supportDetailsClass = Arrays.asList(
            OAuth2AccessTokenAuthenticationToken.class,
            OAuth2AuthorizationCodeRequestAuthenticationToken.class,
            OidcUserInfoAuthenticationToken.class
    );

    public OAuth2JsonAuthenticationSuccessResponse(AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

    @Override
    public void setting(RestResult<Object> result, HttpServletRequest request) {

        Object details = result.getData();

        if (supportDetailsClass.stream().noneMatch(s -> s.isAssignableFrom(details.getClass()))) {
            return ;
        }

        Map<String, Object> token = new LinkedHashMap<>();

        if (OAuth2AccessTokenAuthenticationToken.class.isAssignableFrom(details.getClass())) {
            OAuth2AccessTokenAuthenticationToken authenticationToken = Casts.cast(details);
            token.put(AccessTokenProperties.DEFAULT_ACCESS_TOKEN_PARAM_NAME,authenticationToken.getAccessToken());
            token.put(AccessTokenProperties.DEFAULT_REFRESH_TOKEN_PARAM_NAME,authenticationToken.getRefreshToken());
        } else {
            token = Casts.convertValue(details, Casts.MAP_TYPE_REFERENCE);
            List<String> properties = authenticationProperties
                    .getOauth2()
                    .getIgnorePrincipalPropertiesMap()
                    .get(request.getRequestURI().replace(AntPathMatcher.DEFAULT_PATH_SEPARATOR, StringUtils.EMPTY));
            if (CollectionUtils.isNotEmpty(properties)) {
                properties.forEach(token::remove);
            }
        }

        result.setData(token);
    }
}
