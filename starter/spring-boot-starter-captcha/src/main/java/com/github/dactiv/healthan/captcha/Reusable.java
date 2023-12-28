package com.github.dactiv.healthan.captcha;

/**
 * 可重试的数据实体接口
 *
 * @author maurice
 */
public interface Reusable extends Expired {

    /**
     * 是否可以重试
     *
     * @return true 是，否则 false
     */
    boolean isRetry();
}
