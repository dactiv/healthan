package com.github.dactiv.healthan.captcha;

import com.github.dactiv.healthan.captcha.intercept.Interceptor;
import com.github.dactiv.healthan.captcha.storage.CaptchaStorageManager;
import com.github.dactiv.healthan.captcha.token.BuildToken;
import com.github.dactiv.healthan.captcha.token.InterceptToken;
import com.github.dactiv.healthan.captcha.token.support.SimpleBuildToken;
import com.github.dactiv.healthan.captcha.token.support.SimpleInterceptToken;
import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.commons.TimeProperties;
import com.github.dactiv.healthan.commons.exception.ErrorCodeException;
import com.github.dactiv.healthan.commons.exception.ServiceException;
import com.github.dactiv.healthan.commons.exception.SystemException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.http.HttpStatus;
import org.springframework.objenesis.instantiator.util.ClassUtils;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 抽象的验证码服务实现，实现一些 {@link CaptchaService} 的部分代码
 *
 * @param <B> 请求体类型，用于在生成验证码时，通过自定类型去创建对象内容来构造验证码响应对象
 */
public abstract class AbstractCaptchaService<B> implements CaptchaService, CaptchaResolver {

    /**
     * 默认提交验证码的参数名称
     */
    private static final String DEFAULT_CAPTCHA_PARAM_NAME = "captchaParamName";
    /**
     * 验证码配置
     */
    protected CaptchaProperties captchaProperties;
    /**
     * 验证码拦截器
     */
    private Interceptor interceptor;
    /**
     * 泛型实体class
     */
    private final Class<B> requestBodyClass;
    /**
     * sprig 的内容校验者，用于校验请求参数是否通过使用
     */
    private Validator validator;
    /**
     * 验证码存储管理器
     */
    private CaptchaStorageManager captchaStorageManager;

    public AbstractCaptchaService() {

        ParameterizedType type = Casts.cast(this.getClass().getGenericSuperclass(), ParameterizedType.class);
        this.requestBodyClass = Casts.cast(type.getActualTypeArguments()[0]);
    }

    public CaptchaProperties getCaptchaProperties() {
        return captchaProperties;
    }

    public void setCaptchaProperties(CaptchaProperties captchaProperties) {
        this.captchaProperties = captchaProperties;
    }

    public Interceptor getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public CaptchaStorageManager getCaptchaStorageManager() {
        return captchaStorageManager;
    }

    public void setCaptchaStorageManager(CaptchaStorageManager captchaStorageManager) {
        this.captchaStorageManager = captchaStorageManager;
    }

    @Override
    public boolean isSupport(HttpServletRequest request) {
        boolean result = CaptchaService.super.isSupport(request);

        if (result) {
            return true;
        }

        String type = request.getHeader(captchaProperties.getCaptchaTypeHeaderName());
        if (StringUtils.isEmpty(type)) {
            type = request.getHeader(captchaProperties.getCaptchaTypeParamName());
        }

        return getType().equals(type);
    }

    protected InterceptToken createInterceptToken(String token) {
        return createInterceptToken(token, null, null);
    }

    protected InterceptToken createInterceptToken(String token, String id, TimeProperties timeProperties) {
        SimpleInterceptToken interceptToken = new SimpleInterceptToken();
        if (StringUtils.isEmpty(id)) {
            interceptToken.setId(UUID.randomUUID().toString());
        }
        Map<String, Object> args = getCreateArgs();
        interceptToken.setArgs(args);
        interceptToken.setTokenParamName(getTokenParamName());
        interceptToken.setType(getType());
        interceptToken.setToken(new CacheProperties(token, timeProperties));

        return interceptToken;
    }

    @Override
    public ConstructionCaptchaMeta createConstructionCaptchaMeta(HttpServletRequest request) {
        return new ConstructionCaptchaMeta(getType(), this.getCreateArgs());
    }

    @Override
    public BuildToken generateToken(String deviceIdentified, HttpServletRequest httpServletRequest) {

        Assert.hasText(deviceIdentified, "deviceIdentified 不能为空");

        String value = DigestUtils.md5DigestAsHex(deviceIdentified.getBytes());

        SimpleBuildToken token = new SimpleBuildToken();

        token.setId(deviceIdentified);
        token.setTokenParamName(getTokenParamName());
        token.setToken(new CacheProperties(value, captchaProperties.getBuildTokenCache().getExpiresTime()));
        token.setType(getType());

        Map<String, Object> args = getCreateArgs();
        token.setArgs(args);

        String generateInterceptorValue = httpServletRequest.getParameter(captchaProperties.getIgnoreInterceptorParamName());
        String isGenerateInterceptor = StringUtils.defaultString(generateInterceptorValue, Boolean.TRUE.toString());

        String interceptorType = getInterceptorType();
        if (StringUtils.isNotEmpty(interceptorType) && BooleanUtils.toBoolean(isGenerateInterceptor)) {
            InterceptToken interceptToken = interceptor.generateCaptchaIntercept(
                    token.getToken().getName(),
                    token.getType(),
                    interceptorType
            );
            token.setInterceptToken(interceptToken);
        }

        captchaStorageManager.saveBuildToken(token);
        return token;
    }

    protected String getInterceptorType() {
        return null;
    }

    @Override
    public InterceptToken generateInterceptorToken(BuildToken buildToken) {
        InterceptToken interceptToken = createInterceptToken(
                buildToken.getToken().getName(),
                buildToken.getId(),
                buildToken.getToken().getExpiresTime()
        );

        // 保存拦截token
        captchaStorageManager.saveInterceptToken(interceptToken);

        return interceptToken;
    }

    @Override
    public RestResult<Map<String, Object>> deleteCaptcha(HttpServletRequest request) {
        String token = request.getParameter(getTokenParamName());
        BuildToken buildToken = getBuildToken(request);

        String verifyTokenExist = StringUtils.defaultString(request.getParameter(captchaProperties.getVerifyTokenExistParamName()), Boolean.TRUE.toString());

        if (Objects.isNull(buildToken) && BooleanUtils.toBoolean(verifyTokenExist)) {
            return RestResult.ofException(ErrorCodeException.CONTENT_NOT_EXIST, new SystemException("找不到 token 为 [" + token + "] 的验证码 token 信息"));
        }

        // 成功后删除 绑定 token
        captchaStorageManager.deleteBuildToken(buildToken);
        // 删除验证码信息
        captchaStorageManager.deleteCaptcha(buildToken);

        return RestResult.of("删除验证么缓存信息成功");
    }

    @Override
    public RestResult<Map<String, Object>> verify(HttpServletRequest request) {
        BuildToken buildToken = getBuildToken(request);

        if (Objects.isNull(buildToken)) {
            throw new ServiceException("绑定 token 已过期");
        }

        return verify(buildToken, request);
    }

    @Override
    public RestResult<Map<String, Object>> verifyInterceptToken(HttpServletRequest request) {
        String token = request.getParameter(getTokenParamName());
        InterceptToken interceptToken = captchaStorageManager.getInterceptToken(token);
        if (Objects.isNull(interceptToken)) {
            throw new ServiceException("拦截 token 已过期");
        }
        return verify(interceptToken, request);
    }

    @Override
    public BuildToken getBuildToken(String token) {
        return captchaStorageManager.getBuildToken(token);
    }

    @Override
    public BuildToken getBuildToken(HttpServletRequest request) {
        String token = request.getParameter(getTokenParamName());
        return getBuildToken(token);
    }

    @Override
    public Object generateCaptcha(HttpServletRequest request) throws Exception {
        String tokenValue = request.getParameter(getTokenParamName());
        InterceptToken token = getBuildToken(tokenValue);

        if (Objects.isNull(token)) {
            token = captchaStorageManager.getInterceptToken(tokenValue);
        }

        if (Objects.isNull(token)) {
            throw new ErrorCodeException("验证码 token 已过期", ErrorCodeException.CONTENT_NOT_EXIST);
        }

        B entity = bindRequest(request);

        return generateCaptcha(token, entity, request);
    }

    protected B bindRequest(HttpServletRequest request) throws BindException {
        B entity = ClassUtils.newInstance(requestBodyClass);

        WebDataBinder binder = new WebDataBinder(entity, requestBodyClass.getSimpleName());

        MutablePropertyValues mutablePropertyValues = new MutablePropertyValues(request.getParameterMap());
        // 根据实体 class 绑定 request 的参数到实体中
        binder.bind(mutablePropertyValues);
        // 验证参数是否正确
        if (validator != null) {
            binder.setValidator(validator);
            binder.validate();
            if (binder.getBindingResult().hasErrors()) {
                throw new BindException(binder.getBindingResult());
            }
        }

        return entity;
    }

    /**
     * 生成验证码
     *
     * @param buildToken 绑定 token
     * @param requestBody 请求参数对象
     *
     * @throws Exception 生成验证码错误时跑出
     *
     * @return 验证码对象
     */
    protected Object generateCaptcha(InterceptToken buildToken, B requestBody, HttpServletRequest request) throws Exception {
        SimpleCaptcha exist = captchaStorageManager.getCaptcha(buildToken);

        if (Objects.nonNull(exist) && !exist.isRetry()) {
            return RestResult.of("当前验证码未到可重试的时间", HttpStatus.PROCESSING.value());
        }

        // 生成验证码
        GenerateCaptchaResult result = doGenerateCaptcha(buildToken, requestBody, request);

        SimpleCaptcha captcha = createMatchCaptcha(result.getMatchValue(), request, buildToken, requestBody);

        captchaStorageManager.saveCaptcha(captcha);

        return result.getResult();
    }

    protected SimpleCaptcha createMatchCaptcha(String value, HttpServletRequest request, InterceptToken buildToken, B requestBody) {
        SimpleCaptcha captcha = new SimpleCaptcha();

        captcha.setExpireTime(getCaptchaExpireTime());
        captcha.setValue(value);

        String verifySuccessDelete = StringUtils.defaultString(request.getParameter(captchaProperties.getVerifySuccessDeleteParamName()), Boolean.TRUE.toString());
        captcha.setVerifySuccessDelete(BooleanUtils.toBoolean(verifySuccessDelete));

        TimeProperties retryTime = getRetryTime();
        if (Objects.nonNull(retryTime)) {
            captcha.setRetryTime(retryTime);
        }

        return captcha;
    }

    /**
     * 生成验证码
     *
     * @param buildToken 绑定 token
     * @param requestBody 请求体
     * @param request 请i去对象
     *
     * @return 生成验证码结果集
     */
    protected abstract GenerateCaptchaResult doGenerateCaptcha(InterceptToken buildToken, B requestBody, HttpServletRequest request)  throws Exception;

    /**
     * 获取验证码过期时间
     *
     * @return 过期时间
     */
    protected abstract TimeProperties getCaptchaExpireTime();

    /**
     * 获取可重试时间
     *
     * @return 重试时间（单位：秒）
     */
    protected TimeProperties getRetryTime() {
        return null;
    }

    /**
     * 校验验证码
     *
     * @param token  绑定 token
     * @param request http servlet request
     *
     * @return 验证结果
     */
    protected RestResult<Map<String, Object>> verify(InterceptToken token, HttpServletRequest request) {

        SimpleCaptcha exist = captchaStorageManager.getCaptcha(token);
        // 如果没有，表示超时，需要客户端重新生成一个
        if (exist == null || exist.isExpired()) {
            return new RestResult<>(
                    "验证码已过期",
                    HttpStatus.REQUEST_TIMEOUT.value(),
                    ErrorCodeException.DEFAULT_EXCEPTION_CODE,
                    new LinkedHashMap<>()
            );
        }

        // 匹配验证码是否通过
        if (matchesCaptcha(request, exist)) {

            onMatchesCaptchaSuccess(token, request, exist);

            return RestResult.of("验证通过");
        }
        onMatchesCaptchaFailure(token, request, exist);
        return RestResult.of("验证码不正确",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCodeException.DEFAULT_EXCEPTION_CODE
        );
    }

    /**
     * 匹配验证码是否正确
     *
     * @param request http servlet reuqest
     * @param captcha 当前验证码
     * @return true 是，否则 false
     */
    protected boolean matchesCaptcha(HttpServletRequest request, SimpleCaptcha captcha) {

        String requestCaptcha = request.getParameter(getCaptchaParamName());
        // 匹配验证码
        return StringUtils.equalsIgnoreCase(captcha.getValue(), requestCaptcha);

    }

    protected void onMatchesCaptchaFailure(InterceptToken token, HttpServletRequest request, SimpleCaptcha exist) {
        if (!isMatchesFailureDeleteCaptcha()) {
            return ;
        }
        // 删除验证码信息
        captchaStorageManager.deleteCaptcha(token);
    }

    protected void onMatchesCaptchaSuccess(InterceptToken token, HttpServletRequest request, SimpleCaptcha exist) {
        if(token instanceof BuildToken && exist.isVerifySuccessDelete()) {
            BuildToken buildToken = Casts.cast(token);
            // 成功后删除 绑定 token
            captchaStorageManager.deleteBuildToken(buildToken);
            // 删除验证码信息
            captchaStorageManager.deleteCaptcha(buildToken);
        }
    }

    /**
     * 是否校验验证码失败直接删除当前验证码信息
     *
     * @return true 是，否则 false
     */
    protected boolean isMatchesFailureDeleteCaptcha() {
        return true;
    }

    @Override
    public String getTokenParamName() {
        return Casts.UNDERSCORE + getType() + StringUtils.capitalize(captchaProperties.getTokenParamNameSuffix());
    }

    public Map<String, Object> getCreateArgs() {

        Map<String, Object> args = new LinkedHashMap<>();

        Map<String, Object> generate = createGenerateArgs();

        if (MapUtils.isNotEmpty(generate)) {
            args.put(CaptchaResolver.GENERATE_ARGS_KEY, generate);
        }

        Map<String, Object> post = createPostArgs(generate);

        if (MapUtils.isNotEmpty(post)) {
            args.put(CaptchaResolver.POST_ARGS_KEY, post);
        }

        return args;
    }

    /**
     * 构造 post 参数信息
     *
     * @return 构造参数 map
     */
    protected Map<String, Object> createPostArgs(Map<String, Object> generate) {
        Map<String, Object> post = new LinkedHashMap<>();

        Object value = getCaptchaParamName();

        if (Objects.nonNull(value)) {
            post.put(DEFAULT_CAPTCHA_PARAM_NAME, value);
        }

        return post;
    }

    /**
     * 获取生成验证码时需要的构造参数
     *
     * @return 构造参数 map
     */
    protected Map<String, Object> createGenerateArgs() {
        return new LinkedHashMap<>();
    }
}
