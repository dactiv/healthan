package com.github.dactiv.healthan.canal.service.support;

import com.github.dactiv.healthan.canal.domain.entity.CanalRowDataChangeNoticeRecordEntity;
import com.github.dactiv.healthan.canal.resolver.CanalRowDataChangeNoticeResolver;
import com.github.dactiv.healthan.canal.service.CanalRowDataChangeNoticeService;
import com.github.dactiv.healthan.commons.exception.SystemException;

import java.util.List;

/**
 * 抽象的 canal 行数据变更通知实现
 *
 * @author maurice.chen
 */
public abstract class AbstractCanalRowDataChangeNoticeService implements CanalRowDataChangeNoticeService {

    private List<CanalRowDataChangeNoticeResolver> canalRowDataChangeNoticeResolvers;

    public AbstractCanalRowDataChangeNoticeService() {
    }

    public AbstractCanalRowDataChangeNoticeService(List<CanalRowDataChangeNoticeResolver> canalRowDataChangeNoticeResolvers) {
        this.canalRowDataChangeNoticeResolvers = canalRowDataChangeNoticeResolvers;
    }

    @Override
    public void sendCanalRowDataChangeNoticeRecord(CanalRowDataChangeNoticeRecordEntity entity) {
        canalRowDataChangeNoticeResolvers
                .stream()
                .filter(c -> c.isSupport(entity))
                .findFirst()
                .orElseThrow(() -> new SystemException("找不到 [" + entity + "] 的通知解析器支持"))
                .send(entity, this::saveCanalRowDataChangeNoticeRecordEntity);

    }
}
