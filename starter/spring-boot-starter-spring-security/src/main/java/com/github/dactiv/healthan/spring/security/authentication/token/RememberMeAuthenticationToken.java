package com.github.dactiv.healthan.spring.security.authentication.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.LinkedHashSet;

/**
 * 记住我认证 token
 *
 * @author maurice.chen
 */
public class RememberMeAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = -3891156895932143485L;

    /**
     * 登陆账户
     */
    private final String username;

    /**
     * token
     */
    private final String token;

    /**
     * 类型, 用于区别每个 remember me token 的分类，该值取决于 {@link com.github.dactiv.healthan.security.entity.SecurityPrincipal#getType}
     *
     * @see com.github.dactiv.healthan.security.entity.SecurityPrincipal
     */
    private final String type;

    public RememberMeAuthenticationToken(String username, String token, String type) {
        super(new LinkedHashSet<>());
        this.username = username;
        this.token = token;
        this.type = type;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    public String getType() {
        return type;
    }
}
