package com.github.dactiv.healthan.spring.security.authentication;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.MultiValueMap;

public class FormLoginAuthenticationDetails extends WebAuthenticationDetails {

    private final String type;

    private final MultiValueMap<String, String> parameterMap;

    public FormLoginAuthenticationDetails(WebAuthenticationDetails webAuthenticationDetails, String type, MultiValueMap<String, String> parameterMap) {
        super(webAuthenticationDetails.getRemoteAddress(), webAuthenticationDetails.getSessionId());
        this.type = type;
        this.parameterMap = parameterMap;
    }

    public String getType() {
        return type;
    }

    public MultiValueMap<String, String> getParameterMap() {
        return parameterMap;
    }
}
