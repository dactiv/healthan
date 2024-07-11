package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 认证 filter 实现, 用于结合 {@link TypeSecurityPrincipalService} 多用户类型认证的统一入口
 *
 * @author maurice.chen
 */
public class RequestAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationProperties authenticationProperties;

    private final List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolvers;

    private final List<TypeSecurityPrincipalService> typeSecurityPrincipalServices;

    public RequestAuthenticationFilter(AuthenticationProperties authenticationProperties,
                                       List<AuthenticationTypeTokenResolver> authenticationTypeTokenResolver,
                                       List<TypeSecurityPrincipalService> typeSecurityPrincipalServices) {
        this.authenticationProperties = authenticationProperties;

        setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher(authenticationProperties.getLoginProcessingUrl(), HttpMethod.POST.name())
        );

        setUsernameParameter(authenticationProperties.getUsernameParamName());
        setPasswordParameter(authenticationProperties.getPasswordParamName());

        this.authenticationTypeTokenResolvers = authenticationTypeTokenResolver;
        this.typeSecurityPrincipalServices = typeSecurityPrincipalServices;
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {

        if (StringUtils.isNotBlank(obtainType(request))) {
            return true;
        } else {
            return super.requiresAuthentication(request, response);
        }

    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        if (!HttpMethod.POST.matches(request.getMethod()) && super.requiresAuthentication(request, response)) {
            throw new AuthenticationServiceException("不支持 [" + request.getMethod() + "] 方式的登陆请求");
        }

        Authentication token = createToken(request, response);
        if (AbstractAuthenticationToken.class.isAssignableFrom(token.getClass())) {
            AbstractAuthenticationToken authenticationToken = Casts.cast(token);
            authenticationToken.setDetails(this.authenticationDetailsSource.buildDetails(request));
        }
        return getAuthenticationManager().authenticate(token);
    }

    /**
     * 创建当前用户认证 token
     *
     * @param request  http servlet request
     * @param response http servlet response
     *
     * @return 当前用户认证 token
     *
     * @throws AuthenticationException 认证异常
     */
    public Authentication createToken(HttpServletRequest request,
                                      HttpServletResponse response) throws AuthenticationException {

        String type = obtainType(request);

        if (StringUtils.isBlank(type)) {
            throw new AuthenticationServiceException("授权类型不正确");
        }

        String token = request.getHeader(authenticationProperties.getTokenHeaderName());

        if (StringUtils.isNotBlank(token)) {
            String resolverType = request.getHeader(authenticationProperties.getTokenResolverHeaderName());

            AuthenticationTypeTokenResolver resolver = authenticationTypeTokenResolvers
                    .stream()
                    .filter(a -> a.isSupport(resolverType))
                    .findFirst()
                    .orElseThrow(() -> new AuthenticationServiceException("找不到类型 [" + resolverType + "] token 解析器实现"));

            return resolver.createToken(request, response, token);

        } else {
            TypeSecurityPrincipalService typeSecurityPrincipalService = typeSecurityPrincipalServices
                    .stream()
                    .filter(u -> u.getType().contains(type))
                    .findFirst()
                    .orElseThrow(() -> new AuthenticationServiceException("找不到类型为 [" + type + "] 的用户明细服务实现"));

            return typeSecurityPrincipalService.createToken(request, response, type);
        }

    }

    /**
     * 获取类型
     *
     * @param request http servlet request
     *
     * @return 类型
     */
    protected String obtainType(HttpServletRequest request) {

        String type = request.getHeader(authenticationProperties.getTypeHeaderName());

        if (StringUtils.isBlank(type)) {
            type = request.getParameter(authenticationProperties.getTypeParamName());
        }

        return type;
    }


}
