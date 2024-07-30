package com.github.dactiv.healthan.spring.security.authentication.config;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.TimeProperties;
import com.github.dactiv.healthan.spring.security.authentication.service.TypeSecurityPrincipalManager;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter;

import java.util.LinkedList;
import java.util.List;

/**
 * 认证配置
 *
 * @author maurice.chen
 */
@ConfigurationProperties("healthan.authentication")
public class AuthenticationProperties {

    /**
     * 默认的认证类型 header 名称
     */
    public static final String SECURITY_FORM_TYPE_HEADER_NAME = "X-AUTHENTICATION-TYPE";
    /**
     * 默认的认证 token header 名称
     */
    public static final String SECURITY_FORM_TOKEN_HEADER_NAME = "X-AUTHENTICATION-TOKEN";
    /**
     * 默认的认证 token 解析 header 名称
     */
    public static final String SECURITY_FORM_TOKEN_RESOLVER_HEADER_NAME = "X-AUTHENTICATION-TOKEN-RESOLVER";
    /**
     * 默认的认证类型参数名称
     */
    public static final String SECURITY_FORM_TYPE_PARAM_NAME = "authenticationType";
    /**
     * 默认的登陆账户参数名
     */
    public static final String SECURITY_FORM_USERNAME_PARAM_NAME = "username";
    /**
     * 默认的登陆密码参数名
     */
    public static final String SECURITY_FORM_PASSWORD_PARAM_NAME = "password";

    /**
     * 方法的接口路径
     */
    private List<String> permitUriAntMatchers = new LinkedList<>();

    /**
     * 默认用户配置
     */
    private List<SecurityProperties.User> users = new LinkedList<>();

    /**
     * 登陆 url
     */
    private String loginProcessingUrl = DefaultLoginPageGeneratingFilter.DEFAULT_LOGIN_PAGE_URL;

    /**
     * 认证类型 header 名称
     */
    private String typeHeaderName = SECURITY_FORM_TYPE_HEADER_NAME;

    /**
     * 认证 token 名称
     */
    private String tokenHeaderName = SECURITY_FORM_TOKEN_HEADER_NAME;

    /**
     * 认证 token 解析名称
     */
    private String tokenResolverHeaderName = SECURITY_FORM_TOKEN_RESOLVER_HEADER_NAME;

    /**
     * 认证类型参数名称
     */
    private String typeParamName = SECURITY_FORM_TYPE_PARAM_NAME;

    /**
     * 登陆账户参数名
     */
    private String usernameParamName = SECURITY_FORM_USERNAME_PARAM_NAME;

    /**
     * 登陆密码参数名
     */
    private String passwordParamName = SECURITY_FORM_PASSWORD_PARAM_NAME;

    /**
     * ip 认证 (ip 白名单) 配置
     */
    private List<IpAuthenticationProperties> ipAuthentications = new LinkedList<>();

    /**
     * 认证缓存配置信息
     */
    private CacheProperties authenticationCache = CacheProperties.of(TypeSecurityPrincipalManager.DEFAULT_AUTHENTICATION_KEY_NAME, TimeProperties.ofDay(7));

    /**
     * 授权缓存配置信息
     */
    private CacheProperties authorizationCache = CacheProperties.of(TypeSecurityPrincipalManager.DEFAULT_AUTHORIZATION_KEY_NAME, TimeProperties.ofDay(7));

    /**
     * 是否允许访问投票器的拒绝和同意相等时允许访问
     */
    private boolean allowIfEqualGrantedDeniedDecisions = false;

    /**
     * 是否隐藏找不到用户异常
     */
    private boolean hideUserNotFoundExceptions = true;

    /**
     * 要忽略的审计类型
     */
    private List<String> ignoreAuditTypes;

    /**
     * 要忽略的审计用户名称
     */
    private List<String> ignoreAuditPrincipals;

    /**
     * 要忽略的审计操作数据最终用户名称
     */
    private List<String> ignoreOperationDataTracePrincipals;

    /**
     * 是否隐藏找不到用户异常
     *
     * @return true 是，否则 false
     */
    public boolean isHideUserNotFoundExceptions() {
        return hideUserNotFoundExceptions;
    }

    /**
     * 设置是否隐藏找不到用户异常
     *
     * @param hideUserNotFoundExceptions true 是，否则 false
     */
    public void setHideUserNotFoundExceptions(boolean hideUserNotFoundExceptions) {
        this.hideUserNotFoundExceptions = hideUserNotFoundExceptions;
    }

    /**
     * 获取默认用户信息集合
     *
     * @return 默认用户信息集合
     */
    public List<SecurityProperties.User> getUsers() {
        return users;
    }

    /**
     * 设置默认用户信息集合
     *
     * @param users 默认用户信息集合
     */
    public void setUsers(List<SecurityProperties.User> users) {
        this.users = users;
    }

    /**
     * 获取处理登陆请求的 url
     *
     * @return 处理登陆请求的 url
     */
    public String getLoginProcessingUrl() {
        return loginProcessingUrl;
    }

    /**
     * 设置处理登陆请求的 url
     *
     * @param loginProcessingUrl 处理登陆请求的 url
     */
    public void setLoginProcessingUrl(String loginProcessingUrl) {
        this.loginProcessingUrl = loginProcessingUrl;
    }

    /**
     * 获取认证类型 header 名称
     *
     * @return 认证类型 header 名称
     */
    public String getTypeHeaderName() {
        return typeHeaderName;
    }

    /**
     * 设置认证类型 header 名称
     *
     * @param typeHeaderName 认证类型 header 名称
     */
    public void setTypeHeaderName(String typeHeaderName) {
        this.typeHeaderName = typeHeaderName;
    }

    /**
     * 获取认证类型参数名称
     *
     * @return 认证类型参数名称
     */
    public String getTypeParamName() {
        return typeParamName;
    }

    /**
     * 设置认证类型参数名称
     *
     * @param typeParamName 认证类型参数名称
     */
    public void setTypeParamName(String typeParamName) {
        this.typeParamName = typeParamName;
    }

    /**
     * 获取认证 token 的头名称
     *
     * @return token 的头名称
     */
    public String getTokenHeaderName() {
        return tokenHeaderName;
    }

    /**
     * 设置 token 的头名称
     *
     * @param tokenHeaderName token 的头名称
     */
    public void setTokenHeaderName(String tokenHeaderName) {
        this.tokenHeaderName = tokenHeaderName;
    }

    /**
     * 获取认证 token 解析名称
     *
     * @return 认证 token 解析名称
     */
    public String getTokenResolverHeaderName() {
        return tokenResolverHeaderName;
    }

    /**
     * 设置认证 token 解析名称
     *
     * @param tokenResolverHeaderName 认证 token 解析名称
     */
    public void setTokenResolverHeaderName(String tokenResolverHeaderName) {
        this.tokenResolverHeaderName = tokenResolverHeaderName;
    }

    /**
     * 获取 ip 认证 (ip 白名单) 配置
     *
     * @return  ip 认证配置
     */
    public List<IpAuthenticationProperties> getIpAuthentications() {
        return ipAuthentications;
    }

    /**
     * 设置 ip 认证 (ip 白名单) 配置
     *
     * @param ipAuthentications  ip 认证配置
     */
    public void setIpAuthentications(List<IpAuthenticationProperties> ipAuthentications) {
        this.ipAuthentications = ipAuthentications;
    }

    /**
     * 获取登陆账户参数名
     *
     * @return 登陆账户参数名
     */
    public String getUsernameParamName() {
        return usernameParamName;
    }

    /**
     * 设置登陆账户参数名
     *
     * @param usernameParamName 登陆账户参数名
     */
    public void setUsernameParamName(String usernameParamName) {
        this.usernameParamName = usernameParamName;
    }

    /**
     * 获取登陆密码参数名
     *
     * @return 登陆密码参数名
     */
    public String getPasswordParamName() {
        return passwordParamName;
    }

    /**
     * 设置登陆密码参数名
     *
     * @param passwordParamName 登陆密码参数名
     */
    public void setPasswordParamName(String passwordParamName) {
        this.passwordParamName = passwordParamName;
    }

    /**
     * 获取开放的接口路径
     *
     * @return 开放的接口路径
     */
    public List<String> getPermitUriAntMatchers() {
        return permitUriAntMatchers;
    }

    /**
     * 设置开放的接口路径
     *
     * @param permitUriAntMatchers 开放的接口路径
     */
    public void setPermitUriAntMatchers(List<String> permitUriAntMatchers) {
        this.permitUriAntMatchers = permitUriAntMatchers;
    }

    /**
     * 获取认证缓存配置信息
     *
     * @return 认证缓存配置信息
     */
    public CacheProperties getAuthenticationCache() {
        return authenticationCache;
    }

    /**
     * 设置认证缓存配置信息
     *
     * @param authenticationCache 认证缓存配置信息
     */
    public void setAuthenticationCache(CacheProperties authenticationCache) {
        this.authenticationCache = authenticationCache;
    }

    /**
     * 获取授权缓存配置信息
     *
     * @return 授权缓存配置信息
     */
    public CacheProperties getAuthorizationCache() {
        return authorizationCache;
    }

    /**
     * 设置授权缓存配置信息
     *
     * @param authorizationCache 授权缓存配置信息
     */
    public void setAuthorizationCache(CacheProperties authorizationCache) {
        this.authorizationCache = authorizationCache;
    }

    /**
     * 获取是否允许访问投票器的拒绝和同意相等时允许访问
     *
     * @return true 是，否则 false
     */
    public boolean isAllowIfEqualGrantedDeniedDecisions() {
        return allowIfEqualGrantedDeniedDecisions;
    }

    /**
     * 设置是否允许访问投票器的拒绝和同意相等时允许访问
     *
     * @param allowIfEqualGrantedDeniedDecisions true 是，否则 false
     */
    public void setAllowIfEqualGrantedDeniedDecisions(boolean allowIfEqualGrantedDeniedDecisions) {
        this.allowIfEqualGrantedDeniedDecisions = allowIfEqualGrantedDeniedDecisions;
    }

    /**
     * 获取要忽略的审计用户名称
     *
     * @return 要忽略的审计用户名称集合
     */
    public List<String> getIgnoreAuditPrincipals() {
        return ignoreAuditPrincipals;
    }

    /**
     * 设置要忽略的审计用户名称
     *
     * @param ignoreAuditPrincipals 要忽略的审计用户名称集合
     */
    public void setIgnoreAuditPrincipals(List<String> ignoreAuditPrincipals) {
        this.ignoreAuditPrincipals = ignoreAuditPrincipals;
    }

    /**
     * 获取要忽略的审计类型
     *
     * @return 要忽略的审计类型集合
     */
    public List<String> getIgnoreAuditTypes() {
        return ignoreAuditTypes;
    }

    /**
     * 设置要忽略的审计类型
     *
     * @param ignoreAuditTypes 要忽略的审计类型
     */
    public void setIgnoreAuditTypes(List<String> ignoreAuditTypes) {
        this.ignoreAuditTypes = ignoreAuditTypes;
    }

    /**
     * 获取要忽略的审计操作数据最终用户名称
     *
     * @return 要忽略的审计操作数据最终用户名称
     */
    public List<String> getIgnoreOperationDataTracePrincipals() {
        return ignoreOperationDataTracePrincipals;
    }

    /**
     * 设置要忽略的审计操作数据最终用户名称
     *
     * @param ignoreOperationDataTracePrincipals 要忽略的审计操作数据最终用户名称
     */
    public void setIgnoreOperationDataTracePrincipals(List<String> ignoreOperationDataTracePrincipals) {
        this.ignoreOperationDataTracePrincipals = ignoreOperationDataTracePrincipals;
    }
}
