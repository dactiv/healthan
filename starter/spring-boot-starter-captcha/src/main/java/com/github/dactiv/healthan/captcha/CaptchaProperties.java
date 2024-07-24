package com.github.dactiv.healthan.captcha;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.TimeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;

/**
 * 验证码配置信息
 *
 * @author maurice.chen
 */
@ConfigurationProperties("healthan.captcha")
public class CaptchaProperties {

    public static final String DEFAULT_IGNORE_INTERCEPTOR_PARAM_NAME = "ignoreInterceptor";

    public static final String DEFAULT_VERIFY_SUCCESS_DELETE_PARAM_NAME = "verifySuccessDelete";

    public static final String DEFAULT_VERIFY_TOKEN_EXIST_PARAM_NAME = "verifyTokenExist";

    public static final String DEFAULT_TOKEN_PARAM_NAME_SUFFIX = "captchaToken";

    public static final String DEFAULT_CAPTCHA_TYPE_HEADER_NAME = "X-CAPTCHA-TYPE";

    public static final String DEFAULT_CAPTCHA_TYPE_PARAM_NAME = "captchaType";

    /**
     * 验证码 token 缓存配置
     */
    private CacheProperties buildTokenCache = CacheProperties.of("healthan:captcha:build-token:", TimeProperties.ofMinutes(5));
    /**
     * 拦截器 token 缓存配置
     */
    private CacheProperties interceptorTokenCache = CacheProperties.of("healthan:captcha:interceptor-token:", TimeProperties.ofMinutes(5));

    /**
     * 获取拦截器参数名称，用于在生成 token 时判断一些逻辑执行操作,参考:{@link AbstractCaptchaService#generateToken(String, HttpServletRequest)}
     */
    private String ignoreInterceptorParamName = DEFAULT_IGNORE_INTERCEPTOR_PARAM_NAME;

    /**
     * 校验验证码成功是否删除参数名称，当提交改名称参数为 true 时，校验成功后会删除缓存的内容
     */
    private String verifySuccessDeleteParamName = DEFAULT_VERIFY_SUCCESS_DELETE_PARAM_NAME;

    /**
     * 校验 token 是否存在参数名称
     */
    private String verifyTokenExistParamName = DEFAULT_VERIFY_TOKEN_EXIST_PARAM_NAME;

    /**
     * 验证绑定 token 的参数后缀名
     */
    private String tokenParamNameSuffix = DEFAULT_TOKEN_PARAM_NAME_SUFFIX;

    /**
     * 默认验证码类型
     */
    private String defaultCaptchaType = "tianai";

    /**
     * 验证码类型请求头名称
     */
    private String captchaTypeHeaderName = DEFAULT_CAPTCHA_TYPE_HEADER_NAME;

    /**
     * 验证码类型请求参数名称
     */
    private String captchaTypeParamName = DEFAULT_CAPTCHA_TYPE_PARAM_NAME;

    /**
     * 必须要校验验证码通过后在执行的 url 集合
     */
    private List<String> verifyUrls = new LinkedList<>();

    /**
     * 匹配 url 时使用 {@link  org.springframework.util.AntPathMatcher} 作为匹配标准，改值会写入 {@link  org.springframework.util.AntPathMatcher#setCaseSensitive(boolean)} 中
     */
    private boolean filterAntPathMatcherCaseSensitive = true;

    /**
     * {@link com.github.dactiv.healthan.captcha.filter.CaptchaVerificationFilter} 的排序值
     */
    private int filterOrderValue = Ordered.HIGHEST_PRECEDENCE + 60;

    public CaptchaProperties() {
    }

    public CacheProperties getBuildTokenCache() {
        return buildTokenCache;
    }

    public void setBuildTokenCache(CacheProperties buildTokenCache) {
        this.buildTokenCache = buildTokenCache;
    }

    public String getIgnoreInterceptorParamName() {
        return ignoreInterceptorParamName;
    }

    public void setIgnoreInterceptorParamName(String ignoreInterceptorParamName) {
        this.ignoreInterceptorParamName = ignoreInterceptorParamName;
    }

    public String getVerifySuccessDeleteParamName() {
        return verifySuccessDeleteParamName;
    }

    public void setVerifySuccessDeleteParamName(String verifySuccessDeleteParamName) {
        this.verifySuccessDeleteParamName = verifySuccessDeleteParamName;
    }

    public String getVerifyTokenExistParamName() {
        return verifyTokenExistParamName;
    }

    public void setVerifyTokenExistParamName(String verifyTokenExistParamName) {
        this.verifyTokenExistParamName = verifyTokenExistParamName;
    }

    public String getTokenParamNameSuffix() {
        return tokenParamNameSuffix;
    }

    public void setTokenParamNameSuffix(String tokenParamNameSuffix) {
        this.tokenParamNameSuffix = tokenParamNameSuffix;
    }

    public String getDefaultCaptchaType() {
        return defaultCaptchaType;
    }

    public void setDefaultCaptchaType(String defaultCaptchaType) {
        this.defaultCaptchaType = defaultCaptchaType;
    }

    public CacheProperties getInterceptorTokenCache() {
        return interceptorTokenCache;
    }

    public void setInterceptorTokenCache(CacheProperties interceptorTokenCache) {
        this.interceptorTokenCache = interceptorTokenCache;
    }

    public String getCaptchaTypeHeaderName() {
        return captchaTypeHeaderName;
    }

    public void setCaptchaTypeHeaderName(String captchaTypeHeaderName) {
        this.captchaTypeHeaderName = captchaTypeHeaderName;
    }

    public String getCaptchaTypeParamName() {
        return captchaTypeParamName;
    }

    public void setCaptchaTypeParamName(String captchaTypeParamName) {
        this.captchaTypeParamName = captchaTypeParamName;
    }

    public List<String> getVerifyUrls() {
        return verifyUrls;
    }

    public void setVerifyUrls(List<String> verifyUrls) {
        this.verifyUrls = verifyUrls;
    }

    public boolean isFilterAntPathMatcherCaseSensitive() {
        return filterAntPathMatcherCaseSensitive;
    }

    public void setFilterAntPathMatcherCaseSensitive(boolean filterAntPathMatcherCaseSensitive) {
        this.filterAntPathMatcherCaseSensitive = filterAntPathMatcherCaseSensitive;
    }

    public int getFilterOrderValue() {
        return filterOrderValue;
    }

    public void setFilterOrderValue(int filterOrderValue) {
        this.filterOrderValue = filterOrderValue;
    }
}
