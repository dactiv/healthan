package com.github.dactiv.healthan.spring.security.authentication.handler;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 响应 json 数据的认证失败处理实现
 *
 * @author maurice.chen
 */
public class JsonAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final List<JsonAuthenticationFailureResponse> failureResponses;

    private final List<AntPathRequestMatcher> loginRequestMatchers = new LinkedList<>();

    public JsonAuthenticationFailureHandler(List<JsonAuthenticationFailureResponse> failureResponses,
                                            AuthenticationProperties authenticationProperties) {
        this.failureResponses = failureResponses;
        this.loginRequestMatchers.add(new AntPathRequestMatcher(authenticationProperties.getLoginProcessingUrl(), HttpMethod.POST.name()));
        this.loginRequestMatchers.add(new AntPathRequestMatcher(authenticationProperties.getOauthEndpointUri()));
        this.loginRequestMatchers.add(new AntPathRequestMatcher(authenticationProperties.getOauthOidcEndpointUri()));
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException e) throws IOException {

        if (loginRequestMatchers.stream().noneMatch(matcher -> matcher.matches(request))) {
            return ;
        }

        RestResult<Map<String, Object>> result = RestResult.ofException(
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                e,
                new LinkedHashMap<>()
        );

        if (CollectionUtils.isNotEmpty(failureResponses)) {
            failureResponses.forEach(f -> f.setting(result, request, e));
        }

        response.setStatus(result.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(Casts.writeValueAsString(result));
    }

}
