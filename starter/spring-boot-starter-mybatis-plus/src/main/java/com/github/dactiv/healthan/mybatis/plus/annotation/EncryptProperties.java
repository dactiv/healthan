package com.github.dactiv.healthan.mybatis.plus.annotation;

import com.github.dactiv.healthan.mybatis.plus.CryptoNullClass;
import com.github.dactiv.healthan.mybatis.plus.EncryptService;

import java.lang.annotation.*;

/**
 * 加密属性配置
 *
 * @author maurice.chen
 */
@Documented
@Repeatable(Encrypts.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
public @interface EncryptProperties {

    String[] value();

    /**
     * spring bean 名称
     *
     * @return spring bean 名称
     */
    String beanName() default "";

    /**
     * 加密服务
     *
     * @return 加密服务
     */
    Class<? extends EncryptService> serviceClass() default CryptoNullClass.class;
}
