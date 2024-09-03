package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Enumeration;

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
        context.getHeaderNames();
        return new FormLoginAuthenticationDetails(
                webAuthenticationDetails,
                type,
                Casts.castMapToMultiValueMap(context.getParameterMap()),
                convertHeaders(context)
        );
    }

    private MultiValueMap<String, String> convertHeaders(HttpServletRequest request) {
        MultiValueMap<String, String> headersMap = new LinkedMultiValueMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);

            while (headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();
                headersMap.add(headerName, headerValue);
            }
        }

        return headersMap;
    }
}
