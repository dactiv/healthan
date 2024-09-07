package com.github.dactiv.healthan.captcha;

import java.io.Serial;
import java.io.Serializable;

/**
 * 生成验证码结果集
 *
 * @author maurice
 */
public class GenerateCaptchaResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1739665352581051182L;
    /**
     * 结果集
     */
    private final Object result;

    /**
     * 要匹配的验证码值
     */
    private final String matchValue;

    public GenerateCaptchaResult(Object result, String matchValue) {
        this.result = result;
        this.matchValue = matchValue;
    }

    public Object getResult() {
        return result;
    }

    public String getMatchValue() {
        return matchValue;
    }

    public static GenerateCaptchaResult of(Object result, String matchValue) {
        return new GenerateCaptchaResult(result, matchValue);
    }
}
