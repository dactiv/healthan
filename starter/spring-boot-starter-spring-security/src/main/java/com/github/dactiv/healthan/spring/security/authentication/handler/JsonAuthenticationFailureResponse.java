package com.github.dactiv.healthan.spring.security.authentication.handler;

import com.github.dactiv.healthan.commons.RestResult;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 响应 json 数据的认证失败处理实现后的数据追加接口
 *
 * @author maurice.chen
 */
public interface JsonAuthenticationFailureResponse {

    /**
     * 设置响应信息
     *
     * @param result  响应内容
     * @param request http 请求
     * @param e 异常信息
     */
    void setting(RestResult<Map<String, Object>> result, HttpServletRequest request, AuthenticationException e);
}
