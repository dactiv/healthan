package com.github.dactiv.healthan.spring.security.authentication;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.MultiValueMap;

/**
 * 表单登录授权明细信息
 *
 * @author maurice.chen
 */
public class FormLoginAuthenticationDetails extends WebAuthenticationDetails {

    /**
     * 登录类型，用于区分使用不同的 {@link TypeSecurityPrincipalService} 执行认证授权等业务
     */
    private final String type;

    /**
     * 请求参数信息，用于对某些特定业务流程做一些附加动作使用
     */
    private final MultiValueMap<String, String> requestParameters;

    /**
     * 请求头信息，用于对某些特定业务流程做一些附加动作使用
     */
    private final MultiValueMap<String, String> requestHeaders;

    public FormLoginAuthenticationDetails(WebAuthenticationDetails webAuthenticationDetails,
                                          String type,
                                          MultiValueMap<String, String> requestParameters,
                                          MultiValueMap<String, String> requestHeaders) {
        super(webAuthenticationDetails.getRemoteAddress(), webAuthenticationDetails.getSessionId());
        this.type = type;
        this.requestParameters = requestParameters;
        this.requestHeaders = requestHeaders;
    }

    /**
     * 获取登录类型
     *
     * @return 登录类型
     */
    public String getType() {
        return type;
    }

    /**
     * 获取请求参数信息
     *
     * @return 请求参数信息
     */
    public MultiValueMap<String, String> getRequestParameters() {
        return requestParameters;
    }

    /**
     * 获取请求头信息
     *
     * @return 请求头信息
     */
    public MultiValueMap<String, String> getRequestHeaders() {
        return requestHeaders;
    }
}
