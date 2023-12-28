package com.github.dactiv.healthan.mybatis.plus.interceptor;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.ReflectionUtils;
import com.github.dactiv.healthan.crypto.algorithm.Base64;
import com.github.dactiv.healthan.crypto.algorithm.CodecUtils;
import com.github.dactiv.healthan.mybatis.plus.CryptoNullClass;
import com.github.dactiv.healthan.mybatis.plus.CryptoService;
import com.github.dactiv.healthan.mybatis.plus.DecryptService;
import com.github.dactiv.healthan.mybatis.plus.EncryptService;
import com.github.dactiv.healthan.mybatis.plus.annotation.DecryptProperties;
import com.github.dactiv.healthan.mybatis.plus.annotation.Decryption;
import com.github.dactiv.healthan.mybatis.plus.annotation.EncryptProperties;
import com.github.dactiv.healthan.mybatis.plus.annotation.Encryption;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 加解密拦截器实现
 *
 * @author maurice.chen
 */
public class CryptoInterceptor implements InnerInterceptor {

    public static final List<SqlCommandType> SUPPORT_COMMANDS = Arrays.asList(SqlCommandType.UPDATE, SqlCommandType.INSERT, SqlCommandType.SELECT);

    /**
     * entity类缓存
     */
    private static final Map<String, Class<?>> ENTITY_CLASS_CACHE = new ConcurrentHashMap<>();

    private boolean wrapperMode = false;

    private ApplicationContext applicationContext;

    public CryptoInterceptor() {
    }

    public CryptoInterceptor(boolean wrapperMode) {
        this.wrapperMode = wrapperMode;
    }

    public CryptoInterceptor(boolean wrapperMode, ApplicationContext applicationContext) {
        this.wrapperMode = wrapperMode;
        this.applicationContext = applicationContext;
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) {
        if (!SUPPORT_COMMANDS.contains(ms.getSqlCommandType())) {
            return;
        }
        if (parameter instanceof Map) {
            Map<String, Object> map = Casts.cast(parameter);
            crypto(map, ms.getId(), ms.getSqlCommandType());
        } else {
            Map<CryptoService, List<Field>> fields = this.getCryptoFields(parameter.getClass(), ms.getSqlCommandType());
            if (MapUtils.isEmpty(fields)) {
                return;
            }
            this.doCrypto(parameter, fields, ms.getSqlCommandType());
        }
    }

    private void crypto(Map<String, Object> map, String id, SqlCommandType sqlCommandType) {
        Object et = map.getOrDefault(Constants.ENTITY, null);
        if (Objects.nonNull(et)) {

            Map<CryptoService, List<Field>> fields = this.getCryptoFields(et.getClass(), sqlCommandType);
            if (MapUtils.isEmpty(fields)) {
                return;
            }
            this.doCrypto(et, fields, sqlCommandType);
        } else if (wrapperMode && map.entrySet().stream().anyMatch(t -> Objects.equals(t.getKey(), Constants.WRAPPER))) {
            // update(LambdaUpdateWrapper) or update(UpdateWrapper)
            this.doCrypto(map, id, sqlCommandType);
        }
    }

    private void doCrypto(Object entity, Map<CryptoService, List<Field>> fields, SqlCommandType sqlCommandType) {
        if (SqlCommandType.SELECT.equals(sqlCommandType)) {
            for (Map.Entry<CryptoService, List<Field>> entry : fields.entrySet()) {
                DecryptService decryptService = Casts.cast(entry.getKey(), DecryptService.class);
                for (Field field : entry.getValue()) {
                    Object value = ReflectionUtils.getFieldValue(entity, field);
                    if (Objects.isNull(value)) {
                        continue;
                    }
                    String text = decryptService.decrypt(value.toString());
                    ReflectionUtils.setFieldValue(entity, field.getName(), text);
                }

            }
        } else {
            for (Map.Entry<CryptoService, List<Field>> entry : fields.entrySet()) {
                EncryptService encryptService = Casts.cast(entry.getKey(), EncryptService.class);
                for (Field field : entry.getValue()) {
                    Object value = ReflectionUtils.getFieldValue(entity, field);
                    if (Objects.isNull(value) || Base64.isBase64(CodecUtils.toBytes(value.toString()))) {
                        continue;
                    }
                    String text = encryptService.encrypt(value.toString());
                    ReflectionUtils.setFieldValue(entity, field.getName(), text);
                }

            }
        }
    }

    private void doCrypto(Map<String, Object> map, String id, SqlCommandType sqlCommandType) {
        Object ew = map.get(Constants.WRAPPER);

        if (Objects.isNull(ew) || !AbstractWrapper.class.isAssignableFrom(ew.getClass())) {
            return ;
        }

        /*if (ew instanceof Update) {
            Update updateWrapper = Casts.cast(ew);
            Class<?> entityClass = ENTITY_CLASS_CACHE.get(id);
            if (null == entityClass) {
                try {
                    final String className = id.substring(0, id.lastIndexOf('.'));
                    entityClass = ReflectionKit.getSuperClassGenericType(Class.forName(className), Mapper.class, 0);
                    ENTITY_CLASS_CACHE.put(id, entityClass);
                } catch (ClassNotFoundException e) {
                    throw ExceptionUtils.mpe(e);
                }
            }
            Map<CryptoService, List<Field>> fields = this.getCryptoFields(entityClass, SqlCommandType.UPDATE);
            this.doCrypto(et, fields, sqlCommandType);
            //List<Field> fields = this.getCryptoFields(entityClass, SqlCommandType.UPDATE);
            //fields.forEach(f -> updateWrapper.set(Casts.toSnakeCase(f.getName()), doCrypto(f)));
        } else if (ew instanceof Query) {

        }*/

    }

    public EncryptService getEncryptService(String beanName, Class<? extends EncryptService> serviceClass) {
        EncryptService encryptService = null;
        if (Objects.nonNull(applicationContext)) {
            encryptService = getCryptoService(serviceClass, beanName);
        }

        if (Objects.isNull(encryptService) && serviceClass != CryptoNullClass.class) {
            encryptService = BeanUtils.instantiateClass(serviceClass);
        }

        return encryptService;
    }

    public DecryptService getDecryptService(String beanName, Class<? extends DecryptService> serviceClass) {

        DecryptService encryptService = null;
        if (Objects.nonNull(applicationContext)) {
            encryptService = getCryptoService(serviceClass, beanName);
        }

        if (Objects.isNull(encryptService) && serviceClass != CryptoNullClass.class) {
            encryptService = BeanUtils.instantiateClass(serviceClass);
        }

        return encryptService;
    }

    public <T extends CryptoService> T getCryptoService(Class<T> cryptoService, String beanName) {

        T result = null;

        if (StringUtils.isNotEmpty(beanName) && CryptoNullClass.class != cryptoService) {
            result = applicationContext.getBean(beanName, cryptoService);
        } else if (StringUtils.isNotEmpty(beanName)) {
            Object bean = applicationContext.getBean(beanName);
            if (!DecryptService.class.isAssignableFrom(bean.getClass())) {
                return null;
            }
            result = Casts.cast(bean);
        } else if (CryptoNullClass.class != cryptoService) {
            result = applicationContext.getBean(cryptoService);
        }

        return result;
    }

    private Map<CryptoService, List<Field>> getCryptoFields(Class<?> entityClass, SqlCommandType sqlCommandType) {
        List<Field> fields = ReflectionUtils.findFields(entityClass);
        Map<CryptoService, List<Field>> result = new LinkedHashMap<>();
        if (sqlCommandType == SqlCommandType.SELECT) {
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
        } else {
            Set<EncryptProperties> decrypts = AnnotatedElementUtils.findAllMergedAnnotations(entityClass, EncryptProperties.class);

            if (CollectionUtils.isNotEmpty(decrypts)) {
                for (EncryptProperties properties : decrypts) {
                    EncryptService encryptService = getEncryptService(properties.beanName(), properties.serviceClass());
                    List<Field> fieldList = result.computeIfAbsent(encryptService, k -> new LinkedList<>());
                    fields
                            .stream()
                            .filter(f -> ArrayUtils.contains(properties.value(), f.getName()))
                            .filter(f -> fieldList.stream().noneMatch(l -> StringUtils.equals(l.getName(), f.getName())))
                            .forEach(fieldList::add);
                }
            }

            for (Field field : fields) {
                Encryption encryption = AnnotatedElementUtils.findMergedAnnotation(field, Encryption.class);
                if (Objects.isNull(encryption)) {
                    continue;
                }
                EncryptService encryptService = getEncryptService(encryption.beanName(), encryption.serviceClass());
                List<Field> fieldList = result.computeIfAbsent(encryptService, k -> new LinkedList<>());
                fieldList.add(field);
            }
        }

        return result;

    }
}
