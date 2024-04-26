package com.github.dactiv.healthan.canal.resolver.support;

import com.alibaba.otter.canal.protocol.FlatMessage;
import com.github.dactiv.healthan.canal.domain.CanalMessage;
import com.github.dactiv.healthan.canal.domain.entity.CanalRowDataChangeNoticeEntity;
import com.github.dactiv.healthan.canal.domain.entity.CanalRowDataChangeNoticeRecordEntity;
import com.github.dactiv.healthan.canal.resolver.CanalRowDataChangeResolver;
import com.github.dactiv.healthan.canal.service.CanalRowDataChangeNoticeService;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.enumerate.support.YesOrNo;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * canal 行数据变更通知解析器
 *
 * @author maurice.chen
 */
public class CanalRowDataChangeNoticeResolver implements CanalRowDataChangeResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CanalRowDataChangeNoticeResolver.class);

    /**
     * canal 行谁变更通知服务
     */
    private CanalRowDataChangeNoticeService canalRowDataChangeNoticeService;

    public CanalRowDataChangeNoticeResolver(CanalRowDataChangeNoticeService canalRowDataChangeNoticeService) {
        this.canalRowDataChangeNoticeService = canalRowDataChangeNoticeService;
    }

    public CanalRowDataChangeNoticeResolver() {
    }

    @Override
    public void change(CanalMessage message) {
        // 获取消息里的目标结构，最终结果为:数据库名.表明
        List<String> destinations = new LinkedList<>();
        for (FlatMessage flatMessage : message.getFlatMessageList()) {
            String destination = flatMessage.getDatabase() + Casts.DOT + flatMessage.getTable();
            if (destinations.contains(destination)) {
                continue;
            }
            destinations.add(destination);
        }

        // 通过数据库名称.表名称查询启用的通知实体
        List<CanalRowDataChangeNoticeEntity> result = canalRowDataChangeNoticeService.findEnableByDestinations(destinations);

        List<CanalRowDataChangeNoticeRecordEntity> recordList = new LinkedList<>();

        // 循环构造所有要发送的消息记录
        for (CanalRowDataChangeNoticeEntity notification : result) {
            // 克隆一次新的对象内容，防止某些通知将 message 原始数据修改。
            CanalMessage temp = Casts.of(message, CanalMessage.class);
            if (Objects.isNull(temp)) {
                LOGGER.warn("克隆 [{}] 数据成 temp 时出现 null 情况",message);
                continue;
            }

            temp.setFlatMessageList(temp.getFlatMessageList().stream().map(f -> Casts.of(f, FlatMessage.class)).collect(Collectors.toList()));

            // 如果需要过滤仅需要的数据，过滤数据
            if (YesOrNo.Yes.equals(notification.getFilterRegularExpressionsMessage())) {
                List<FlatMessage> filterMessage = temp
                        .getFlatMessageList()
                        .stream()
                        .filter(f -> notification.getRegularExpressions().stream().anyMatch(n -> Pattern.matches(n, f.getDatabase() + Casts.DOT + f.getTable())))
                        .collect(Collectors.toList());

                temp.setFlatMessageList(filterMessage);
            }

            // 如果需要进行字段映射，映射字段
            if (MapUtils.isNotEmpty(notification.getFieldMappings())) {
                List<FlatMessage> flatMessages = temp
                        .getFlatMessageList()
                        .stream()
                        .filter(f -> notification.getFieldMappings().containsKey(f.getDatabase() + Casts.DOT + f.getTable()))
                        .collect(Collectors.toList());

                canalRowDataChangeNoticeService.mappingField(flatMessages, notification.getFieldMappings());
            }

            recordList.addAll(canalRowDataChangeNoticeService.createCanalRowDataChangeNoticeRecordEntity(notification, temp));

        }

        recordList.forEach(canalRowDataChangeNoticeService::saveCanalRowDataChangeNoticeRecordEntity);

        recordList.forEach(canalRowDataChangeNoticeService::sendCanalRowDataChangeNoticeRecord);
    }

    public CanalRowDataChangeNoticeService getCanalRowDataChangeNoticeService() {
        return canalRowDataChangeNoticeService;
    }

    public void setCanalRowDataChangeNoticeService(CanalRowDataChangeNoticeService canalRowDataChangeNoticeService) {
        this.canalRowDataChangeNoticeService = canalRowDataChangeNoticeService;
    }
}
