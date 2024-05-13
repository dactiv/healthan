package com.github.dactiv.healthan.spring.security.authentication.token;

import com.github.dactiv.healthan.commons.CacheProperties;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 简单的用户认证 token
 *
 * @author maurice.chen
 */
public class SimpleAuthenticationToken extends AbstractAuthenticationToken {

    
    private static final long serialVersionUID = 3747271533448473641L;

    private final UsernamePasswordAuthenticationToken token;

    /**
     * 用户类型
     */
    private final String type;

    /**
     * 是否记住我认证
     */
    private final boolean rememberMe;

    /**
     * 当前用户认证 token
     *
     * @param username 登陆账户
     * @param type  用户类型
     */
    public SimpleAuthenticationToken(String username, String type, boolean rememberMe) {
        this(new UsernamePasswordAuthenticationToken(username, null), type, rememberMe);
    }

    /**
     * 当前用户认证 token
     *
     * @param token 登陆账户密码认证令牌
     * @param type  用户类型
     */
    public SimpleAuthenticationToken(UsernamePasswordAuthenticationToken token,
                                     String type,
                                     boolean rememberMe) {
        this(token, type, rememberMe, new ArrayList<>());
    }

    /**
     * 当前用户认证 token
     *
     * @param token       登陆账户密码认证令牌
     * @param type        用户类型
     * @param userDetails 用户明细实体
     * @param authorities 授权信息
     */
    public SimpleAuthenticationToken(UsernamePasswordAuthenticationToken token,
                                     String type,
                                     UserDetails userDetails,
                                     Collection<? extends GrantedAuthority> authorities,
                                     boolean rememberMe) {

        this(token, type, rememberMe, authorities);
        setAuthenticated(true);
        setDetails(userDetails);
    }

    /**
     * 当前用户认证 token
     *
     * @param token       登陆账户密码认证令牌
     * @param type        用户类型
     * @param authorities 授权信息
     */
    public SimpleAuthenticationToken(UsernamePasswordAuthenticationToken token,
                                     String type,
                                     boolean rememberMe,
                                     Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.type = type;
        this.rememberMe = rememberMe;
    }

    @Override
    public Object getCredentials() {
        return token.getCredentials();
    }

    @Override
    public Object getPrincipal() {
        return token.getPrincipal();
    }

    @Override
    public String getName() {
        return getType() + CacheProperties.DEFAULT_SEPARATOR + token.getPrincipal();
    }

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    public String getType() {
        return type;
    }

    /**
     * 获取是否记住我认证
     *
     * @return 是否记住我认证
     */
    public boolean isRememberMe() {
        return rememberMe;
    }

    /**
     * 获取 token 信息
     *
     * @return token 信息
     */
    public UsernamePasswordAuthenticationToken getToken() {
        return token;
    }
}
