package com.github.dactiv.healthan.spring.security.authentication.token;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.security.audit.IdAuditEvent;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 简单的用户认证审计 token
 *
 * @author maurice.chen
 */
public class AuditAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 3747271533448473641L;

    public static final String PRINCIPAL_KEY = "principal";

    public static final String DETAILS_KEY = "details";

    public static final String AUTHORITIES_KEY = "authorities";

    /**
     * 当前用户
     */
    private final SecurityPrincipal principal;

    /**
     * 最后登录时间
     */
    private final Date lastAuthenticationTime;

    /**
     * 当前用户类型
     */
    private final String principalType;

    /**
     * 是否记住我认证
     */
    private boolean isRememberMe = false;

    /**
     * 当前用户认证 token
     *
     * @param principal   当前用户
     * @param authorities 授权信息
     */
    public AuditAuthenticationToken(SecurityPrincipal principal,
                                    String principalType,
                                    Collection<? extends GrantedAuthority> authorities,
                                    Date lastAuthenticationTime) {
        super(authorities);
        this.principal = principal;
        this.principalType = principalType;
        this.lastAuthenticationTime = lastAuthenticationTime;
    }

    public AuditAuthenticationToken(SecurityPrincipal principal,
                                    TypeAuthenticationToken token) {
        this(principal, token, new LinkedHashSet<>());
    }

    public AuditAuthenticationToken(SecurityPrincipal principal,
                                    TypeAuthenticationToken token,
                                    Collection<? extends GrantedAuthority> grantedAuthorities) {
        this(principal, token.getType(), grantedAuthorities, new Date());
    }

    @Override
    public Object getCredentials() {
        return principal.getCredentials();
    }

    @Override
    public Object getPrincipal() {
        return getSecurityPrincipal();
    }

    @JsonIgnore
    public SecurityPrincipal getSecurityPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return getPrincipalType() + CacheProperties.DEFAULT_SEPARATOR + principal.getName();
    }

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    public String getPrincipalType() {
        return principalType;
    }

    /**
     * 获取最后登录时间
     *
     * @return 最后登录时间
     */
    public Date getLastAuthenticationTime() {
        return lastAuthenticationTime;
    }

    /**
     * 是否记住我认证
     *
     * @return true 是，否则 false
     */
    public boolean isRememberMe() {
        return isRememberMe;
    }

    /**
     * 设置是否记住我认证
     *
     * @param rememberMe true 是，否则 false
     */
    public void setRememberMe(boolean rememberMe) {
        isRememberMe = rememberMe;
    }

    /**
     * 转换为 map 数据
     *
     * @return map 数据
     */
    public Map<String, Object> toMap() {
        return toMap(true);
    }

    /**
     * 转换为 map 数据
     *
     * @param loadAuthorities 是否加载 权限信息
     *
     * @return map 数据
     */
    public Map<String, Object> toMap(boolean loadAuthorities) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(IdAuditEvent.TYPE_FIELD_NAME, getPrincipalType());
        map.put(PRINCIPAL_KEY, getPrincipal());
        map.put(DETAILS_KEY, getDetails());

        if (loadAuthorities) {
            List<String> authorities = getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            map.put(AUTHORITIES_KEY, authorities);
        }

        return map;
    }
}
