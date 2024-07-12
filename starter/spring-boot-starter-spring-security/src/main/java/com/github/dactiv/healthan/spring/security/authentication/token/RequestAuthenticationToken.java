package com.github.dactiv.healthan.spring.security.authentication.token;

import com.github.dactiv.healthan.spring.security.authentication.FormLoginAuthenticationDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.MultiValueMap;


/**
 * 请求认证 token
 *
 * @author maurice
 */
public class RequestAuthenticationToken extends TypeAuthenticationToken {

    private static final long serialVersionUID = 8070060147431763553L;

    private final MultiValueMap<String, String> parameterMap;

    public RequestAuthenticationToken(MultiValueMap<String, String> parameterMap,
                                      Object principal,
                                      Object credentials,
                                      String type) {
        super(principal, credentials, type);
        this.parameterMap = parameterMap;
    }

    public RequestAuthenticationToken(FormLoginAuthenticationDetails details, UsernamePasswordAuthenticationToken token) {
        super(token.getPrincipal(), token.getCredentials(), details.getType());
        WebAuthenticationDetails requestAuthenticationDetails = new WebAuthenticationDetails(
                details.getRemoteAddress(),
                details.getSessionId()
        );
        setDetails(requestAuthenticationDetails);
        parameterMap = details.getRequestParameters();
    }

    public MultiValueMap<String, String> getParameterMap() {
        return parameterMap;
    }
}
