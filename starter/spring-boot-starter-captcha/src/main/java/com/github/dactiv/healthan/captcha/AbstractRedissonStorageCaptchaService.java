package com.github.dactiv.healthan.captcha;

public abstract class AbstractRedissonStorageCaptchaService<B> extends AbstractCaptchaService<B> {

    /*@Override
    protected Object generateCaptcha(InterceptToken buildToken, B requestBody, HttpServletRequest request) throws Exception {
        SimpleCaptcha exist = getCaptchaStorageManager().getCaptcha(buildToken);

        if (Objects.nonNull(exist) && !exist.isRetry()) {
            return RestResult.of("当前验证码未到可重试的时间", HttpStatus.PROCESSING.value());
        }

        // 生成验证码
        GenerateCaptchaResult result = doGenerateCaptcha(buildToken, requestBody, request);

        SimpleCaptcha captcha = createMatchCaptcha(result.getMatchValue(), request, buildToken, requestBody);

        getCaptchaStorageManager().saveCaptcha(captcha);

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

        SimpleCaptcha exist = getCaptchaStorageManager().getCaptcha(token);
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
        getCaptchaStorageManager().deleteCaptcha(token);
    }

    protected void onMatchesCaptchaSuccess(InterceptToken token, HttpServletRequest request, SimpleCaptcha exist) {
        if(token instanceof BuildToken && exist.isVerifySuccessDelete()) {
            BuildToken buildToken = Casts.cast(token);
            // 成功后删除 绑定 token
            getCaptchaStorageManager().deleteBuildToken(buildToken);
            // 删除验证码信息
            getCaptchaStorageManager().deleteCaptcha(buildToken);
        }
    }

    *//*@Override
    public RestResult<Map<String, Object>> deleteCaptcha(HttpServletRequest request) {
        String token = request.getParameter(getTokenParamName());
        BuildToken buildToken = getCaptchaStorageManager().getBuildToken(token);

        String verifyTokenExist = StringUtils.defaultString(request.getParameter(captchaProperties.getVerifyTokenExistParamName()), Boolean.TRUE.toString());

        if (Objects.isNull(buildToken) && BooleanUtils.toBoolean(verifyTokenExist)) {
            return RestResult.ofException(ErrorCodeException.CONTENT_NOT_EXIST, new SystemException("找不到 token 为 [" + token + "] 的验证码 token 信息"));
        }

        // 成功后删除 绑定 token
        getCaptchaStorageManager().deleteBuildToken(buildToken);
        // 删除验证码信息
        getCaptchaStorageManager().deleteCaptcha(buildToken);

        return RestResult.of("删除验证么缓存信息成功");
    }*//*

    *//**
     * 匹配验证码是否正确
     *
     * @param request http servlet reuqest
     * @param captcha 当前验证码
     * @return true 是，否则 false
     *//*
    protected boolean matchesCaptcha(HttpServletRequest request, SimpleCaptcha captcha) {

        String requestCaptcha = request.getParameter(getCaptchaParamName());
        // 匹配验证码
        return StringUtils.equalsIgnoreCase(captcha.getValue(), requestCaptcha);

    }

    *//**
     * 是否校验验证码失败直接删除当前验证码信息
     *
     * @return true 是，否则 false
     *//*
    protected boolean isMatchesFailureDeleteCaptcha() {
        return true;
    }

    *//**
     * 生成验证码
     *
     * @param buildToken 绑定 token
     * @param requestBody 请求体
     * @param request 请i去对象
     *
     * @return 生成验证码结果集
     *
     * @throws Exception
     *//*
    protected abstract GenerateCaptchaResult doGenerateCaptcha(InterceptToken buildToken, B requestBody, HttpServletRequest request)  throws Exception;

    *//**
     * 获取验证码过期时间
     *
     * @return 过期时间
     *//*
    protected abstract TimeProperties getCaptchaExpireTime();

    *//**
     * 获取可重试时间
     *
     * @return 重试时间（单位：秒）
     *//*
    protected TimeProperties getRetryTime() {
        return null;
    }*/

}
