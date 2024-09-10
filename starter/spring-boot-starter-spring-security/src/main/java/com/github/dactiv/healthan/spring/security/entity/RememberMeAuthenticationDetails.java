package com.github.dactiv.healthan.spring.security.entity;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 * 记住我认证明细实现, 添加请求 url 用于在审计中调用什么地址才算审计用户登录使用
 *
 * @see com.github.dactiv.healthan.spring.security.audit.SecurityAuditEventRepositoryInterceptor#preAddHandle(AuditEvent)
 *
 * @author maurice.chen
 */
public class RememberMeAuthenticationDetails extends WebAuthenticationDetails {

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
