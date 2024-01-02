package com.github.dactiv.healthan.captcha.tianai.config;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import com.github.dactiv.healthan.commons.TimeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@ConfigurationProperties("healthan.captcha.tianai")
public class TianaiCaptchaProperties {

    public static final String JS_URL_KEY = "jsUrl";


    public static final String JS_CONTROLLER = "/tianai-js";

    public static final String JS_BASE_URL_TOKEN = "_$_[baseUrlToken]_$_";


    /**
     * 短信验证码的超时时间
     */
    private TimeProperties captchaExpireTime = new TimeProperties(5, TimeUnit.MINUTES);

    private List<String> randomCaptchaType = Arrays.asList(CaptchaTypeConstant.SLIDER, CaptchaTypeConstant.ROTATE, CaptchaTypeConstant.CONCAT);

    private String jsPath = "classpath:tianai/js/tianai-captcha.js";

    /**
     * 接口基础 url 路径
     */
    private String apiBaseUrl = "http://localhost:8080";

    /**
     * 提交短信验证码的参数名称
     */
    private String captchaParamName = "_tianaiCaptcha";

    private Map<String, Float> tolerantMap = new LinkedHashMap<>();

    private Map<String, List<ResourceProperties>> resourceMap = new LinkedHashMap<>();

    private Map<String, List<TemplateProperties>> templateMap = new LinkedHashMap<>();

    private TimeProperties serverVerifyTimeout = TimeProperties.of(2,TimeUnit.MINUTES);

    public TianaiCaptchaProperties() {
    }

    public TimeProperties getCaptchaExpireTime() {
        return captchaExpireTime;
    }

    public void setCaptchaExpireTime(TimeProperties captchaExpireTime) {
        this.captchaExpireTime = captchaExpireTime;
    }

    public List<String> getRandomCaptchaType() {
        return randomCaptchaType;
    }

    public void setRandomCaptchaType(List<String> randomCaptchaType) {
        this.randomCaptchaType = randomCaptchaType;
    }

    public String getJsPath() {
        return jsPath;
    }

    public void setJsPath(String jsPath) {
        this.jsPath = jsPath;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getCaptchaParamName() {
        return captchaParamName;
    }

    public void setCaptchaParamName(String captchaParamName) {
        this.captchaParamName = captchaParamName;
    }

    public Map<String, Float> getTolerantMap() {
        return tolerantMap;
    }

    public void setTolerantMap(Map<String, Float> tolerantMap) {
        this.tolerantMap = tolerantMap;
    }

    public Map<String, List<ResourceProperties>> getResourceMap() {
        return resourceMap;
    }

    public void setResourceMap(Map<String, List<ResourceProperties>> resourceMap) {
        this.resourceMap = resourceMap;
    }

    public Map<String, List<TemplateProperties>> getTemplateMap() {
        return templateMap;
    }

    public void setTemplateMap(Map<String, List<TemplateProperties>> templateMap) {
        this.templateMap = templateMap;
    }

    public TimeProperties getServerVerifyTimeout() {
        return serverVerifyTimeout;
    }

    public void setServerVerifyTimeout(TimeProperties serverVerifyTimeout) {
        this.serverVerifyTimeout = serverVerifyTimeout;
    }
}
