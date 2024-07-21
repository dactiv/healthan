package com.github.dactiv.healthan.spring.security.audit;

import java.lang.annotation.*;

/**
 * 数据操作留痕注解
 *
 * @author maurice.chen
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationDataTrace {

    /**
     * 目标名称
     *
     * @return 目标名称
     */
    String name();

    /**
     *
     * @return
     */
    String principal();
}
