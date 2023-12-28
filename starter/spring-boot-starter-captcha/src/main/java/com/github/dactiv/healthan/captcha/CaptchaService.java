package com.github.dactiv.healthan.captcha;

import com.github.dactiv.healthan.commons.RestResult;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 验证码服务
 *
 * @author maurice
 */
public interface CaptchaService {

    /**
     * 生成 token
     *
     * @param deviceIdentified 设备唯一是被
     * @param request          http servlet request
     *
     * @return token
     */
    BuildToken generateToken(String deviceIdentified, HttpServletRequest request);

    /**
     * 生成验证码
     *
     * @param request http servlet request
     * @return 验证码结果
     * @throws Exception 生成错误时抛出
     */
    Object generateCaptcha(HttpServletRequest request) throws Exception;

    /**
     * 验证请求
     *
     * @param request http servlet 请求
     *
     * @return 验证结果集
     */
    RestResult<Map<String, Object>> verify(HttpServletRequest request);

    /**
     * 获取验证码类型
     *
     * @return 验证码类型
     */
    String getType();

    /**
     * 是否支持本次请求
     *
     * @param request http servlet request
     * @return true 是，否则 false
     */
    default boolean isSupport(HttpServletRequest request) {
        String type = request.getHeader(CaptchaProperties.DEFAULT_CAPTCHA_TYPE_HEADER_NAME);
        if (StringUtils.isEmpty(type)) {
            type = request.getParameter(CaptchaProperties.DEFAULT_CAPTCHA_TYPE_PARAM_NAME);
        }
        return getType().equals(type);
    }

    /**
     * 获取提交时的验证码参数名称
     *
     * @return 参数名称
     */
    String getCaptchaParamName();

    /**
     * 获取提交时的绑定 token 参数名称
     *
     * @return 名称
     */
    String getTokenParamName();

    /**
     * 获取当前存在的绑定 token
     *
     * @param token token 值
     *
     * @return 绑定 token
     */
    BuildToken getBuildToken(String token);

    /**
     * 获取拦截 token
     *
     * @param token 拦截 token 值
     *
     * @return 拦截 token
     */
    InterceptToken getInterceptToken(String token);

    /**
     * 获取当前存在的绑定 token
     *
     * @param request http servlet request
     *
     * @return 绑定 token
     */
    BuildToken getBuildToken(HttpServletRequest request);

    /**
     * 获取当前存在的拦截 token
     *
     * @param request http servlet request
     *
     * @return 绑定 token
     */
    InterceptToken getInterceptToken(HttpServletRequest request);

    /**
     * 保存绑定 token
     *
     * @param token token 值
     */
    void saveBuildToken(BuildToken token);

    /**
     * 保存拦截 token
     *
     * @param token token
     */
    void saveInterceptToken(InterceptToken token);

    /**
     * 创建拦截 token
     *
     * @param token 绑定 token
     *
     * @return 拦截 token
     */
    InterceptToken generateInterceptorToken(BuildToken token);

    /**
     * 验证拦截 token
     *
     * @param request http servlet request
     *
     * @return 验证结果集
     */
    RestResult<Map<String, Object>> verifyInterceptToken(HttpServletRequest request);

    /**
     * 删除验证码
     *
     * @param request http servlet request
     *
     * @return 删除结果集
     */
    RestResult<Map<String, Object>> delete(HttpServletRequest request);
}
