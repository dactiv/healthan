package com.github.dactiv.healthan.commons.annotation;

import java.lang.annotation.*;

/**
 * 忽略字段注解
 *
 * @author maurice.chen
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IgnoreField {

}
