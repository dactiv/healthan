package com.github.dactiv.healthan.mybatis.plus.annotation;

import com.github.dactiv.healthan.mybatis.plus.CryptoNullClass;
import com.github.dactiv.healthan.mybatis.plus.EncryptService;

import java.lang.annotation.*;

/**
 * 加密，用于在需要加密字段内容时使用
 *
 * @author maurice.chen
 */

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Encryption {

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
