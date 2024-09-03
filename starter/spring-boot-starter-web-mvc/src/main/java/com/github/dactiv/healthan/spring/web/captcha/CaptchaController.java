package com.github.dactiv.healthan.spring.web.captcha;

import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import com.github.dactiv.healthan.captcha.CaptchaProperties;
import com.github.dactiv.healthan.captcha.DelegateCaptchaService;
import com.github.dactiv.healthan.captcha.intercept.Interceptor;
import com.github.dactiv.healthan.captcha.tianai.TianaiCaptchaService;
import com.github.dactiv.healthan.captcha.tianai.config.TianaiCaptchaProperties;
import com.github.dactiv.healthan.captcha.token.BuildToken;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.spring.web.mvc.SpringMvcUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

/**
 * 验证码控制器
 *
 * @author maurice.chen
 */
@RequestMapping(CaptchaController.CONTROLLER_NAME)
public class CaptchaController {

    public static final String APPLICATION_JAVASCRIPT_UTF8 = "application/javascript;charset=UTF-8";

    public static final String CONTROLLER_NAME = "captcha";

    private final Interceptor interceptor;

    private final CaptchaProperties captchaProperties;

    private final DelegateCaptchaService delegateCaptchaService;

    private final TianaiCaptchaService tianaiCaptchaService;

    private final ResourceLoader resourceLoader;

    public CaptchaController(DelegateCaptchaService delegateCaptchaService,
                             Interceptor interceptor,
                             ResourceLoader resourceLoader,
                             CaptchaProperties captchaProperties,
                             TianaiCaptchaService tianaiCaptchaService) {
        this.delegateCaptchaService = delegateCaptchaService;
        this.interceptor = interceptor;
        this.resourceLoader = resourceLoader;
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
    @ResponseBody
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
    @ResponseBody
    @RequestMapping("generateCaptcha")
    public Object generateCaptcha(HttpServletRequest request) throws Exception {

        RestResult<Map<String, Object>> result = interceptor.verifyCaptcha(request);

        if (result.isSuccess()) {
            return delegateCaptchaService.generateCaptcha(request);
        }

        return result;
    }

    /**
     * 获取 tianai js
     *
     * @return js 内容
     */
    @GetMapping(TianaiCaptchaProperties.JS_CONTROLLER)
    public ResponseEntity<String> tianaiJs() throws IOException {
        Resource resource = resourceLoader.getResource(tianaiCaptchaService.getTianaiCaptchaProperties().getJsPath());

        String text = IOUtils.toString(resource.getInputStream(), Charset.defaultCharset());
        String content = text.replace(TianaiCaptchaProperties.JS_BASE_URL_TOKEN, tianaiCaptchaService.getTianaiCaptchaProperties().getApiBaseUrl());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JAVASCRIPT_UTF8);
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    /**
     * 校验验证码
     *
     * @param request http servlet request
     * @return rest 结果集
     */
    @ResponseBody
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
    @ResponseBody
    @PostMapping("clientVerify")
    public RestResult<Object> clientVerify(@RequestBody ImageCaptchaTrack track, HttpServletRequest request) {
        String token = request.getParameter(tianaiCaptchaService.getTokenParamName());
        Assert.hasText(token, tianaiCaptchaService.getTokenParamName() + "参数不能为空");

        return tianaiCaptchaService.clientVerify(track, token);
    }
}
