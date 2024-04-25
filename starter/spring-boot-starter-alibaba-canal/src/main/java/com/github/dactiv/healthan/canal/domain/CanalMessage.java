package com.github.dactiv.healthan.canal.domain;

import com.alibaba.otter.canal.protocol.FlatMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.dactiv.healthan.commons.id.StringIdEntity;

import java.util.List;

/**
 * canal 消息实体
 *
 * @author maurice.chen
 */
public class CanalMessage extends StringIdEntity {

    private static final long serialVersionUID = 5994433350821723923L;
    /**
     * 事务 id
     */
    private String transactionId;

    /**
     * binlog 消息内容集合
     */
    @JsonIgnoreProperties({"sqlType", "mysqlType", "sql", "gtid", "database", "isDdl"})
    private List<FlatMessage> flatMessageList;

    public CanalMessage() {
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<FlatMessage> getFlatMessageList() {
        return flatMessageList;
    }

    public void setFlatMessageList(List<FlatMessage> flatMessageList) {
        this.flatMessageList = flatMessageList;
    }
}
