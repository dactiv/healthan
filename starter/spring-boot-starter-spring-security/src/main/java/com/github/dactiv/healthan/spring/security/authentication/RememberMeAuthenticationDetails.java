package com.github.dactiv.healthan.spring.security.authentication;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 *
 */
public class RememberMeAuthenticationDetails extends WebAuthenticationDetails {

    public static final String DETAILS_FIELD = "details";

    /**
     * 请求地址
     */
    private final String requestUri;

    public RememberMeAuthenticationDetails(String remoteAddress,
                                           String sessionId,
                                           String requestUri) {
        super(remoteAddress, sessionId);
        this.requestUri = requestUri;
    }

    /**
     * 获取请求地址
     *
     * @return 请求地址
     */
    public String getRequestUri() {
        return requestUri;
    }
}
