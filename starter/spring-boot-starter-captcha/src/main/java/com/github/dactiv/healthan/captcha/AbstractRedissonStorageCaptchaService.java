package com.github.dactiv.healthan.captcha;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.RestResult;
import com.github.dactiv.healthan.commons.TimeProperties;
import com.github.dactiv.healthan.commons.exception.ErrorCodeException;
import com.github.dactiv.healthan.commons.exception.SystemException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractRedissonStorageCaptchaService<B> extends AbstractRedissonCaptchaService<B> {

    @Override
    protected Object generateCaptcha(InterceptToken buildToken, B requestBody, HttpServletRequest request) throws Exception {
        RBucket<SimpleCaptcha> bucket = getCaptchaBucket(buildToken);
        SimpleCaptcha exist = bucket.get();

        if (Objects.nonNull(exist) && !exist.isRetry()) {
            return RestResult.of("当前验证码未到可重试的时间", HttpStatus.PROCESSING.value());
        }

        // 生成验证码
        GenerateCaptchaResult result = doGenerateCaptcha(buildToken, requestBody, request);

        SimpleCaptcha captcha = createMatchCaptcha(result.getMatchValue(), request, buildToken, requestBody);

        bucket.setAsync(captcha, captcha.getExpireTime().getValue(), captcha.getExpireTime().getUnit());

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

    @Override
    protected RestResult<Map<String, Object>> verify(InterceptToken token, HttpServletRequest request) {

        SimpleCaptcha exist = getCaptchaBucket(token).get();
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

    protected void onMatchesCaptchaFailure(InterceptToken token, HttpServletRequest request, SimpleCaptcha exist) {
        if (!isMatchesFailureDeleteCaptcha()) {
            return ;
        }
        // 删除验证码信息
        deleteCaptcha(token);
    }

    protected void onMatchesCaptchaSuccess(InterceptToken token, HttpServletRequest request, SimpleCaptcha exist) {
        if(token instanceof BuildToken && exist.isVerifySuccessDelete()) {
            BuildToken buildToken = Casts.cast(token);
            // 成功后删除 绑定 token
            deleteBuildToken(buildToken);
            // 删除验证码信息
            deleteCaptcha(buildToken);
        }
    }

    @Override
    public RestResult<Map<String, Object>> delete(HttpServletRequest request) {
        String token = request.getParameter(getTokenParamName());
        BuildToken buildToken = getBuildToken(token);

        String verifyTokenExist = StringUtils.defaultString(request.getParameter(captchaProperties.getVerifyTokenExistParamName()), Boolean.TRUE.toString());

        if (Objects.isNull(buildToken) && BooleanUtils.toBoolean(verifyTokenExist)) {
            return RestResult.ofException(ErrorCodeException.CONTENT_NOT_EXIST, new SystemException("找不到 token 为 [" + token + "] 的验证码 token 信息"));
        }

        // 成功后删除 绑定 token
        deleteBuildToken(buildToken);
        // 删除验证码信息
        deleteCaptcha(buildToken);

        return RestResult.of("删除验证么缓存信息成功");
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

    /**
     * 是否校验验证码失败直接删除当前验证码信息
     *
     * @return true 是，否则 false
     */
    protected boolean isMatchesFailureDeleteCaptcha() {
        return true;
    }

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
     * 获取验证码桶
     *
     * @param token token 值
     * @return 绑定 token 桶
     */
    public RBucket<SimpleCaptcha> getCaptchaBucket(InterceptToken token) {
        return redissonClient.getBucket(getCaptchaKey(token));
    }

    /**
     * 获取验证码桶
     *
     * @param token token 值
     * @return 绑定 token 桶
     */
    public RBucket<SimpleCaptcha> getCaptchaBucket(String token) {
        return redissonClient.getBucket(getCaptchaKey(token));
    }

    /**
     * 删除缓存验证码
     *
     * @param token 绑定 token 值
     */
    public void deleteCaptcha(InterceptToken token) {
        getCaptchaBucket(token).deleteAsync();
    }

    /**
     * 获取存储在 redis 的验证码实体 key 名称
     *
     * @param token 拦截 token
     * @return key 名称
     */
    private String getCaptchaKey(InterceptToken token) {
        return getCaptchaKey(token.getToken().getName());
    }

    private String getCaptchaKey(String key) {
        String name = getType() + CacheProperties.DEFAULT_SEPARATOR + SimpleCaptcha.class.getSimpleName().toLowerCase() + CacheProperties.DEFAULT_SEPARATOR + key;
        return captchaProperties.getBuildTokenCache().getName(name);
    }

}
