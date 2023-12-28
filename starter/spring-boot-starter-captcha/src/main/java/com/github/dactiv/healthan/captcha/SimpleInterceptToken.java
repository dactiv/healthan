package com.github.dactiv.healthan.captcha;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.TimeProperties;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

/**
 * 简单的验证码拦截 token 实现
 *
 * @author maurice
 */
public class SimpleInterceptToken extends ConstructionCaptchaMeta implements InterceptToken {

    private static final long serialVersionUID = -7503502769793672985L;

    /**
     * 唯一识别
     */
    private String id;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 绑定 token 值
     */
    private CacheProperties token;

    /**
     * 对应的 token 参数名称
     */
    private String tokenParamName;

    public SimpleInterceptToken() {
    }

    public SimpleInterceptToken(String type, Map<String, Object> args) {
        super(type, args);
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public CacheProperties getToken() {
        return token;
    }

    public void setToken(CacheProperties token) {
        this.token = token;
    }

    public String getTokenParamName() {
        return tokenParamName;
    }

    public void setTokenParamName(String tokenParamName) {
        this.tokenParamName = tokenParamName;
    }

    @Override
    public boolean isExpired() {

        TimeProperties time = token.getExpiresTime();

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expireTime = LocalDateTime
                .ofInstant(getCreationTime().toInstant(), ZoneId.systemDefault())
                .plus(time.getValue(), time.toChronoUnit());

        return now.isAfter(expireTime);
    }
}
