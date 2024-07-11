package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.http.HttpServletRequest;

public class FormLoginAuthenticationDetailsSource extends WebAuthenticationDetailsSource {

    private final AuthenticationProperties authenticationProperties;

    public FormLoginAuthenticationDetailsSource(AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

    @Override
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        WebAuthenticationDetails webAuthenticationDetails = super.buildDetails(context);
        String type = context.getParameter(authenticationProperties.getTypeParamName());
        if (StringUtils.isEmpty(type)) {
            type = context.getHeader(authenticationProperties.getTypeHeaderName());
        }
        return new FormLoginAuthenticationDetails(
                webAuthenticationDetails,
                type,
                Casts.castMapToMultiValueMap(context.getParameterMap())
        );
    }
}
