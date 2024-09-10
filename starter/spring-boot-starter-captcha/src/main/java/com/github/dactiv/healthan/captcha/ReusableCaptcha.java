package com.github.dactiv.healthan.captcha;

import com.github.dactiv.healthan.commons.TimeProperties;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * 抽象的可重试的验证码实现
 *
 * @author maurice
 */
public class ReusableCaptcha extends ExpiredCaptcha implements Serializable, Reusable {

    @Serial
    private static final long serialVersionUID = 2295130548867148592L;

    /**
     * 重试时间（单位：秒）
     */
    private TimeProperties retryTime;

    public ReusableCaptcha() {
    }

    /**
     * 是否可重试
     *
     * @return true 是，否则 false
     */
    @Override
    public boolean isRetry() {

        if (Objects.isNull(retryTime)) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime expireTime = LocalDateTime
                .ofInstant(getCreationTime().toInstant(), ZoneId.systemDefault())
                .plus(retryTime.getValue(), retryTime.getUnit().toChronoUnit());

        return now.isAfter(expireTime);
    }

    public TimeProperties getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(TimeProperties retryTime) {
        this.retryTime = retryTime;
    }
}
