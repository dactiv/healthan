package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.RememberMeProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.RequestAuthenticationToken;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 抽象的用户明细服务实现，实现创建认证 token，等部分公用功能
 *
 * @author maurice.chen
 *
 */
public abstract class AbstractUserDetailsService implements UserDetailsService {

    private AuthenticationProperties authenticationProperties;

    private RememberMeProperties rememberMeProperties;

    public AbstractUserDetailsService() {
    }

    public void setAuthenticationProperties(AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

    public void setRememberMeProperties(RememberMeProperties rememberMeProperties) {
        this.rememberMeProperties = rememberMeProperties;
    }

    @Override
    public Authentication createToken(HttpServletRequest request, HttpServletResponse response, String type) {
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        username = StringUtils.defaultString(username, StringUtils.EMPTY);
        password = StringUtils.defaultString(password, StringUtils.EMPTY);

        boolean rememberMe = obtainRememberMe(request);
        MultiValueMap<String, String> parameterMap = Casts.castMapToMultiValueMap(request.getParameterMap());

        return new RequestAuthenticationToken(parameterMap, username, password, type, rememberMe);
    }

    /**
     * 获取记住我
     *
     * @param request http servlet request
     *
     * @return true 记住我，否则 false
     */
    protected boolean obtainRememberMe(HttpServletRequest request) {
        return BooleanUtils.toBoolean(request.getParameter(rememberMeProperties.getParamName()));
    }

    /**
     * 获取登陆账户
     *
     * @param request http servlet request
     *
     * @return 登陆账户
     */
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(authenticationProperties.getUsernameParamName());
    }

    /**
     * 获取登陆密码
     *
     * @param request http servlet request
     *
     * @return 登陆密码
     */
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(authenticationProperties.getPasswordParamName());
    }

    /**
     * 获取配置信息
     *
     * @return 配置信息
     */
    public AuthenticationProperties getAuthenticationProperties() {
        return authenticationProperties;
    }
}
