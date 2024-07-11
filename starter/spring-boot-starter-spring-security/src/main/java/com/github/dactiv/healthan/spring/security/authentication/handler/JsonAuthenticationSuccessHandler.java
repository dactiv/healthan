package com.github.dactiv.healthan.spring.security.authentication.handler;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.AuthenticationSuccessToken;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 响应 json 数据的认证失败处理实现
 *
 * @author maurice.chen
 */
public class JsonAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final List<JsonAuthenticationSuccessResponse> successResponses;

    private final List<AntPathRequestMatcher> loginRequestMatchers = new LinkedList<>();

    public JsonAuthenticationSuccessHandler(List<JsonAuthenticationSuccessResponse> successResponses,
                                            AuthenticationProperties authenticationProperties) {
        this(successResponses, authenticationProperties, new LinkedList<>());
    }

    public JsonAuthenticationSuccessHandler(List<JsonAuthenticationSuccessResponse> successResponses,
                                            AuthenticationProperties authenticationProperties,
                                            List<AntPathRequestMatcher> antPathRequestMatchers) {

        this.successResponses = successResponses;

        if (CollectionUtils.isNotEmpty(antPathRequestMatchers)) {
            this.loginRequestMatchers.addAll(antPathRequestMatchers);
        }

        this.loginRequestMatchers.add(new AntPathRequestMatcher(authenticationProperties.getLoginProcessingUrl(), HttpMethod.POST.name()));
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain chain,
                                        Authentication authentication) throws IOException, ServletException {

        if (loginRequestMatchers.stream().noneMatch(matcher -> matcher.matches(request))) {
            chain.doFilter(request, response);
        } else {
            onAuthenticationSuccess(request, response, authentication);
        }
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException{

        RestResult<Object> result = RestResult.of(HttpStatus.OK.getReasonPhrase());
        if (authentication instanceof AuthenticationSuccessToken) {
            AuthenticationSuccessToken authenticationSuccessToken = Casts.cast(authentication);
            result.setData(authenticationSuccessToken.toMap());
        } else {
            result.setData(authentication);
        }

        if (CollectionUtils.isNotEmpty(successResponses)) {
            successResponses.forEach(f -> f.setting(result, request));
        }

        if (loginRequestMatchers.stream().anyMatch(matcher -> matcher.matches(request))) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(Casts.writeValueAsString(result));
        }
    }
}
