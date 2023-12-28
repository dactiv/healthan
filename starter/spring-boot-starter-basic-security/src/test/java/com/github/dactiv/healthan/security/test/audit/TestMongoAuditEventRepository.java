package com.github.dactiv.healthan.security.test.audit;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.id.StringIdEntity;
import com.github.dactiv.healthan.security.audit.PluginAuditEvent;
import com.github.dactiv.healthan.security.audit.mongo.MongoAuditEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 审计事件仓库单元测试
 *
 * @author maurice.chen
 */
@SpringBootTest
@ActiveProfiles("mongo")
public class TestMongoAuditEventRepository {

    @Autowired
    private MongoAuditEventRepository auditEventRepository;

    @Test
    public void test() {

        Instant instant = Instant.now();

        int before = auditEventRepository.find("admin", instant, null).size();

        auditEventRepository.add(new PluginAuditEvent("admin", "test", new LinkedHashMap<>()));
        List<AuditEvent> auditEvents = auditEventRepository.find("admin", instant, null);

        Assertions.assertEquals(before + 1, auditEvents.size());

        PluginAuditEvent target = Casts.cast(auditEvents.iterator().next());

        StringIdEntity id = new StringIdEntity();
        id.setId(target.getId());
        id.setCreationTime(Date.from(target.getTimestamp()));

        AuditEvent event = auditEventRepository.get(id);

        Assertions.assertEquals(event.getPrincipal(), target.getPrincipal());
        Assertions.assertEquals(event.getType(), target.getType());
        Assertions.assertEquals(event.getData().size(), target.getData().size());
        Assertions.assertEquals(event.getTimestamp(), target.getTimestamp());

        auditEventRepository.getMongoTemplate().dropCollection(auditEventRepository.getCollectionName(instant));
    }


}
