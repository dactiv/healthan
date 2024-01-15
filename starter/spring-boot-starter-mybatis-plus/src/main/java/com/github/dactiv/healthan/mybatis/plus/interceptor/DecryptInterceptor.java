package com.github.dactiv.healthan.mybatis.plus.interceptor;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.ReflectionUtils;
import com.github.dactiv.healthan.crypto.algorithm.Base64;
import com.github.dactiv.healthan.crypto.algorithm.CodecUtils;
import com.github.dactiv.healthan.mybatis.plus.CryptoNullClass;
import com.github.dactiv.healthan.mybatis.plus.CryptoService;
import com.github.dactiv.healthan.mybatis.plus.DecryptService;
import com.github.dactiv.healthan.mybatis.plus.annotation.DecryptProperties;
import com.github.dactiv.healthan.mybatis.plus.annotation.Decryption;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.util.*;


/**
 * 解密内部拦截器实现
 *
 * @author maurice.chen
 */
@Intercepts(
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
        )
)
public class DecryptInterceptor implements Interceptor {

    private final ApplicationContext applicationContext;

    public DecryptInterceptor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object o = invocation.proceed();
        if (List.class.isAssignableFrom(o.getClass())) {
            List<Object> list = Casts.cast(o);
            Map<Class<?>, Map<CryptoService, List<Field>>> cacheEntity = new LinkedHashMap<>();
            for (Object item : list) {
                Map<CryptoService, List<Field>> cryptoServiceListMap = cacheEntity.computeIfAbsent(item.getClass(), k -> new LinkedHashMap<>());
                if (MapUtils.isEmpty(cryptoServiceListMap)) {
                    cryptoServiceListMap.putAll(getCryptoFields(item.getClass()));
                }
                decrypt(item, cryptoServiceListMap);
            }
        } else {
            Map<CryptoService, List<Field>> cryptoServiceListMap = getCryptoFields(o.getClass());
            decrypt(o, cryptoServiceListMap);
        }
        return o;
    }

    public void decrypt(Object entity, Map<CryptoService, List<Field>> fields) {
        for (Map.Entry<CryptoService, List<Field>> entry : fields.entrySet()) {
            DecryptService decryptService = Casts.cast(entry.getKey(), DecryptService.class);
            for (Field field : entry.getValue()) {
                Object value = ReflectionUtils.getFieldValue(entity, field);
                if (Objects.isNull(value) || !Base64.isBase64(CodecUtils.toBytes(value.toString()))) {
                    continue;
                }
                String text = decryptService.decrypt(value.toString());
                ReflectionUtils.setFieldValue(entity, field.getName(), text);
            }

        }
    }

    public DecryptService getDecryptService(String beanName, Class<? extends DecryptService> serviceClass) {

        DecryptService encryptService = null;
        if (Objects.nonNull(applicationContext)) {
            encryptService = EncryptInnerInterceptor.getCryptoService(applicationContext, serviceClass, beanName);
        }

        if (Objects.isNull(encryptService) && serviceClass != CryptoNullClass.class) {
            encryptService = BeanUtils.instantiateClass(serviceClass);
        }

        return encryptService;
    }

    public Map<CryptoService, List<Field>> getCryptoFields(Class<?> entityClass) {
        Map<CryptoService, List<Field>> result = new LinkedHashMap<>();
        if (Objects.isNull(entityClass)) {
            return result;
        }
        List<Field> fields = ReflectionUtils.findFields(entityClass);
        Set<DecryptProperties> decrypts = AnnotatedElementUtils.findAllMergedAnnotations(entityClass, DecryptProperties.class);

        if (CollectionUtils.isNotEmpty(decrypts)) {
            for (DecryptProperties properties : decrypts) {
                DecryptService decryptService = getDecryptService(properties.beanName(), properties.serviceClass());
                List<Field> fieldList = result.computeIfAbsent(decryptService, k -> new LinkedList<>());
                fields
                        .stream()
                        .filter(f -> ArrayUtils.contains(properties.value(), f.getName()))
                        .filter(f -> fieldList.stream().noneMatch(l -> StringUtils.equals(l.getName(), f.getName())))
                        .forEach(fieldList::add);
            }
        }

        for (Field field : fields) {
            Decryption decryption = AnnotatedElementUtils.findMergedAnnotation(field, Decryption.class);
            if (Objects.isNull(decryption)) {
                continue;
            }
            DecryptService decryptService = getDecryptService(decryption.beanName(), decryption.serviceClass());
            List<Field> fieldList = result.computeIfAbsent(decryptService, k -> new LinkedList<>());
            fieldList.add(field);
        }

        return result;

    }
}
