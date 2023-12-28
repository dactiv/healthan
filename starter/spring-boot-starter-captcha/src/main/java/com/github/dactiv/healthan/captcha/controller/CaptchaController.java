package com.github.dactiv.healthan.captcha.controller;

import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.github.dactiv.healthan.captcha.BuildToken;
import com.github.dactiv.healthan.captcha.CaptchaProperties;
import com.github.dactiv.healthan.captcha.DelegateCaptchaService;
import com.github.dactiv.healthan.captcha.intercept.Interceptor;
import com.github.dactiv.healthan.captcha.tianai.TianaiCaptchaService;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.spring.web.mvc.SpringMvcUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * 验证码控制器
 *
 * @author maurice.chen
 */
@RestController
@RequestMapping("captcha")
public class CaptchaController {

    private final Interceptor interceptor;

    private final CaptchaProperties captchaProperties;

    private final DelegateCaptchaService delegateCaptchaService;

    private final TianaiCaptchaService tianaiCaptchaService;

    public CaptchaController(DelegateCaptchaService delegateCaptchaService,
                             Interceptor interceptor,
                             CaptchaProperties captchaProperties,
                             TianaiCaptchaService tianaiCaptchaService) {
        this.delegateCaptchaService = delegateCaptchaService;
        this.interceptor = interceptor;
        this.captchaProperties = captchaProperties;
        this.tianaiCaptchaService = tianaiCaptchaService;
    }

    /**
     * 生成 token
     *
     * @param type             验证码类型
     * @param deviceIdentified 设备唯一识别
     * @return 创建绑定 token
     */
    @GetMapping("generateToken")
    public BuildToken generateToken(@RequestParam(required = false) String type,
                                    @RequestParam(required = false) String deviceIdentified,
                                    HttpServletRequest request) {


        if (StringUtils.isBlank(deviceIdentified)) {
            deviceIdentified = SpringMvcUtils.getDeviceIdentified();
        }

        if (StringUtils.isEmpty(deviceIdentified) && Objects.nonNull(request.getSession())) {
            deviceIdentified = request.getSession().getId();
        }

        if (StringUtils.isEmpty(type)) {
            type = captchaProperties.getDefaultCaptchaType();
        }

        return delegateCaptchaService.generateToken(type, deviceIdentified, request);
    }

    /**
     * 生成验证码
     *
     * @param request http servlet request
     * @return 验证码
     * @throws Exception 生成错误时抛出
     */
    @RequestMapping("generateCaptcha")
    public Object generateCaptcha(HttpServletRequest request) throws Exception {

        RestResult<Map<String, Object>> result = interceptor.verifyCaptcha(request);

        if (result.isSuccess()) {
            return delegateCaptchaService.generateCaptcha(request);
        }

        return result;
    }

    /**
     * 校验验证码
     *
     * @param request http servlet request
     * @return rest 结果集
     */
    @PostMapping("verifyCaptcha")
    public RestResult<Map<String, Object>> verifyCaptcha(HttpServletRequest request) {
        return delegateCaptchaService.verify(request);
    }

    /**
     * tianai 行为验证码客户端验证处理
     *
     * @param track 行为验证拖动 track
     * @param request http servlet request
     *
     * @return rest 结果集
     */
    @PostMapping("clientVerify")
    public RestResult<Object> clientVerify(@RequestBody ImageCaptchaTrack track, HttpServletRequest request) {
        String token = request.getParameter(tianaiCaptchaService.getTokenParamName());
        Assert.hasText(token, tianaiCaptchaService.getTokenParamName() + "参数不能为空");

        return tianaiCaptchaService.clientVerify(track, token);
    }
}
