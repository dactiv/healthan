package com.github.dactiv.healthan.security.audit;

import com.github.dactiv.healthan.commons.id.StringIdEntity;
import com.github.dactiv.healthan.commons.page.Page;
import com.github.dactiv.healthan.commons.page.PageRequest;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;

import java.time.Instant;

/**
 * 插件审计事件的仓库实现
 *
 * @author maurice.chen
 */
public interface ExtendAuditEventRepository extends AuditEventRepository {

    /**
     * 获取分页信息
     *
     * @param pageRequest 分页请求
     * @param principal   当前人
     * @param after       在什么时间之后的数据
     * @param type        类型
     *
     * @return 分页信息
     */
    Page<AuditEvent> findPage(PageRequest pageRequest, String principal, Instant after, String type);

    /**
     * 通过唯一识别获取数据
     *
     * @param idEntity 唯一识别;
     *
     * @return 审计事件
     */
    AuditEvent get(StringIdEntity idEntity);

}
