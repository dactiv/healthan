package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.crypto.algorithm.Base64;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.security.entity.TypeUserDetails;
import com.github.dactiv.healthan.spring.security.authentication.config.RememberMeProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.RememberMeAuthenticationToken;
import com.github.dactiv.healthan.spring.security.authentication.token.SimpleAuthenticationToken;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * cookie 形式的记住我实现
 *
 * @author maurice.chen
 */
public class CookieRememberService implements RememberMeServices {

    private final RememberMeProperties rememberMeProperties;

    private final RedissonClient redissonClient;

    public CookieRememberService(RememberMeProperties rememberMeProperties, RedissonClient redissonClient) {
        this.rememberMeProperties = rememberMeProperties;
        this.redissonClient = redissonClient;
    }

    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        String rememberMeCookie = getRememberMeCookieValue(request);

        if (StringUtils.isBlank(rememberMeCookie)) {
            return null;
        }

        return getTokenValue(rememberMeCookie);
    }

    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {
        removeCookie(request, response);
    }

    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        if (!SimpleAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            return ;
        }

        SimpleAuthenticationToken authenticationToken = Casts.cast(authentication);
        if (!authenticationToken.isRememberMe()) {
            return;
        }

        if (!SecurityPrincipal.class.isAssignableFrom(authentication.getDetails().getClass())) {
            return;
        }

        SecurityPrincipal principal = Casts.cast(authentication.getDetails());
        String rememberMeToken = principal.toTypeUserDetails().toUniqueValue()
                + CacheProperties.DEFAULT_SEPARATOR
                + System.currentTimeMillis();
        RBucket<RememberMeAuthenticationToken> bucket = getRememberMeTokenBucket(principal.toTypeUserDetails());

        RememberMeAuthenticationToken rememberMeAuthenticationToken = new RememberMeAuthenticationToken(
                principal.getUsername(),
                rememberMeToken,
                principal.getType()
        );
        bucket.set(rememberMeAuthenticationToken);
        if (Objects.nonNull(rememberMeProperties.getCache().getExpiresTime())) {
            bucket.expireAsync(rememberMeProperties.getCache().getExpiresTime().toDuration());
        }

        int maxAge = (int) rememberMeProperties.getCache().getExpiresTime().toSeconds();

        setCookie(rememberMeAuthenticationToken, maxAge, request, response);
    }

    /**
     * 获取记住我桶信息
     *
     * @param userDetails 用户明细
     *
     * @return 记住我桶信息
     */
    public RBucket<RememberMeAuthenticationToken> getRememberMeTokenBucket(TypeUserDetails<Object> userDetails) {
        String key = rememberMeProperties.getCache().getName(userDetails.toUniqueValue());
        return redissonClient.getBucket(key);
    }

    /**
     * 删除记住我 token
     *
     * @param userDetails 用户明细
     */
    public void deleteRememberMeToken(TypeUserDetails<Object> userDetails) {
        RBucket<RememberMeAuthenticationToken> bucket = getRememberMeTokenBucket(userDetails);
        bucket.deleteAsync();
    }

    /**
     * 获取记住我的 cookie 值
     *
     * @param request http 请求
     *
     * @return 记住我的 cookie 值
     */
    protected String getRememberMeCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (ArrayUtils.isEmpty(cookies)) {
            return null;
        }

        Optional<Cookie> optional = Arrays
                .stream(cookies)
                .filter(c -> c.getName().equals(rememberMeProperties.getCookie().getName()))
                .findFirst();

        return optional.map(Cookie::getValue).orElse(null);
    }

    /**
     * 设置 cookie 内容
     *
     * @param token    token 信息
     * @param maxAge   过期时间
     * @param request  http 请求信息
     * @param response http 请求信息
     */
    protected void setCookie(RememberMeAuthenticationToken token, int maxAge, HttpServletRequest request, HttpServletResponse response) {
        String cookieValue = encodeTokenValue(token);

        Cookie cookie = createCookie(request);

        cookie.setMaxAge(maxAge);
        cookie.setValue(cookieValue);

        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }

    /**
     * 编码 token 值
     *
     * @param token token 信息
     *
     * @return 编码后的 token 值
     */
    private String encodeTokenValue(RememberMeAuthenticationToken token) {

        String json = Casts.writeValueAsString(token);

        return rememberMeProperties.isBase64Value()
                ? Base64.encodeToString(json.getBytes(Charset.defaultCharset()))
                : json;
    }

    /**
     * 获取获取 token 值，并转换为对象
     *
     * @param token token 值
     *
     * @return 记住我 token
     */
    private RememberMeAuthenticationToken getTokenValue(String token) {

        if (rememberMeProperties.isBase64Value()) {
            token = Base64.decodeToString(token);
        }

        return Casts.readValue(token, RememberMeAuthenticationToken.class);
    }

    /**
     * 创建一个新的 cookie
     *
     * @param request http 请求信息
     *
     * @return cookie
     */
    protected Cookie createCookie(HttpServletRequest request) {
        Cookie cookie = new Cookie(rememberMeProperties.getCookie().getName(), null);
        cookie.setPath(getCookiePath(request));

        if (StringUtils.isNotEmpty(rememberMeProperties.getCookie().getDomain())) {
            cookie.setDomain(rememberMeProperties.getCookie().getDomain());
        }

        if (rememberMeProperties.getCookie().getSecure() == null) {
            cookie.setSecure(request.isSecure());
        } else {
            cookie.setSecure(rememberMeProperties.getCookie().getSecure());
        }

        return cookie;
    }

    /**
     * 删除 cookie
     *
     * @param request  http 请求信息
     * @param response http 响应信息
     */
    protected void removeCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = createCookie(request);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * 获取 cookie 路径
     *
     * @param request http 请求信息
     *
     * @return cookie 路径
     */
    private String getCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return !contextPath.isEmpty() ? contextPath : AntPathMatcher.DEFAULT_PATH_SEPARATOR;
    }
}
