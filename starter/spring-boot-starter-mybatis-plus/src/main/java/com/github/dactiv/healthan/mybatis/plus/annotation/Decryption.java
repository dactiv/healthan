package com.github.dactiv.healthan.mybatis.plus.annotation;

import com.github.dactiv.healthan.mybatis.plus.CryptoNullClass;
import com.github.dactiv.healthan.mybatis.plus.DecryptService;

import java.lang.annotation.*;

/**
 * 解密，用于在需要解密字段内容时使用
 *
 * @author maurice.chen
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Decryption {

    /**
     * spring bean 名称
     *
     * @return spring bean 名称
     */
    String beanName() default "";

    /**
     * 解密服务
     *
     * @return 解密服务
     */
    Class<? extends DecryptService> serviceClass() default CryptoNullClass.class;
}
