package com.github.dactiv.healthan.security.test.audit;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.id.StringIdEntity;
import com.github.dactiv.healthan.security.audit.IdAuditEvent;
import com.github.dactiv.healthan.security.audit.elasticsearch.ElasticsearchAuditEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 审计事件仓库单元测试
 *
 * @author maurice.chen
 */
@SpringBootTest
@ActiveProfiles("elasticsearch")
public class TestElasticsearchAuditEventRepository {

    @Autowired
    private ElasticsearchAuditEventRepository auditEventRepository;

    @Test
    public void test() throws InterruptedException {
        Instant instant = Instant.now();

        int before = auditEventRepository.find("admin", instant, null).size();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("d",1);
        map.put("xx",3);
        map.put("test","tests");

        Map<String, Object> date = new LinkedHashMap<>();
        date.put("date", new Date());
        map.put("data",date);


        auditEventRepository.add(new IdAuditEvent("admin", "test", map));
        Thread.sleep(5000);
        List<AuditEvent> auditEvents = auditEventRepository.find("admin", instant, null);

        Assertions.assertEquals(before + 1, auditEvents.size());

        IdAuditEvent target = Casts.cast(auditEvents.iterator().next());

        StringIdEntity id = new StringIdEntity();
        id.setId(target.getId());
        id.setCreationTime(Date.from(target.getTimestamp()));

        AuditEvent event = auditEventRepository.get(id);

        Assertions.assertEquals(event.getPrincipal(), target.getPrincipal());
        Assertions.assertEquals(event.getType(), target.getType());
        Assertions.assertEquals(event.getData().size(), target.getData().size());
        Assertions.assertEquals(event.getTimestamp(), target.getTimestamp());

        auditEventRepository
                .getElasticsearchOperations()
                .indexOps(IndexCoordinates.of(auditEventRepository.getIndexName(instant)))
                .delete();

    }


}
