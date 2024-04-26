package com.github.dactiv.healthan.canal.service;

import com.alibaba.otter.canal.protocol.FlatMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.dactiv.healthan.canal.domain.CanalMessage;
import com.github.dactiv.healthan.canal.domain.entity.CanalRowDataChangeNoticeEntity;
import com.github.dactiv.healthan.canal.domain.entity.CanalRowDataChangeNoticeRecordEntity;
import com.github.dactiv.healthan.canal.domain.meta.HttpCanalRowDataChangeNoticeMeta;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.enumerate.support.Protocol;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * canal 行数据变更通知服务
 *
 * @author maurice.chen
 */
public interface CanalRowDataChangeNoticeService {

    String HTTP_ENTITY_FIELD = "httpEntity";

    /**
     * 根据目标集合获取 canal 行数据变更实体集合
     *
     * @param destinations 数据库名.表明的目标信息
     *
     * @return canal 行数据变更通知实体集合
     */
    List<CanalRowDataChangeNoticeEntity> findEnableByDestinations(List<String> destinations);

    /**
     * 创建 canal 行数据变更通知记录实体
     *
     * @param notice 通知实体
     * @param message canal 消息
     *
     * @return canal 行变更通知记录实体集合
     */
    default List<CanalRowDataChangeNoticeRecordEntity> createCanalRowDataChangeNoticeRecordEntity(CanalRowDataChangeNoticeEntity notice, CanalMessage message) {
        List<CanalRowDataChangeNoticeRecordEntity> result = new ArrayList<>();
        if (Protocol.HTTP_OR_HTTPS.equals(notice.getProtocol())) {

            List<HttpCanalRowDataChangeNoticeMeta> metas = Casts.convertValue(
                    notice.getProtocolMeta().get(HTTP_ENTITY_FIELD),
                    new TypeReference<List<HttpCanalRowDataChangeNoticeMeta>>() {}
            );
            for (HttpCanalRowDataChangeNoticeMeta map : metas) {
                CanalRowDataChangeNoticeRecordEntity entity = Casts.of(
                        notice,
                        CanalRowDataChangeNoticeRecordEntity.class,
                        CanalRowDataChangeNoticeEntity.PROTOCOL_META_FIELD_NAME
                );
                if (Objects.isNull(entity)) {
                    continue;
                }
                entity.setProtocolMeta(Casts.convertValue(map, Casts.MAP_TYPE_REFERENCE));
                entity.setRequestBody(Casts.convertValue(message, Casts.MAP_TYPE_REFERENCE));
                result.add(entity);
            }
        } else {
            CanalRowDataChangeNoticeRecordEntity entity = Casts.of(
                    notice,
                    CanalRowDataChangeNoticeRecordEntity.class,
                    CanalRowDataChangeNoticeEntity.PROTOCOL_META_FIELD_NAME
            );

            if (Objects.nonNull(entity)) {
                entity.setRequestBody(Casts.convertValue(message, Casts.MAP_TYPE_REFERENCE));
                result.add(entity);
            }
        }
        return result;
    }

    /**
     * 保存 canal 行数据变更通知实体
     *
     * @param record canal 行数据变更记录实体集合
     */
    void saveCanalRowDataChangeNoticeRecordEntity(CanalRowDataChangeNoticeRecordEntity record);

    /**
     * 发送 canal 行数据变更通知
     *
     * @param canalRowDataChangeNoticeRecordEntity canal 行数据变更通知实体
     */
    void sendCanalRowDataChangeNoticeRecord(CanalRowDataChangeNoticeRecordEntity canalRowDataChangeNoticeRecordEntity);

    /**
     * 映射字段，用于将表字段映射成其他字段名
     *
     * @param flatMessages canal 表字段消息
     * @param fieldMappings 要映射的表和字段内容
     */
    default void mappingField(List<FlatMessage> flatMessages, Map<String, Map<String, String>> fieldMappings) {
        for (FlatMessage flatMessage : flatMessages) {
            if (!fieldMappings.containsKey(flatMessage.getDatabase() + Casts.DOT + flatMessage.getTable())) {
                continue;
            }

            Map<String, String> fields = fieldMappings.get(flatMessage.getDatabase() + Casts.DOT + flatMessage.getTable());
            List<Map<String, String>> newData = mappingNewField(flatMessage.getData(), fields);
            if (CollectionUtils.isNotEmpty(newData)) {
                flatMessage.setData(newData);
            }

            List<Map<String, String>> newOldData = mappingNewField(flatMessage.getOld(), fields);
            if (CollectionUtils.isNotEmpty(newOldData)) {
                flatMessage.setOld(newOldData);
            }

            List<String> newPkNames = new LinkedList<>();

            for (String pkName : flatMessage.getPkNames()) {
                newPkNames.add(fields.getOrDefault(pkName, pkName));
            }
            flatMessage.setPkNames(newPkNames);
        }
    }

    /**
     * 映射新字段信息
     *
     * @param dataList canal 表数据信息
     * @param fields 字段信息
     *
     * @return 新的行数据变更结构
     */
    default List<Map<String, String>> mappingNewField(List<Map<String, String>> dataList, Map<String, String> fields) {
        List<Map<String, String>> newData = new LinkedList<>();
        if (CollectionUtils.isEmpty(dataList)) {
            return newData;
        }

        for (Map<String, String> data : dataList) {
            Map<String, String> newValue = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                // 获取 entry.getKey()，如果没有对应的字段映射，就用 entry.getKey() 值
                String field = fields.getOrDefault(entry.getKey(), entry.getKey());
                newValue.put(StringUtils.defaultString(field, entry.getKey()), entry.getValue());
            }
            newData.add(newValue);
        }

        return newData;
    }

}
