package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.RequestAuthenticationToken;
import com.github.dactiv.healthan.spring.security.authentication.token.TypeAuthenticationToken;
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
public abstract class AbstractTypeSecurityPrincipalService implements TypeSecurityPrincipalService {

    private AuthenticationProperties authenticationProperties;

    public AbstractTypeSecurityPrincipalService() {
    }

    public void setAuthenticationProperties(AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

    @Override
    public Authentication createToken(HttpServletRequest request, HttpServletResponse response, String type) {
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        username = StringUtils.defaultString(username, StringUtils.EMPTY);
        password = StringUtils.defaultString(password, StringUtils.EMPTY);

        MultiValueMap<String, String> parameterMap = Casts.castMapToMultiValueMap(request.getParameterMap());

        return new RequestAuthenticationToken(parameterMap, username, password, type);
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

    @Override
    public CacheProperties getAuthorizationCache(TypeAuthenticationToken token, SecurityPrincipal principal) {
        String suffix = token.getType() + CacheProperties.DEFAULT_SEPARATOR + principal.getName();
        return CacheProperties.of(
                authenticationProperties.getAuthorizationCache().getName(suffix),
                authenticationProperties.getAuthorizationCache().getExpiresTime()
        ) ;
    }

    @Override
    public CacheProperties getAuthenticationCache(TypeAuthenticationToken token) {
        return CacheProperties.of(
                authenticationProperties.getAuthenticationCache().getName(token.getName()),
                authenticationProperties.getAuthenticationCache().getExpiresTime()
        ) ;
    }
}
