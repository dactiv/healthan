package com.github.dactiv.healthan.canal.domain.entity;

import com.github.dactiv.healthan.commons.enumerate.support.Protocol;
import com.github.dactiv.healthan.commons.enumerate.support.YesOrNo;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * canal 行数据变更通知实体
 *
 * @author maurice.chen
 */
public class CanalRowDataChangeNoticeEntity implements Serializable {

    private static final long serialVersionUID = -5105461307882560087L;

    public static final String PROTOCOL_META_FIELD_NAME = "protocolMeta";

    /**
     * 是否启用
     */
    private YesOrNo enabled = YesOrNo.No;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 过滤数据库表的正则表达式
     */
    private List<String> regularExpressions;

    /**
     * 字段映射内容
     */
    private Map<String, Map<String, String>> fieldMappings = new LinkedHashMap<>();

    /**
     * 是否仅仅需要 regularExpressions 匹配的数据消息
     */
    private YesOrNo filterRegularExpressionsMessage = YesOrNo.No;

    /**
     * 协议类型
     */
    private Protocol protocol;

    /**
     * 协议元数据信息
     */
    private Map<String, Object> protocolMeta;

    public CanalRowDataChangeNoticeEntity() {

    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public List<String> getRegularExpressions() {
        return regularExpressions;
    }

    public void setRegularExpressions(List<String> regularExpressions) {
        this.regularExpressions = regularExpressions;
    }

    public Map<String, Map<String, String>> getFieldMappings() {
        return fieldMappings;
    }

    public void setFieldMappings(Map<String, Map<String, String>> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    public YesOrNo getFilterRegularExpressionsMessage() {
        return filterRegularExpressionsMessage;
    }

    public void setFilterRegularExpressionsMessage(YesOrNo filterRegularExpressionsMessage) {
        this.filterRegularExpressionsMessage = filterRegularExpressionsMessage;
    }

    public YesOrNo getEnabled() {
        return enabled;
    }

    public void setEnabled(YesOrNo enabled) {
        this.enabled = enabled;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Map<String, Object> getProtocolMeta() {
        return protocolMeta;
    }

    public void setProtocolMeta(Map<String, Object> protocolMeta) {
        this.protocolMeta = protocolMeta;
    }
}
