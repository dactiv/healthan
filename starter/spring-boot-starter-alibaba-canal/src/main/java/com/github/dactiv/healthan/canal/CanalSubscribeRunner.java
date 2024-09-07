package com.github.dactiv.healthan.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.client.impl.ClusterCanalConnector;
import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import com.alibaba.otter.canal.common.utils.ExecutorTemplate;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.FlatMessage;
import com.alibaba.otter.canal.protocol.Message;
import com.github.dactiv.healthan.canal.config.CanalInstanceProperties;
import com.github.dactiv.healthan.canal.config.CanalProperties;
import com.github.dactiv.healthan.canal.domain.CanalEntryRowDataMeta;
import com.github.dactiv.healthan.canal.domain.CanalMessage;
import com.github.dactiv.healthan.commons.Casts;
import com.github.dactiv.healthan.commons.ReflectionUtils;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

/**
 * canal 订阅消息运行者
 *
 * @author maurice.chen
 */
public class CanalSubscribeRunner implements Runnable {

    public static final Logger LOGGER = LoggerFactory.getLogger(CanalSubscribeRunner.class);


    public static final List<CanalEntry.EntryType> TRANSACTION_TYPES = Arrays.asList(CanalEntry.EntryType.TRANSACTIONBEGIN, CanalEntry.EntryType.TRANSACTIONEND);

    public static final List<CanalEntry.EventType> DML_EVENT_TYPES = Arrays.asList(CanalEntry.EventType.INSERT, CanalEntry.EventType.UPDATE, CanalEntry.EventType.DELETE);

    private CanalConnector connector;

    private volatile boolean running = true;

    private final CanalProperties canalProperties;

    private final Consumer<CanalMessage> consumer;

    private final CanalInstanceProperties instance;

    private final ThreadPoolExecutor buildExecutor;

    private Future<?> future;

    private Date exceptionTime = new Date();

    public CanalSubscribeRunner(CanalInstanceProperties instance,
                                CanalProperties canalProperties,
                                ThreadPoolExecutor buildExecutor,
                                Consumer<CanalMessage> consumer) {
        this.canalProperties = canalProperties;
        this.buildExecutor = buildExecutor;
        this.consumer = consumer;
        this.instance = instance;

        this.connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(instance.getHost(), instance.getTcpPort()),
                instance.getName(),
                instance.getUsername(),
                instance.getPassword()
        );

    }

    @Override
    public void run() {
        while (running) {

            if (exceptionTime.getTime() > System.currentTimeMillis()) {
                continue;
            }

            try {
                connector.connect();
                LOGGER.info("链接 canal 的 [{}] 实例成功", instance.getName());
                connector.subscribe();
                while (running) {
                    Message message = connector.getWithoutAck(canalProperties.getBatchSize());
                    long batchId = message.getId();
                    if (message.getEntries().stream().anyMatch(e -> CanalEntry.EntryType.ROWDATA.equals(e.getEntryType()))) {
                        resolve(message);
                    }

                    if (batchId != -1) {
                        // 提交确认
                        connector.ack(batchId);
                    }
                }
            } catch (Throwable e) {
                exceptionTime = new Date(System.currentTimeMillis() + canalProperties.getExceptionRetryTime().getUnit().toMillis(canalProperties.getExceptionRetryTime().getValue()));
                LOGGER.warn("canal 的 [{}] 处理消息错误, 错误消息为 [{}], 将会在: {} 后重连在获取消息", instance.getName(), e.getMessage(), exceptionTime);
                // 处理失败, 回滚数据
                rollbackConnection();
            } finally {
                disconnectConnection();
            }

        }
    }

    /**
     * 回滚数据
     */
    private void rollbackConnection() {
        if (Objects.isNull(connector)) {
            return;
        }
        if (SimpleCanalConnector.class.isAssignableFrom(connector.getClass())) {
            boolean connected = Casts.cast(ReflectionUtils.getFieldValue(connector, "connected"));
            if (!connected) {
                return;
            }
        }
        try {
            connector.rollback(); // 处理失败, 回滚数据
        } catch (Exception e) {
            LOGGER.error("canal 回滚数据错误", e);
        }
    }

    /**
     * 断开链接
     */
    private void disconnectConnection() {
        if (Objects.isNull(connector)) {
            return;
        }
        try {
            connector.disconnect(); // 处理失败, 回滚数据
        } catch (Exception e) {
            LOGGER.error("canal 释放数据错误", e);
        }
    }

    public void resolve(Message message) {

        try {

            CanalMessage canalMessage = new CanalMessage();
            canalMessage.setId(UUID.randomUUID().toString());

            message
                    .getEntries()
                    .stream()
                    .filter(e -> CanalEntry.EntryType.TRANSACTIONEND.equals(e.getEntryType()))
                    .map(CanalSubscribeRunner::buildTransactionEndMessage)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .ifPresent(t -> canalMessage.setTransactionId(t.getTransactionId()));

            CanalEntryRowDataMeta[] entryRowData = buildMessageData(message, buildExecutor);
            List<FlatMessage> flatMessages = messageConverter(entryRowData, message.getId());

            canalMessage.setFlatMessageList(flatMessages);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("canal 收到 binlog 变更消息，事务 id 为 {}, 变更数据条目为 {} 条", canalMessage.getTransactionId(), canalMessage.getFlatMessageList().size());
            }
            consumer.accept(canalMessage);
        } catch (Exception e) {
            LOGGER.error("canal 解析数据变更内容出现错误", e);
        }
    }

    public void stop() {
        this.running = false;

        if (Objects.isNull(connector)) {
            LOGGER.warn("停止 canal 的 [{}] 操作发现实例的 connector 为空跳过 connector 停止操作", instance.getName());
            return;
        }

        try {
            connector.unsubscribe();
        } catch (Exception e) {
            LOGGER.warn("取消订阅 canal 的 [{}] 失败， 直接断开:{}", instance.getName(), e.getMessage());
            connector.disconnect();
        }

        if (connector instanceof ClusterCanalConnector) {
            ClusterCanalConnector canalConnector = Casts.cast(connector);
            canalConnector.stopRunning();
        } else if (connector instanceof SimpleCanalConnector) {
            SimpleCanalConnector canalConnector = Casts.cast(connector);
            canalConnector.stopRunning();
        }

        connector = null;

        LOGGER.info("停止 canal 的 [{}] 实例成功", instance.getName());
    }

    public static CanalEntry.TransactionEnd buildTransactionEndMessage(CanalEntry.Entry entry) {
        try {
            return CanalEntry.TransactionEnd.parseFrom(entry.getStoreValue());
        } catch (Exception e) {
            LOGGER.warn("解析 TransactionBegin 时出现错误", e);
            return null;
        }
    }

    public static CanalEntryRowDataMeta[] buildMessageData(Message message, ThreadPoolExecutor executor) {
        ExecutorTemplate template = new ExecutorTemplate(executor);
        if (message.isRaw()) {
            List<ByteString> rawEntries = message.getRawEntries();
            final CanalEntryRowDataMeta[] data = new CanalEntryRowDataMeta[rawEntries.size()];
            int i = 0;
            for (ByteString byteString : rawEntries) {
                final int index = i;
                template.submit(() -> {
                    try {
                        CanalEntry.Entry entry = CanalEntry.Entry.parseFrom(byteString);
                        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

                        data[index] = new CanalEntryRowDataMeta();
                        data[index].setEntry(entry);
                        data[index].setRowChange(rowChange);

                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                });

                i++;
            }

            template.waitForResult();
            return data;
        } else {
            final CanalEntryRowDataMeta[] dataArray = new CanalEntryRowDataMeta[message.getEntries().size()];
            int i = 0;
            for (CanalEntry.Entry entry : message.getEntries()) {
                final int index = i;
                template.submit(() -> {
                    try {
                        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                        dataArray[index] = new CanalEntryRowDataMeta();
                        dataArray[index].setEntry(entry);
                        dataArray[index].setRowChange(rowChange);
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException(e);
                    }
                });

                i++;
            }

            template.waitForResult();
            return dataArray;
        }
    }

    public static List<FlatMessage> messageConverter(CanalEntryRowDataMeta[] dataArray, long id) {
        List<FlatMessage> flatMessages = new ArrayList<>();
        for (CanalEntryRowDataMeta entryRowData : dataArray) {
            CanalEntry.Entry entry = entryRowData.getEntry();
            CanalEntry.RowChange rowChange = entryRowData.getRowChange();
            // 如果有分区路由,则忽略begin/end事件
            if (TRANSACTION_TYPES.contains(entry.getEntryType())) {
                continue;
            }

            if (rowChange.getIsDdl()) {
                continue;
            }
            if (!DML_EVENT_TYPES.contains(rowChange.getEventType())) {
                continue;
            }

            FlatMessage flatMessage = createFlatMessage(id, entry, rowChange);

            flatMessages.add(flatMessage);
        }
        return flatMessages;
    }

    private static FlatMessage createFlatMessage(long id, CanalEntry.Entry entry, CanalEntry.RowChange rowChange) {
        FlatMessage flatMessage = new FlatMessage(id);
        flatMessage.setDatabase(entry.getHeader().getSchemaName());
        flatMessage.setTable(entry.getHeader().getTableName());
        flatMessage.setIsDdl(rowChange.getIsDdl());
        flatMessage.setType(rowChange.getEventType().toString());
        flatMessage.setEs(entry.getHeader().getExecuteTime());
        flatMessage.setTs(System.currentTimeMillis());
        flatMessage.setSql(rowChange.getSql());

        Map<String, Integer> sqlType = new LinkedHashMap<>();
        Map<String, String> mysqlType = new LinkedHashMap<>();

        List<Map<String, String>> data = new ArrayList<>();
        List<Map<String, String>> old = new ArrayList<>();

        Set<String> updateSet = new HashSet<>();
        boolean hasInitPkNames = false;
        for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {

            Map<String, String> row = new LinkedHashMap<>();
            List<CanalEntry.Column> columns;

            if (rowChange.getEventType() == CanalEntry.EventType.DELETE) {
                columns = rowData.getBeforeColumnsList();
            } else {
                columns = rowData.getAfterColumnsList();
            }

            for (CanalEntry.Column column : columns) {
                if (!hasInitPkNames && column.getIsKey()) {
                    flatMessage.addPkName(column.getName());
                }
                sqlType.put(column.getName(), column.getSqlType());
                mysqlType.put(column.getName(), column.getMysqlType());
                if (column.getIsNull()) {
                    row.put(column.getName(), null);
                } else {
                    row.put(column.getName(), column.getValue());
                }
                // 获取update为true的字段
                if (column.getUpdated()) {
                    updateSet.add(column.getName());
                }
            }

            hasInitPkNames = true;
            if (!row.isEmpty()) {
                data.add(row);
            }

            if (rowChange.getEventType() == CanalEntry.EventType.UPDATE) {
                Map<String, String> rowOld = new LinkedHashMap<>();
                for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
                    if (updateSet.contains(column.getName())) {
                        if (column.getIsNull()) {
                            rowOld.put(column.getName(), null);
                        } else {
                            rowOld.put(column.getName(), column.getValue());
                        }
                    }
                }
                // update操作将记录修改前的值
                old.add(rowOld);
            }
        }
        if (!sqlType.isEmpty()) {
            flatMessage.setSqlType(sqlType);
        }
        if (!mysqlType.isEmpty()) {
            flatMessage.setMysqlType(mysqlType);
        }
        if (!data.isEmpty()) {
            flatMessage.setData(data);
        }
        if (!old.isEmpty()) {
            flatMessage.setOld(old);
        }

        return flatMessage;
    }

    public CanalInstanceProperties getInstance() {
        return instance;
    }

    public Future<?> getFuture() {
        return future;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }
}
