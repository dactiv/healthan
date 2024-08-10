package com.github.dactiv.healthan.mybatis.plus.annotation;

import com.github.dactiv.healthan.mybatis.plus.interceptor.DataOwnerInterceptor;
import com.github.dactiv.healthan.mybatis.plus.service.DataOwnerService;

import java.lang.annotation.*;

/**
 * 拥有者注解，用于获取数据时，动态加入该注解所在的字段信息去跟当前执行获取动作的执行人进行对比，形成指定的 where 条件来帮助完成数据权限业务
 *
 * @see DataOwnerInterceptor
 * @author maurice.chen
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataOwner {

    /**
     * 字段名称
     *
     * @return 字段名称
     */
    String fieldName();

    /**
     * 空值是否也带条件，根据 {@link DataOwnerService#getOwner()} 获取实际哟个有者信息，如果获取出来的值为空，是否还要增加响应关联语句。 默认为 false
     *
     * @return true 是，否则 false
     */
    boolean emptyValueExecute() default false;
}
