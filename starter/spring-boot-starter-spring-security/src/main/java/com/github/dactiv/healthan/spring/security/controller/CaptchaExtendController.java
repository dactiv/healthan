package com.github.dactiv.healthan.spring.security.controller;

import com.github.dactiv.healthan.captcha.CaptchaProperties;
import com.github.dactiv.healthan.captcha.DelegateCaptchaService;
import com.github.dactiv.healthan.captcha.intercept.Interceptor;
import com.github.dactiv.healthan.captcha.token.InterceptToken;
import com.github.dactiv.healthan.commons.RestResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 验证码扩展控制器
 *
 * @author maurice.chen
 */
@RestController
@RequestMapping("captcha")
public class CaptchaExtendController {

    private final Interceptor interceptor;

    private final CaptchaProperties captchaProperties;

    private final DelegateCaptchaService delegateCaptchaService;

    public CaptchaExtendController(Interceptor interceptor,
                                   CaptchaProperties captchaProperties,
                                   DelegateCaptchaService delegateCaptchaService) {
        this.interceptor = interceptor;
        this.captchaProperties = captchaProperties;
        this.delegateCaptchaService = delegateCaptchaService;
    }

    /**
     * 创建生成验证码拦截
     *
     * @param token         要拦截的 token
     * @param type          拦截类型
     * @param interceptType 拦截的 token 类型
     * @return 绑定 token
     */
    @PreAuthorize("hasRole('FEIGN')")
    @PostMapping("createCaptchaIntercept")
    public InterceptToken createCaptchaIntercept(@RequestParam String token,
                                                 @RequestParam String type,
                                                 @RequestParam String interceptType) {
        if (StringUtils.isEmpty(type)) {
            type = captchaProperties.getDefaultCaptchaType();
        }
        return interceptor.generateCaptchaIntercept(token, type, interceptType);
    }

    /**
     * 删除验证码
     *
     * @param request http servlet request
     * @return rest 结果集
     */
    @PreAuthorize("hasRole('FEIGN')")
    @PostMapping("deleteCaptcha")
    public RestResult<Map<String, Object>> deleteCaptcha(HttpServletRequest request) {
        return delegateCaptchaService.delete(request);
    }
}
