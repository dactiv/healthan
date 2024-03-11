package com.github.dactiv.healthan.captcha;

import com.github.dactiv.healthan.captcha.intercept.Interceptor;
import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.commons.exception.ErrorCodeException;
import com.github.dactiv.healthan.commons.exception.ServiceException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.MutablePropertyValues;
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

    protected CaptchaProperties captchaProperties;

    private Interceptor interceptor;

    /**
     * 泛型实体class
     */
    private final Class<B> requestBodyClass;

    private Validator validator;

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

        saveBuildToken(token);

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
            saveBuildToken(token);
        }

        return token;
    }

    protected String getInterceptorType() {
        return null;
    }

    @Override
    public InterceptToken generateInterceptorToken(BuildToken buildToken) {
        SimpleInterceptToken interceptToken = Casts.of(buildToken, SimpleInterceptToken.class);

        interceptToken.setType(getType());
        interceptToken.setTokenParamName(getTokenParamName());

        Map<String, Object> args = getCreateArgs();
        interceptToken.setArgs(args);

        // 保存拦截token
        saveInterceptToken(interceptToken);

        return interceptToken;
    }

    @Override
    public RestResult<Map<String, Object>> verify(HttpServletRequest request) {
        String token = request.getParameter(getTokenParamName());
        BuildToken buildToken = getBuildToken(token);

        if (Objects.isNull(buildToken)) {
            throw new ServiceException("绑定 token 已过期");
        }

        return verify(buildToken, request);
    }

    @Override
    public RestResult<Map<String, Object>> verifyInterceptToken(HttpServletRequest request) {
        String token = request.getParameter(getTokenParamName());
        InterceptToken interceptToken = getInterceptToken(token);
        if (Objects.isNull(interceptToken)) {
            throw new ServiceException("拦截 token 已过期");
        }
        return verify(interceptToken, request);
    }

    @Override
    public Object generateCaptcha(HttpServletRequest request) throws Exception {
        InterceptToken token = getBuildToken(request);

        if (Objects.isNull(token)) {
            token = getInterceptToken(request);
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
    protected abstract Object generateCaptcha(InterceptToken buildToken,
                                              B requestBody,
                                              HttpServletRequest request)  throws Exception;

    /**
     * 校验验证码
     *
     * @param token  绑定 token
     * @param request http servlet request
     *
     * @return 验证结果
     */
    protected abstract RestResult<Map<String, Object>> verify(InterceptToken token, HttpServletRequest request);

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
