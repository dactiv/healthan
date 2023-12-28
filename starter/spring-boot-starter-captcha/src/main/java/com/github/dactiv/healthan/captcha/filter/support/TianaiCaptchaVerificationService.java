package com.github.dactiv.healthan.captcha.filter.support;

import com.github.dactiv.healthan.captcha.filter.CaptchaVerificationService;
import com.github.dactiv.healthan.captcha.tianai.TianaiCaptchaService;
import com.github.dactiv.healthan.commons.RestResult;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * tianai 验证码校验服务实现
 *
 * @author maurice.chen
 */
public class TianaiCaptchaVerificationService implements CaptchaVerificationService {

    public static final String DEFAULT_TYPE = "tianai";

    private final TianaiCaptchaService tianaiCaptchaService;

    public TianaiCaptchaVerificationService(TianaiCaptchaService tianaiCaptchaService) {
        this.tianaiCaptchaService = tianaiCaptchaService;
    }

    @Override
    public List<String> getType() {
        return Collections.singletonList(DEFAULT_TYPE);
    }

    @Override
    public void verify(HttpServletRequest request) {
        RestResult<Map<String, Object>> result =  tianaiCaptchaService.verify(request);
        Assert.isTrue(result.isSuccess(), result.getMessage());
    }
}
