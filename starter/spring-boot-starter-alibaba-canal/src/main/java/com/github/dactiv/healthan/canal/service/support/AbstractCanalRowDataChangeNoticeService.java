package com.github.dactiv.healthan.canal.service.support;

import com.github.dactiv.healthan.canal.domain.CanalRowDataChangeNotice;
import com.github.dactiv.healthan.canal.resolver.CanalRowDataChangeNoticeResolver;
import com.github.dactiv.healthan.canal.service.CanalRowDataChangeNoticeService;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.domain.AckMessage;
import com.github.dactiv.healthan.commons.exception.SystemException;

import java.util.List;

/**
 * 抽象的 canal 行数据变更通知实现
 *
 * @author maurice.chen
 */
public abstract class AbstractCanalRowDataChangeNoticeService<N extends CanalRowDataChangeNotice> implements CanalRowDataChangeNoticeService {

    private List<CanalRowDataChangeNoticeResolver> canalRowDataChangeNoticeResolvers;

    public AbstractCanalRowDataChangeNoticeService() {
    }

    public AbstractCanalRowDataChangeNoticeService(List<CanalRowDataChangeNoticeResolver> canalRowDataChangeNoticeResolvers) {
        this.canalRowDataChangeNoticeResolvers = canalRowDataChangeNoticeResolvers;
    }

    @Override
    public void sendAckMessage(AckMessage ackMessage) {
        canalRowDataChangeNoticeResolvers
                .stream()
                .filter(c -> c.isSupport(ackMessage))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到 [" + ackMessage + "] 的通知解析器支持"))
                .send(ackMessage, e -> this.saveAckMessage(Casts.cast(ackMessage)));

    }
}
