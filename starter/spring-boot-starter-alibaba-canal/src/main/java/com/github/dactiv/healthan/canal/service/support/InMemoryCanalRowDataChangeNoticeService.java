package com.github.dactiv.healthan.canal.service.support;

import com.github.dactiv.healthan.canal.domain.entity.CanalRowDataChangeNoticeEntity;
import com.github.dactiv.healthan.canal.resolver.CanalRowDataChangeNoticeResolver;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.domain.AckMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 内存存储形式的 canal 行数据变更通知服务实现
 *
 * @author maurice.chen
 */
public class InMemoryCanalRowDataChangeNoticeService extends AbstractCanalRowDataChangeNoticeService<CanalRowDataChangeNoticeEntity> {

    private static final List<CanalRowDataChangeNoticeEntity> CACHE_NOTICE_DATA = Collections.synchronizedList(new ArrayList<>());

    private static final List<AckMessage> CACHE_NOTICE_RECORD_DATA = Collections.synchronizedList(new ArrayList<>());

    public InMemoryCanalRowDataChangeNoticeService() {
    }

    public InMemoryCanalRowDataChangeNoticeService(List<CanalRowDataChangeNoticeResolver> canalRowDataChangeNoticeResolvers) {
        super(canalRowDataChangeNoticeResolvers);
    }

    @Override
    public List<CanalRowDataChangeNoticeEntity> findEnableByDestinations(List<String> destinations) {
        List<CanalRowDataChangeNoticeEntity> result = new LinkedList<>();
        for (String destination : destinations) {
            String databaseName = StringUtils.substringBefore(destination, Casts.COMMA);
            String tableName = StringUtils.substringAfter(destination, Casts.COMMA);
            if (result.stream().anyMatch(r -> r.getDatabaseName().equals(databaseName) && r.getTableName().equals(tableName))) {
                continue;
            }
            CACHE_NOTICE_DATA
                    .stream()
                    .filter(c -> c.getDatabaseName().equals(databaseName))
                    .filter(c -> c.getTableName().equals(tableName))
                    .forEach(result::add);
        }
        return Collections.emptyList();
    }

    @Override
    public void saveAckMessage(AckMessage record) {
        CACHE_NOTICE_RECORD_DATA.add(record);
    }
}
