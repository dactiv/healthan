package com.github.dactiv.healthan.spring.security.authentication.service.feign;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.crypto.algorithm.Base64;
import com.github.dactiv.healthan.spring.security.authentication.AuthenticationTypeTokenResolver;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.RequestAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * feign 认证类型 token 解析器实现
 *
 * @author maurice.chen
 */
public class FeignAuthenticationTypeTokenResolver implements AuthenticationTypeTokenResolver {

    public static final String DEFAULT_TYPE = "feign";

    private final AuthenticationProperties properties;

    public FeignAuthenticationTypeTokenResolver(AuthenticationProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isSupport(String type) {
        return DEFAULT_TYPE.equals(type);
    }

    @Override
    public Authentication createToken(HttpServletRequest request, HttpServletResponse response, String token) {

        MultiValueMap<String, String> body = decodeUserProperties(token);

        String username = body.getFirst(properties.getUsernameParamName());
        String password = body.getFirst(properties.getPasswordParamName());

        MultiValueMap<String, String> parameterMap = Casts.castMapToMultiValueMap(request.getParameterMap());

        return new RequestAuthenticationToken(parameterMap, username, password, DEFAULT_TYPE);
    }

    /**
     * 解码用户配置
     *
     * @param token token 值
     *
     * @return 参数信息
     */
    public static MultiValueMap<String, String> decodeUserProperties(String token) {
        String param = Base64.decodeToString(token);
        return Casts.castRequestBodyMap(param);
    }
}
