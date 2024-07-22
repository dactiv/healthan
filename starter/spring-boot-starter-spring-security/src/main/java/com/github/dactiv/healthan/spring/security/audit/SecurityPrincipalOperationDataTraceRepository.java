package com.github.dactiv.healthan.spring.security.audit;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.id.number.NumberIdEntity;
import com.github.dactiv.healthan.mybatis.interceptor.audit.OperationDataTraceRecord;
import com.github.dactiv.healthan.mybatis.plus.audit.MybatisPlusOperationDataTraceRepository;
import com.github.dactiv.healthan.mybatis.plus.config.OperationDataTraceProperties;
import com.github.dactiv.healthan.spring.security.audit.config.ControllerAuditProperties;
import com.github.dactiv.healthan.spring.security.authentication.token.AuthenticationSuccessToken;
import com.github.dactiv.healthan.spring.security.entity.SecurityPrincipalOperationDataTraceRecord;
import com.github.dactiv.healthan.spring.web.mvc.SpringMvcUtils;
import net.sf.jsqlparser.statement.Statement;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户明细操作数据留痕仓库实现
 *
 * @author maurice.chen
 */
public class SecurityPrincipalOperationDataTraceRepository extends MybatisPlusOperationDataTraceRepository {

    private final ControllerAuditProperties controllerAuditProperties;

    public SecurityPrincipalOperationDataTraceRepository(OperationDataTraceProperties operationDataTraceProperties,
                                                         ControllerAuditProperties controllerAuditProperties) {
        super(operationDataTraceProperties);
        this.controllerAuditProperties = controllerAuditProperties;
    }

    @Override
    public List<OperationDataTraceRecord> createOperationDataTraceRecord(MappedStatement mappedStatement, Statement statement, Object parameter) throws Exception {

        Optional<HttpServletRequest> optional = SpringMvcUtils.getHttpServletRequest();

        if (!optional.isPresent()) {
            return null;
        }
        HttpServletRequest httpServletRequest = optional.get();
        Object trace = httpServletRequest.getAttribute(ControllerAuditHandlerInterceptor.OPERATION_DATA_TRACE_ATT_NAME);

        if (Objects.isNull(trace) || Boolean.FALSE.equals(trace)) {
            return null;
        }

        SecurityContext context = SecurityContextHolder.getContext();
        if (Objects.isNull(context.getAuthentication())) {
            return null;
        }

        Authentication authentication = context.getAuthentication();
        if (!authentication.isAuthenticated()) {
            return null;
        }

        if (!AuthenticationSuccessToken.class.isAssignableFrom(context.getAuthentication().getClass())) {
            return null;
        }

        AuthenticationSuccessToken authenticationToken = Casts.cast(context.getAuthentication());

        List<OperationDataTraceRecord> records = super.createOperationDataTraceRecord(
                mappedStatement,
                statement,
                parameter
        );

        List<OperationDataTraceRecord> result = new LinkedList<>();

        for (OperationDataTraceRecord record : records.stream().filter(Objects::nonNull).collect(Collectors.toList())) {

            SecurityPrincipalOperationDataTraceRecord traceRecord = Casts.of(record, SecurityPrincipalOperationDataTraceRecord.class);
            if (Objects.isNull(traceRecord)) {
                continue;
            }

            Object auditType = httpServletRequest.getAttribute(controllerAuditProperties.getAuditTypeAttrName());
            if (Objects.nonNull(auditType)) {
                traceRecord.setControllerAuditType(auditType.toString());
            }

            traceRecord.setPrincipal(authenticationToken);
            traceRecord.setRemark(authenticationToken.getName() + StringUtils.SPACE + getDateFormat().format(record.getCreationTime()) + StringUtils.SPACE + record.getType().getName());

            result.add(traceRecord);
        }

        return result;
    }

    @Override
    public AuditEvent createAuditEvent(OperationDataTraceRecord record) {
        AuditEvent event = super.createAuditEvent(record);
        if (!SecurityPrincipalOperationDataTraceRecord.class.isAssignableFrom(record.getClass())) {
            return event;
        }

        SecurityPrincipalOperationDataTraceRecord dataTraceRecord = Casts.cast(record);
        AuthenticationSuccessToken authenticationToken = Casts.cast(dataTraceRecord.getPrincipal());

        Map<String, Object> dataTraceRecordMap = Casts.convertValue(dataTraceRecord, Casts.MAP_TYPE_REFERENCE);
        dataTraceRecordMap.remove(NumberIdEntity.CREATION_TIME_FIELD_NAME);
        dataTraceRecordMap.remove(AuthenticationSuccessToken.PRINCIPAL_KEY);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put(AuthenticationSuccessToken.DETAILS_KEY, authenticationToken.getDetails());

        data.put(ControllerAuditHandlerInterceptor.OPERATION_DATA_TRACE_ATT_NAME, dataTraceRecordMap);

        return new AuditEvent(
                event.getTimestamp(),
                authenticationToken.getName(),
                event.getType(),
                data
        );
    }
}
