package com.github.dactiv.healthan.mybatis.interceptor.audit;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.mybatis.enumerate.OperationDataType;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 内存形式的操作数据留痕仓库实现
 *
 * @author maurice.chen
 */
public abstract class AbstractOperationDataTraceResolver implements OperationDataTraceResolver {

    public static final String DEFAULT_DATE_FORMATTER_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final DateFormat dateFormat;

    public AbstractOperationDataTraceResolver() {
        dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMATTER_PATTERN);
    }

    public AbstractOperationDataTraceResolver(String dateFormatPattern) {
        this.dateFormat = new SimpleDateFormat(dateFormatPattern);
    }

    @Override
    public List<OperationDataTraceRecord> createOperationDataTraceRecord(MappedStatement mappedStatement, Statement statement, Object parameter) throws Exception{

        if (statement instanceof Insert) {
            Insert insert = Casts.cast(statement);
            return createInsertRecord(insert, mappedStatement, statement, parameter);
        } else if (statement instanceof Update) {
            Update update = Casts.cast(statement);
            return createUpdateRecord(update, mappedStatement, statement, parameter);
        } else if (statement instanceof Delete) {
            Delete delete = Casts.cast(statement);
            return createDeleteRecord(delete, mappedStatement, statement, parameter);
        }

        return new LinkedList<>();
    }

    protected List<OperationDataTraceRecord> createDeleteRecord(Delete delete, MappedStatement mappedStatement, Statement statement, Object parameter) throws Exception {
        OperationDataTraceRecord result = createBasicOperationDataTraceRecord(
                OperationDataType.DELETE,
                delete.getTable().getName(),
                Casts.convertValue(parameter, Casts.MAP_TYPE_REFERENCE)
        );
        return Collections.singletonList(result);
    }

    protected List<OperationDataTraceRecord> createUpdateRecord(Update update, MappedStatement mappedStatement, Statement statement, Object parameter) throws Exception {
        OperationDataTraceRecord result = createBasicOperationDataTraceRecord(
                OperationDataType.UPDATE,
                update.getTable().getName(),
                Casts.convertValue(parameter, Casts.MAP_TYPE_REFERENCE)
        );
        return Collections.singletonList(result);
    }

    protected List<OperationDataTraceRecord> createInsertRecord(Insert insert,
                                                                MappedStatement mappedStatement,
                                                                Statement statement,
                                                                Object parameter) throws Exception {
        OperationDataTraceRecord result = createBasicOperationDataTraceRecord(
                OperationDataType.INSERT,
                insert.getTable().getName(),
                Casts.convertValue(parameter, Casts.MAP_TYPE_REFERENCE)
        );

        return Collections.singletonList(result);
    }

    protected OperationDataTraceRecord createBasicOperationDataTraceRecord(OperationDataType type,
                                                                           String target,
                                                                           Map<String, Object> submitData) throws UnknownHostException {
        OperationDataTraceRecord record = new OperationDataTraceRecord();

        record.setPrincipal(InetAddress.getLocalHost().getHostAddress());
        record.setType(type);
        record.setTarget(target);
        record.setSubmitData(submitData);
        record.setRemark(record.getPrincipal() + StringUtils.SPACE + dateFormat.format(record.getCreationTime()) +  StringUtils.SPACE + record.getType().getName());

        return record;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }
}
