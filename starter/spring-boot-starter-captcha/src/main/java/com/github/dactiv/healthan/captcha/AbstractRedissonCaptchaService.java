package com.github.dactiv.healthan.captcha;

import com.github.dactiv.healthan.commons.CacheProperties;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 抽象的 redis 验证码服务实现
 *
 * @author maurice
 */
// FIXME 这个抽象类换成存储类，由具体实现去执行存储流程
public abstract class AbstractRedissonCaptchaService<B> extends AbstractCaptchaService<B> {

    protected RedissonClient redissonClient;

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void saveBuildToken(BuildToken token) {
        RBucket<BuildToken> bucket = getBuildTokenBucket(token.getToken().getName());
        bucket.setAsync(token, token.getToken().getExpiresTime().getValue(), token.getToken().getExpiresTime().getUnit());
    }

    @Override
    public void saveInterceptToken(InterceptToken token) {
        RBucket<InterceptToken> bucket = getInterceptTokenBucket(token.getToken().getName());
        bucket.setAsync(token, token.getToken().getExpiresTime().getValue(), token.getToken().getExpiresTime().getUnit());
    }

    /**
     * 删除绑定 token
     *
     * @param buildToken 绑定 token
     */
    protected void deleteBuildToken(BuildToken buildToken) {
        getBuildTokenBucket(buildToken.getToken().getName()).deleteAsync();
        if (Objects.nonNull(buildToken.getInterceptToken())) {
            deleteInterceptToken(buildToken.getInterceptToken());
        }
    }

    /**
     * 删除拦截 token
     *
     * @param interceptToken 拦截 token
     */
    protected void deleteInterceptToken(InterceptToken interceptToken) {
        RBucket<InterceptToken> bucket = getInterceptTokenBucket(interceptToken.getToken().getName());
        bucket.deleteAsync();
    }

    /**
     * 获取存储在 redis 的生成验证码的 token key 名称
     *
     * @param token token 值
     * @return key 名称
     */
    public String getBuildTokenKey(String token) {
        String name = getType() + CacheProperties.DEFAULT_SEPARATOR + token;
        return captchaProperties.getBuildTokenCache().getName(name);
    }

    /**
     * 获取绑定 token 桶
     *
     * @param token token 值
     * @return 绑定 token 桶
     */
    public RBucket<BuildToken> getBuildTokenBucket(String token) {
        return redissonClient.getBucket(getBuildTokenKey(token));
    }

    /**
     * 获取存储在 redis 的生成验证码的 token key 名称
     *
     * @param token token 值
     * @return key 名称
     */
    public String getInterceptTokenKey(String token) {
        String name = getType() + CacheProperties.DEFAULT_SEPARATOR + token;
        return captchaProperties.getInterceptorTokenCache().getName(name);
    }

    /**
     * 获取绑定 token 桶
     *
     * @param token token 值
     * @return 绑定 token 桶
     */
    public RBucket<InterceptToken> getInterceptTokenBucket(String token) {
        return redissonClient.getBucket(getInterceptTokenKey(token));
    }

    /**
     * 获取绑定 token
     *
     * @param token token 值
     * @return 绑定 token
     */
    @Override
    public BuildToken getBuildToken(String token) {
        RBucket<BuildToken> bucket = getBuildTokenBucket(token);
        return bucket.get();
    }

    @Override
    public InterceptToken getInterceptToken(String token) {
        RBucket<InterceptToken> bucket = getInterceptTokenBucket(token);
        return bucket.get();
    }

    @Override
    public BuildToken getBuildToken(HttpServletRequest request) {
        String token = request.getParameter(getTokenParamName());
        return getBuildToken(token);
    }

    @Override
    public InterceptToken getInterceptToken(HttpServletRequest request) {
        String token = request.getParameter(getTokenParamName());
        return getInterceptToken(token);
    }
}
