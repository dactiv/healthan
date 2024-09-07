package com.github.dactiv.healthan.captcha.token.support;

import com.github.dactiv.healthan.captcha.token.BuildToken;
import com.github.dactiv.healthan.captcha.token.InterceptToken;

import java.io.Serial;

/**
 * 简单的验证码绑定 token 实现
 *
 * @author maurice
 */
public class SimpleBuildToken extends SimpleInterceptToken implements BuildToken {

    @Serial
    private static final long serialVersionUID = 3913898137626092376L;

    /**
     * 拦截 token
     */
    private InterceptToken interceptToken;

    public SimpleBuildToken() {
    }

    @Override
    public InterceptToken getInterceptToken() {
        return interceptToken;
    }

    @Override
    public void setInterceptToken(InterceptToken interceptToken) {
        this.interceptToken = interceptToken;
    }
}
