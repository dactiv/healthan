package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.spring.security.entity.RememberMeAuthenticationDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

/**
 * 记住我认证明细源, 用于获取请求 url 让 {@link com.github.dactiv.healthan.spring.security.audit.SecurityAuditEventRepositoryInterceptor}
 * 来完成针对某个指定接口的记住我是否需要进行审计事件触发使用。
 *
 * @author maurice.chen
 */
public class RememberMeAuthenticationDetailsSource extends WebAuthenticationDetailsSource {

    public RememberMeAuthenticationDetailsSource() {
    }

    @Override
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        WebAuthenticationDetails details = super.buildDetails(context);
        return new RememberMeAuthenticationDetails(details.getRemoteAddress(), details.getSessionId(), context.getRequestURI());
    }
}
