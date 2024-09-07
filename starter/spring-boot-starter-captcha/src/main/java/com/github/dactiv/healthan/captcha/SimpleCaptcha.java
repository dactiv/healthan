package com.github.dactiv.healthan.captcha;

import java.io.Serial;

/**
 * 带匹配值的验证码实现，如：图片验证码，短信验证码，邮箱验证码等类型使用该类做自动匹配
 *
 * @author maurice.chen
 */
public class SimpleCaptcha extends ReusableCaptcha {

    @Serial
    private static final long serialVersionUID = 1623791533763152034L;

    /**
     * 值
     */
    private String value;

    /**
     * 校验成功是否删除
     */
    private boolean verifySuccessDelete = true;

    public SimpleCaptcha() {
    }

    public SimpleCaptcha(String value) {
        this.value = value;
    }

    public SimpleCaptcha(String value, boolean verifySuccessDelete) {
        this.value = value;
        this.verifySuccessDelete = verifySuccessDelete;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isVerifySuccessDelete() {
        return verifySuccessDelete;
    }

    public void setVerifySuccessDelete(boolean verifySuccessDelete) {
        this.verifySuccessDelete = verifySuccessDelete;
    }
}
