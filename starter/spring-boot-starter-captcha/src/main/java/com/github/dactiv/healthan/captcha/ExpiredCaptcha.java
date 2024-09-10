package com.github.dactiv.healthan.captcha;

import com.github.dactiv.healthan.commons.TimeProperties;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 抽象的可过期验证码实现
 *
 * @author maurice
 */
public class ExpiredCaptcha implements Expired, Serializable {

    @Serial
    private static final long serialVersionUID = 2371567553401150929L;

    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 过期时间（单位：秒）
     */
    private TimeProperties expireTime;

    public ExpiredCaptcha() {
    }

    @Override
    public boolean isExpired() {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expire = LocalDateTime
                .ofInstant(getCreationTime().toInstant(), ZoneId.systemDefault())
                .plus(expireTime.getValue(), expireTime.getUnit().toChronoUnit());

        return now.isAfter(expire);
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public TimeProperties getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(TimeProperties expireTime) {
        this.expireTime = expireTime;
    }
}
