package com.github.dactiv.healthan.mybatis.interceptor.audit;

import com.github.dactiv.healthan.commons.Casts;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.util.Arrays;
import java.util.List;

@Intercepts(
        @Signature(
                type = Executor.class,
                method = "update",
                args = {MappedStatement.class, Object.class}
        )
)
public class OperationDataTraceInterceptor implements Interceptor {

    public static final String REMOVE_ESCAPE_REG = "\\\\.|\\n|\\t";

    private static final List<SqlCommandType> SQL_COMMAND_TYPES = Arrays.asList(SqlCommandType.INSERT, SqlCommandType.UPDATE, SqlCommandType.DELETE);

    private final OperationDataTraceResolver operationDataTraceResolver;

    public OperationDataTraceInterceptor(OperationDataTraceResolver operationDataTraceResolver) {
        this.operationDataTraceResolver = operationDataTraceResolver;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();

        if (!Integer.class.isAssignableFrom(result.getClass())) {
            return result;
        }

        // 如果影响行号不大于 0 什么都不做.
        Integer affectedNumber = Casts.cast(result);
        if ( affectedNumber <= 0) {
            return result;
        }

        MappedStatement mappedStatement = Casts.cast(invocation.getArgs()[0]);
        if (!SQL_COMMAND_TYPES.contains(mappedStatement.getSqlCommandType())) {
            return result;
        }

        Object parameter = invocation.getArgs()[1];
        BoundSql boundSql = mappedStatement.getSqlSource().getBoundSql(parameter);
        String sql = RegExUtils.replaceAll(boundSql.getSql(), REMOVE_ESCAPE_REG, StringUtils.SPACE);

        Statement statement = CCJSqlParserUtil.parse(sql);

        List<OperationDataTraceRecord> records = operationDataTraceResolver.createOperationDataTraceRecord(mappedStatement, statement, parameter);

        if (CollectionUtils.isNotEmpty(records)) {
            operationDataTraceResolver.saveOperationDataTraceRecord(records);
        }

        return result;
    }

}
