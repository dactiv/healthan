package com.github.dactiv.healthan.spring.security.authentication.handler;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Map;

/**
 * oauth2 认证成功响应处理
 *
 * @author maurice.chen
 */
public class IgnoreAuthenticationSuccessDataResponse implements JsonAuthenticationSuccessResponse {

    //private final OAuth2Properties oAuth2Properties;

    private final AuthenticationProperties authenticationProperties;

    public IgnoreAuthenticationSuccessDataResponse(AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

    @Override
    public void setting(RestResult<Object> result, HttpServletRequest request) {

        Object details = result.getData();

        Map<String, Object> token = Casts.convertValue(details, Casts.MAP_TYPE_REFERENCE);
        List<String> properties = authenticationProperties
                .getIgnoreAuthenticationSuccessDataMap()
                .get(request.getRequestURI().replace(AntPathMatcher.DEFAULT_PATH_SEPARATOR, StringUtils.EMPTY));
        if (CollectionUtils.isNotEmpty(properties)) {
            properties.forEach(token::remove);
        }

        result.setData(token);
    }
}
