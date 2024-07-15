package com.github.dactiv.healthan.spring.security.audit;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.id.IdEntity;
import com.github.dactiv.healthan.mybatis.interceptor.audit.OperationDataTraceRecord;
import com.github.dactiv.healthan.mybatis.plus.audit.MybatisPlusOperationDataTraceRepository;
import com.github.dactiv.healthan.security.entity.SecurityPrincipal;
import com.github.dactiv.healthan.security.entity.support.SimpleTypePrincipal;
import com.github.dactiv.healthan.spring.security.authentication.token.AuthenticationSuccessToken;
import com.github.dactiv.healthan.spring.security.entity.UserDetailsOperationDataTraceRecord;
import com.github.dactiv.healthan.spring.web.mvc.SpringMvcUtils;
import net.sf.jsqlparser.statement.Statement;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;
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
public abstract class AbstractPrincipalOperationDataTraceRepository extends MybatisPlusOperationDataTraceRepository implements OperationDataTraceRepository {

    public static final String OPERATION_DATA_TRACE_ID_ATTR_NAME = "operationDataTraceId";

    private final SecurityAuditEventRepositoryInterceptor securityAuditEventRepositoryInterceptor;

    public AbstractPrincipalOperationDataTraceRepository(SecurityAuditEventRepositoryInterceptor securityAuditEventRepositoryInterceptor) {
        this.securityAuditEventRepositoryInterceptor = securityAuditEventRepositoryInterceptor;
    }

    @Override
    public void saveOperationDataTraceRecord(List<OperationDataTraceRecord> records) {
        for (OperationDataTraceRecord record: records) {
            if (securityAuditEventRepositoryInterceptor.preAddOperationDataTraceRecordHandle(record)) {
                continue;
            }
            doSaveOperationDataTraceRecord(record);
            securityAuditEventRepositoryInterceptor.postAddOperationDataTraceRecordHandle(record);
        }
    }

    protected abstract void doSaveOperationDataTraceRecord(OperationDataTraceRecord record);

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
        SecurityPrincipal userDetails = Casts.cast(authenticationToken.getPrincipal());
        String username = userDetails.getUsername();

        Map<String, Object> meta = Casts.convertValue(authenticationToken.getDetails(), Casts.MAP_TYPE_REFERENCE);
        if (MapUtils.isEmpty(meta)) {
            meta = new LinkedHashMap<>();
        }

        meta.put(SimpleTypePrincipal.TYPE_FIELD_NAME, authenticationToken.getPrincipalType());
        meta.put(IdEntity.ID_FIELD_NAME, userDetails.getId());

        List<OperationDataTraceRecord> records = super.createOperationDataTraceRecord(
                mappedStatement,
                statement,
                parameter
        );

        Object traceId = httpServletRequest.getAttribute(OPERATION_DATA_TRACE_ID_ATTR_NAME);
        if (Objects.isNull(traceId)) {
            traceId = UUID.randomUUID().toString();
            httpServletRequest.setAttribute(OPERATION_DATA_TRACE_ID_ATTR_NAME, traceId);
        }

        List<OperationDataTraceRecord> result = new LinkedList<>();

        for (OperationDataTraceRecord record : records.stream().filter(Objects::nonNull).collect(Collectors.toList())) {

            UserDetailsOperationDataTraceRecord userDetailsRecord = Casts.of(record, UserDetailsOperationDataTraceRecord.class);
            if (Objects.isNull(userDetailsRecord)) {
                continue;
            }

            Object auditType = httpServletRequest.getAttribute(ControllerAuditHandlerInterceptor.AUDIT_TYPE_ATTR_NAME);
            if (Objects.nonNull(auditType)) {
                userDetailsRecord.setAuditType(auditType.toString());
            }

            userDetailsRecord.setPrincipalMeta(meta);
            userDetailsRecord.setPrincipal(username);
            userDetailsRecord.setTraceId(traceId.toString());
            userDetailsRecord.setRemark(username + StringUtils.SPACE + getDateFormat().format(record.getCreationTime()) + StringUtils.SPACE + record.getType().getName());

            result.add(userDetailsRecord);
        }

        return result;
    }
}