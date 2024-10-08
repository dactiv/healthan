package com.github.dactiv.healthan.security.audit.elasticsearch;

import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.security.AuditIndexProperties;
import com.github.dactiv.healthan.security.KafkaProperties;
import com.github.dactiv.healthan.security.audit.AuditEventRepositoryInterceptor;
import com.github.dactiv.healthan.security.audit.IdAuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

/**
 * kafka 形式得 es 审计事件仓库实现
 *
 * @author maurice.chen
 */
public class KafkaElasticsearchAuditEventRepository extends ElasticsearchAuditEventRepository{

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaElasticsearchAuditEventRepository.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public KafkaElasticsearchAuditEventRepository(List<AuditEventRepositoryInterceptor> interceptors,
                                                  ElasticsearchOperations elasticsearchOperations,
                                                  AuditIndexProperties auditIndexProperties,
                                                  KafkaTemplate<String, Object> kafkaTemplate,
                                                  KafkaProperties kafkaProperties) {
        super(interceptors, elasticsearchOperations, auditIndexProperties);
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    @Override
    public void doAdd(AuditEvent event) {

        IdAuditEvent idAuditEvent = new IdAuditEvent(
                event.getPrincipal(),
                event.getType(),
                event.getData()
        );

        if (IdAuditEvent.class.isAssignableFrom(event.getClass())) {
            idAuditEvent = Casts.cast(event);
        }

        kafkaTemplate.send(kafkaProperties.getTopic(), idAuditEvent).whenComplete((sendResult, throwable) -> {
            if (throwable != null) {
                LOGGER.error("发送 topic: {} 消息失败", kafkaProperties.getTopic(), throwable);
            } else if (LOGGER.isDebugEnabled()){
                LOGGER.debug("发送 topic: {} 消息成功, 响应信息为: {}", kafkaProperties.getTopic(), sendResult.toString());
            }
        });

    }
}
