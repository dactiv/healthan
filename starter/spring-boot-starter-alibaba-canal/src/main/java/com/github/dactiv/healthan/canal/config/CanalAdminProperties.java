package com.github.dactiv.healthan.canal.config;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.TimeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * canal admin 配置
 *
 * @author maurice.chen
 */
@ConfigurationProperties("healthan.canal.admin")
public class CanalAdminProperties {

    /**
     * admin 地址
     */
    private String uri = "http://localhost:9097";

    /**
     * canal admin 登陆账户
     */
    private String username = "admin";

    /**
     * canal admin 登陆密码
     */
    private String password = "123456";

    /**
     * 登陆后的 login 名称
     */
    private String tokenParamName = "token";

    /**
     * 认证 token 缓存
     */
    private CacheProperties loginTokenCache = CacheProperties.of(
            "healthan:alibaba:canal:admin-login-token",
            TimeProperties.ofMinutes(5)
    );

    public CanalAdminProperties() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public CacheProperties getLoginTokenCache() {
        return loginTokenCache;
    }

    public void setLoginTokenCache(CacheProperties loginTokenCache) {
        this.loginTokenCache = loginTokenCache;
    }

    public String getTokenParamName() {
        return tokenParamName;
    }

    public void setTokenParamName(String tokenParamName) {
        this.tokenParamName = tokenParamName;
    }
}
