package com.github.dactiv.healthan.mybatis.plus.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.ReflectionUtils;
import com.github.dactiv.healthan.mybatis.plus.annotation.DataOwner;
import com.github.dactiv.healthan.mybatis.plus.service.BasicService;
import com.github.dactiv.healthan.mybatis.plus.service.DataOwnerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 拥有者拦截器，用于实现在 sql 查询前，动态将当前执行人的数据动态添加到 where 条件忠，实现数据权限业务
 *
 * @author maurice.chen
 */
public class DataOwnerInterceptor implements InnerInterceptor {

    private boolean wrapperMode = false;
    private boolean snakeCase = true;
    private DataOwnerService dataOwnerService;

    public DataOwnerInterceptor(boolean wrapperMode, boolean snakeCase, DataOwnerService dataOwnerService) {
        this.wrapperMode = wrapperMode;
        this.snakeCase = snakeCase;
        this.dataOwnerService = dataOwnerService;
    }

    public DataOwnerInterceptor() {
    }

    @Override
    public void beforeQuery(Executor executor,
                            MappedStatement ms,
                            Object parameter,
                            RowBounds rowBounds,
                            ResultHandler resultHandler,
                            BoundSql boundSql) throws SQLException {
        if (!Map.class.isAssignableFrom(parameter.getClass())) {
            return ;
        }

        Map<String, Object> map = Casts.cast(parameter);
        Object ew = map.getOrDefault(Constants.WRAPPER, null);
        if (Objects.isNull(ew)) {
            return ;
        }
        Object et = map.getOrDefault(Constants.ENTITY, null);

        Class<?> entityClass = null;
        if (Objects.nonNull(et)) {
            entityClass = et.getClass();
        } else if (wrapperMode && map.entrySet().stream().anyMatch(t -> Objects.equals(t.getKey(), Constants.WRAPPER))) {
            entityClass = BasicService.getEntityClass(ms.getId());
        }

        if (Objects.isNull(entityClass)) {
            return ;
        }

        DataOwner dataOwner = AnnotatedElementUtils.findMergedAnnotation(entityClass, DataOwner.class);
        if (Objects.isNull(dataOwner)) {
            return ;
        }

        Optional<Field> optional = ReflectionUtils
                .findFields(entityClass)
                .stream().filter(f -> f.getName().equals(dataOwner.fieldName()))
                .findFirst();
        if (!optional.isPresent()) {
            return ;
        }

        String ownerValue = dataOwnerService.getOwner();
        if (StringUtils.isEmpty(ownerValue) && !dataOwner.emptyValueExecute()) {
            return ;
        }

        QueryWrapper<?> queryWrapper = Casts.cast(ew);
        if (snakeCase) {
            queryWrapper.eq(Casts.castCamelCaseToSnakeCase(optional.get().getName()), ownerValue);
        } else {
            queryWrapper.eq(optional.get().getName(), ownerValue);
        }

    }

}
