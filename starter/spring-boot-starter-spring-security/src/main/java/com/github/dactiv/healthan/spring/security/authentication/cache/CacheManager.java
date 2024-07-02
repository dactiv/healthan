package com.github.dactiv.healthan.spring.security.authentication.cache;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.token.ExpiredToken;
import com.github.dactiv.healthan.spring.security.authentication.token.RefreshToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;

import java.util.Collection;
import java.util.Map;

/**
 * 缓存管理器,用于多 spring security 所扩展的缓存内容进行管理。
 *
 * @author maurice.chen
 */
public interface CacheManager {

    /**
     * 获取缓存的用户信息
     *
     * @param authenticationCache 认证缓存配置
     *
     * @return 用户信息
     */
    SecurityPrincipal getSecurityPrincipal(CacheProperties authenticationCache);

    /**
     * 缓存当前用户
     *
     * @param principal 当前用户
     * @param authenticationCache 认证缓存配置
     */
    void saveSecurityPrincipal(SecurityPrincipal principal, CacheProperties authenticationCache);

    /**
     * 获取授权信息
     *
     * @param authorizationCache 授权缓存配置
     *
     * @return 授权信息集合
     */
    Collection<GrantedAuthority> getGrantedAuthorities(CacheProperties authorizationCache);

    /**
     * 缓存授权信息
     *
     * @param grantedAuthorities 授权信息集合
     * @param authorizationCache 授权缓存配置
     */
    void saveGrantedAuthorities(Collection<GrantedAuthority> grantedAuthorities, CacheProperties authorizationCache);

    /**
     * 获取 spring security 上下文内容
     *
     * @param type 用户类型
     * @param id 主键 id
     *
     * @return spring security 上下文内容
     */
    SecurityContext getSecurityContext(String type, Object id, CacheProperties accessTokenCache);

    /**
     * 延期 spring security 上下文内容
     *
     * @param context spring security 上下文内容
     */
    void delaySecurityContext(SecurityContext context, CacheProperties accessTokenCache);

    /**
     * 保存 spring security 上下文内容
     *
     * @param context spring security 上下文内容
     * @param accessTokenCache 令牌访问缓存配置
     */
    void saveSecurityContext(SecurityContext context, CacheProperties accessTokenCache);

    /**
     * 保存 spring security 上下文 的刷新 token
     *
     * @param refreshToken spring security 上下文 的刷新 token
     * @param refreshTokenCache spring security 上下文 的刷新 token 缓存配置
     */
    void saveSecurityContextRefreshToken(RefreshToken refreshToken, CacheProperties refreshTokenCache);

    /**
     * 验证 token 是否有效
     *
     * @param refreshToken 刷新 token
     * @param refreshTokenCache 刷新 token 缓存配置
     *
     * @return rest 结果集
     */
    RestResult<ExpiredToken> getRefreshToken(String refreshToken, CacheProperties refreshTokenCache);
}
