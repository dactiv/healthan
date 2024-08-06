package com.github.dactiv.healthan.mybatis.plus.test;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.enumerate.support.DisabledOrEnabled;
import com.github.dactiv.healthan.commons.enumerate.support.ExecuteStatus;
import com.github.dactiv.healthan.commons.id.IdEntity;
import com.github.dactiv.healthan.commons.id.StringIdEntity;
import com.github.dactiv.healthan.mybatis.enumerate.OperationDataType;
import com.github.dactiv.healthan.mybatis.interceptor.audit.OperationDataTraceRecord;
import com.github.dactiv.healthan.mybatis.plus.test.entity.AllTypeEntity;
import com.github.dactiv.healthan.mybatis.plus.test.service.AllTypeEntityService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TestBasicService {

    @Autowired
    private AllTypeEntityService allTypeEntityService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Test
    public void testAllType() {

        AllTypeEntity entity = allTypeEntityService.get(1);

        Assertions.assertEquals(entity.getStatus(), DisabledOrEnabled.Enabled);
        Assertions.assertEquals(entity.getDevice().size(), 2);

        Assertions.assertTrue(
                entity
                        .getEntities()
                        .stream()
                        .allMatch(c -> StringIdEntity.class.isAssignableFrom(c.getClass()))
        );

        Assertions.assertTrue(
                entity
                        .getExecutes()
                        .containsAll(Arrays.asList(ExecuteStatus.Processing, ExecuteStatus.Success, ExecuteStatus.Retrying))
        );

        Assertions.assertEquals(entity.getStatus(), DisabledOrEnabled.Enabled);
    }

    @Test
    public void testInsertOrUpdate() throws Exception {
        AllTypeEntity entity = new AllTypeEntity();

        entity.setStatus(DisabledOrEnabled.Disabled);
        entity.setExecutes(Arrays.asList(ExecuteStatus.Failure, ExecuteStatus.Processing));

        allTypeEntityService.save(entity);

        List<AuditEvent> events = auditEventRepository.find(null, null, null);
        Assertions.assertEquals(events.size(), 1);

        AuditEvent event = events.iterator().next();
        Map<String, Object> submitData = Casts.cast(event.getData().get(OperationDataTraceRecord.SUBMIT_DATA_FIELD));

        Assertions.assertEquals(event.getPrincipal(), InetAddress.getLocalHost().getHostAddress());
        Assertions.assertEquals(submitData.get(IdEntity.ID_FIELD_NAME), entity.getId());
        Assertions.assertTrue(StringUtils.endsWith(event.getType(), OperationDataType.INSERT.toString()));

        Map<String, Object> status = Casts.cast(submitData.get("status"));
        Assertions.assertEquals(status.get("value"), DisabledOrEnabled.Disabled.getValue());

        allTypeEntityService
                .lambdaUpdate()
                .set(AllTypeEntity::getStatus, DisabledOrEnabled.Enabled.getValue())
                .eq(AllTypeEntity::getId, entity.getId())
                .update();

        events = auditEventRepository.find(null, null, null);
        Assertions.assertEquals(events.size(), 2);

        event = events.get(events.size() - 1);
        Assertions.assertEquals(event.getPrincipal(), InetAddress.getLocalHost().getHostAddress());
        Assertions.assertTrue(StringUtils.endsWith(event.getType(), OperationDataType.UPDATE.toString()));

        submitData = Casts.cast(event.getData().get(OperationDataTraceRecord.SUBMIT_DATA_FIELD));
        Integer statusValue = Casts.cast(submitData.get("status"));
        Assertions.assertEquals(statusValue, DisabledOrEnabled.Enabled.getValue());

        entity.setStatus(DisabledOrEnabled.Disabled);
        allTypeEntityService.updateById(entity);
        events = auditEventRepository.find(null, null, null);
        Assertions.assertEquals(events.size(), 3);

        event = events.get(events.size() - 1);
        Assertions.assertEquals(event.getPrincipal(), InetAddress.getLocalHost().getHostAddress());
        Assertions.assertTrue(StringUtils.endsWith(event.getType(), OperationDataType.UPDATE.toString()));

        submitData = Casts.cast(event.getData().get(OperationDataTraceRecord.SUBMIT_DATA_FIELD));
        status = Casts.cast(submitData.get("status"));
        Assertions.assertEquals(status.get("value"), DisabledOrEnabled.Disabled.getValue());

        allTypeEntityService.deleteByEntity(entity);
        events = auditEventRepository.find(null, null, null);
        Assertions.assertEquals(events.size(), 4);

        event = events.get(events.size() - 1);
        Assertions.assertEquals(event.getPrincipal(), InetAddress.getLocalHost().getHostAddress());
        Assertions.assertTrue(StringUtils.endsWith(event.getType(), OperationDataType.DELETE.toString()));

        entity.setId(null);
        allTypeEntityService.save(entity);
        events = auditEventRepository.find(null, null, null);
        Assertions.assertEquals(events.size(), 5);

        event = events.get(events.size() - 1);
        submitData = Casts.cast(event.getData().get(OperationDataTraceRecord.SUBMIT_DATA_FIELD));

        Assertions.assertEquals(event.getPrincipal(), InetAddress.getLocalHost().getHostAddress());
        Assertions.assertEquals(submitData.get(IdEntity.ID_FIELD_NAME), entity.getId());
        Assertions.assertTrue(StringUtils.endsWith(event.getType(), OperationDataType.INSERT.toString()));

        allTypeEntityService.lambdaUpdate().eq(AllTypeEntity::getId,entity.getId()).remove();
        events = auditEventRepository.find(null, null, null);
        Assertions.assertEquals(events.size(), 6);

        event = events.get(events.size() - 1);
        Assertions.assertEquals(event.getPrincipal(), InetAddress.getLocalHost().getHostAddress());
        Assertions.assertTrue(StringUtils.endsWith(event.getType(), OperationDataType.DELETE.toString()));

    }

}
