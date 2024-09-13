package com.github.dactiv.healthan.spring.security.authentication.handler;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.OAuth2Properties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * oauth2 认证成功响应处理
 *
 * @author maurice.chen
 */
public class OAuth2JsonAuthenticationSuccessResponse implements JsonAuthenticationSuccessResponse {

    private final OAuth2Properties oAuth2Properties;

    public OAuth2JsonAuthenticationSuccessResponse(OAuth2Properties oAuth2Properties) {
        this.oAuth2Properties = oAuth2Properties;
    }

    @Override
    public void setting(RestResult<Object> result, HttpServletRequest request) {

        Object details = result.getData();

        Map<String, Object> token = Casts.convertValue(details, Casts.MAP_TYPE_REFERENCE);
        List<String> properties = oAuth2Properties
                .getIgnorePrincipalPropertiesMap()
                .get(request.getRequestURI().replace(AntPathMatcher.DEFAULT_PATH_SEPARATOR, StringUtils.EMPTY));
        if (CollectionUtils.isNotEmpty(properties)) {
            properties.forEach(token::remove);
        }

        result.setData(token);
    }
}
