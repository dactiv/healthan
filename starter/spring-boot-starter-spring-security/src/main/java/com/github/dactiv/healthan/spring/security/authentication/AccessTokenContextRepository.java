package com.github.dactiv.healthan.spring.security.authentication;

import com.github.dactiv.healthan.commons.CacheProperties;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.TimeProperties;
import com.github.dactiv.healthan.commons.id.IdEntity;
import com.github.dactiv.healthan.commons.id.number.NumberIdEntity;
import com.github.dactiv.healthan.crypto.CipherAlgorithmService;
import com.github.dactiv.healthan.crypto.algorithm.Base64;
import com.github.dactiv.healthan.crypto.algorithm.ByteSource;
import com.github.dactiv.healthan.crypto.algorithm.cipher.CipherService;
import com.github.dactiv.healthan.crypto.algorithm.exception.CryptoException;
import com.github.dactiv.healthan.security.audit.PluginAuditEvent;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.spring.security.authentication.config.AccessTokenProperties;
import com.github.dactiv.healthan.spring.security.authentication.config.AuthenticationProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.ExpiredToken;
import com.github.dactiv.healthan.spring.security.authentication.token.RefreshToken;
import com.github.dactiv.healthan.spring.security.authentication.token.SimpleAuthenticationToken;
import com.github.dactiv.healthan.spring.security.entity.AccessTokenDetails;
import com.github.dactiv.healthan.spring.security.entity.support.MobileSecurityPrincipal;
import com.github.dactiv.healthan.spring.web.device.DeviceUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.SerializationCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SaveContextOnUpdateOrErrorResponseWrapper;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.DigestUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

/**
 * 访问 token 上下文仓库实现，用于移动端用户明细登陆系统后，返回一个 token， 为无状态的 http 传输中，通过该 token 来完成认证授权等所有工作
 *
 * @author maurice.chen
 */
public class AccessTokenContextRepository extends HttpSessionSecurityContextRepository {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(AccessTokenContextRepository.class);

    private final RedissonClient redissonClient;

    private final AccessTokenProperties accessTokenProperties;

    private final AntPathRequestMatcher loginRequestMatcher;

    private final CipherAlgorithmService cipherAlgorithmService = new CipherAlgorithmService();

    public AccessTokenContextRepository(RedissonClient redissonClient,
                                        AccessTokenProperties accessTokenProperties,
                                        AuthenticationProperties authenticationProperties) {
        this.redissonClient = redissonClient;
        this.accessTokenProperties = accessTokenProperties;
        this.loginRequestMatcher = new AntPathRequestMatcher(
                authenticationProperties.getLoginProcessingUrl(),
                HttpMethod.POST.name()
        );
    }

    @Override
    public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
        HttpServletRequest request = requestResponseHolder.getRequest();
        SecurityContext securityContext = readSecurityContextFromRequest(request);

        if (Objects.isNull(securityContext)) {
            securityContext = super.loadContext(requestResponseHolder);
        }

        return securityContext;
    }

    private SecurityContext readSecurityContextFromRequest(HttpServletRequest request) {
        if (this.loginRequestMatcher.matches(request)) {
            return null;
        }
        String token = request.getHeader(accessTokenProperties.getAccessTokenHeaderName());
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter(accessTokenProperties.getAccessTokenParamName());
        }

        if (StringUtils.isEmpty(token)) {
            return null;
        }

        return getSecurityContext(token);
    }

    public SecurityContext getSecurityContext(String token) {
        
        if (StringUtils.isEmpty(token) || !Base64.isBase64(token.getBytes())) {
            return null;
        }

        CipherService cipherService = cipherAlgorithmService.getCipherService(accessTokenProperties.getCipherAlgorithmName());
        byte[] key = Base64.decode(accessTokenProperties.getCryptoKey());

        try {
            ByteSource byteSource = cipherService.decrypt(Base64.decode(token), key);
            String plaintext = new String(byteSource.obtainBytes(), Charset.defaultCharset());

            Map<String, Object> plaintextUserDetail = Casts.readValue(plaintext, Casts.MAP_TYPE_REFERENCE);
            if (MapUtils.isEmpty(plaintextUserDetail)) {
                return null;
            }

            String type = plaintextUserDetail.getOrDefault(PluginAuditEvent.TYPE_FIELD_NAME, StringUtils.EMPTY).toString();
            Object id = plaintextUserDetail.getOrDefault(IdEntity.ID_FIELD_NAME, StringUtils.EMPTY).toString();

            RBucket<SecurityContext> bucket = getSecurityContextBucket(type, id);
            SecurityContext context = bucket.get();
            if (Objects.isNull(context)) {
                return null;
            }

            if (!SimpleAuthenticationToken.class.isAssignableFrom(context.getAuthentication().getClass())) {
                return null;
            }

            SimpleAuthenticationToken authenticationToken = Casts.cast(context.getAuthentication());
            if (!AccessTokenDetails.class.isAssignableFrom(authenticationToken.getDetails().getClass())) {
                return null;
            }

            AccessTokenDetails accessTokenDetails = Casts.cast(authenticationToken.getDetails());
            if (Objects.isNull(accessTokenDetails)) {
                return null;
            }

            ExpiredToken accessToken = accessTokenDetails.getToken();
            if (Objects.isNull(accessToken)) {
                return null;
            }

            String existToken = accessToken.getToken();
            if (!StringUtils.equals(existToken, token)) {
                return null;
            }

            SecurityPrincipal principal = Casts.cast(authenticationToken.getPrincipal());
            if (principal instanceof MobileSecurityPrincipal) {
                MobileSecurityPrincipal mobileSecurityPrincipal = Casts.cast(principal);
                Object deviceIdentified = plaintextUserDetail.get(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME);
                if (!Objects.equals(deviceIdentified, mobileSecurityPrincipal.getDeviceIdentified())) {
                    return null;
                }
            }

            if (RefreshToken.class.isAssignableFrom(accessToken.getClass())) {
                return context;
            }

            TimeProperties expiresTime = accessTokenProperties.getAccessTokenCache().getExpiresTime();
            if (Objects.nonNull(expiresTime)) {
                bucket.expireAsync(expiresTime.toDuration());
            }

            return context;
        } catch (CryptoException e) {
            LOGGER.warn("通过密钥:{} 解密 token:{} 失败", accessTokenProperties.getCryptoKey(), token);
        }

        return null;
    }

    public RBucket<SecurityContext> getSecurityContextBucket(String type, Object deviceIdentified) {
        String key = getAccessTokenKey(type, deviceIdentified);
        return redissonClient.getBucket(key, new SerializationCodec());
    }

    public String getAccessTokenKey(String type, Object deviceIdentified) {
        return accessTokenProperties.getAccessTokenCache().getName(type + CacheProperties.DEFAULT_SEPARATOR + deviceIdentified);
    }

    public String generateCiphertext(SimpleAuthenticationToken token) {
        return generateCiphertext(token, accessTokenProperties.getCryptoKey());
    }

    public String generateCiphertext(SimpleAuthenticationToken token, String aesKey) {
        Map<String, Object> json = Casts.convertValue(token.toTypeUserDetails(), Casts.MAP_TYPE_REFERENCE);

        json.put(NumberIdEntity.CREATION_TIME_FIELD_NAME, System.currentTimeMillis());

        if (token.getPrincipal() instanceof MobileSecurityPrincipal) {
            MobileSecurityPrincipal mobileSecurityPrincipal = Casts.cast(token.getPrincipal());
            json.put(DeviceUtils.REQUEST_DEVICE_IDENTIFIED_PARAM_NAME, mobileSecurityPrincipal.getDeviceIdentified());
        }

        String plaintext = Casts.writeValueAsString(json);

        CipherService cipherService = cipherAlgorithmService.getCipherService(accessTokenProperties.getCipherAlgorithmName());
        byte[] key = Base64.decode(aesKey);
        ByteSource source = cipherService.encrypt(plaintext.getBytes(Charset.defaultCharset()), key);

        return source.getBase64();
    }

    @Override
    public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        SaveContextOnUpdateOrErrorResponseWrapper responseWrapper = WebUtils.getNativeResponse(response,
                SaveContextOnUpdateOrErrorResponseWrapper.class);
        if (Objects.nonNull(responseWrapper)) {
            super.saveContext(context, request, response);
        }

        saveRedissonSecurityContext(context, request, response);
    }

    /**
     * 删除缓存
     *
     * @param token 认证 token
     */
    public void deleteContext(SimpleAuthenticationToken token) {
        SecurityPrincipal principal = Casts.cast(token.getPrincipal());
        RBucket<SecurityContext> bucket = getSecurityContextBucket(token.getPrincipalType(), principal.getId());
        bucket.deleteAsync();
    }

    /**
     * 删除缓存
     *
     * @param type 用户类型
     * @param id 设备唯一识别
     */
    public void deleteContext(String type, Object id) {
        RBucket<SecurityContext> bucket = getSecurityContextBucket(type, id);
        bucket.deleteAsync();
    }

    protected void saveRedissonSecurityContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
        if (Objects.isNull(context.getAuthentication()) || !context.getAuthentication().isAuthenticated()) {
            return;
        }

        Authentication authentication = context.getAuthentication();

        if (!SimpleAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            return;
        }

        SimpleAuthenticationToken authenticationToken = Casts.cast(authentication);
        Object details = authenticationToken.getDetails();
        if (!AccessTokenDetails.class.isAssignableFrom(details.getClass())) {
            return ;
        }

        AccessTokenDetails accessTokenDetails = Casts.cast(details);
        ExpiredToken accessTokenValue = accessTokenDetails.getToken();
        if (Objects.isNull(accessTokenValue)) {
            return ;
        }

        if (RefreshToken.class.isAssignableFrom(accessTokenValue.getClass())) {
            saveAccessToken(Casts.cast(accessTokenValue));
        }

        SecurityPrincipal principal = Casts.cast(authenticationToken.getPrincipal());
        RBucket<SecurityContext> bucket = getSecurityContextBucket(authenticationToken.getPrincipalType(), principal.getId());
        TimeProperties timeProperties = accessTokenProperties.getAccessTokenCache().getExpiresTime();
        if (Objects.nonNull(timeProperties)) {
            bucket.setAsync(context, timeProperties.getValue(), timeProperties.getUnit());
        } else {
            bucket.setAsync(context);
        }
    }

    private void saveAccessToken(RefreshToken refreshToken) {

        RBucket<ExpiredToken> refreshTokenBucket = getAccessTokenBucket(
                refreshToken.getToken(),
                accessTokenProperties.getRefreshTokenCache()
        );
        TimeProperties refreshExpiresTime = refreshToken.getExpiresTime();
        if (Objects.nonNull(refreshExpiresTime)) {
            refreshTokenBucket.setAsync(refreshToken, refreshExpiresTime.getValue(), refreshExpiresTime.getUnit());
        } else {
            refreshTokenBucket.setAsync(refreshToken);
        }

        RBucket<ExpiredToken> accessTokenBucket = getAccessTokenBucket(
                refreshToken.getToken(),
                accessTokenProperties.getAccessTokenCache()
        );
        TimeProperties accessTokenExpiresTime = refreshToken.getAccessToken().getExpiresTime();
        if (Objects.nonNull(accessTokenExpiresTime)) {
            accessTokenBucket.setAsync(
                    refreshToken.getAccessToken(),
                    accessTokenExpiresTime.getValue(),
                    accessTokenExpiresTime.getUnit()
            );
        } else {
            accessTokenBucket.setAsync(refreshToken.getAccessToken());
        }

    }

    private RBucket<ExpiredToken> getAccessTokenBucket(String tokenValue, CacheProperties cacheProperties) {
        String md5 = DigestUtils.md5DigestAsHex(tokenValue.getBytes());
        return redissonClient.getBucket(cacheProperties.getName(md5));
    }

    @Override
    public boolean containsContext(HttpServletRequest request) {
        boolean superValue = super.containsContext(request);
        SecurityContext context = readSecurityContextFromRequest(request);
        return superValue || Objects.nonNull(context);
    }
}
