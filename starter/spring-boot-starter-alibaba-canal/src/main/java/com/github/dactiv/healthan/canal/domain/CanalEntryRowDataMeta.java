package com.github.dactiv.healthan.canal.domain;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.io.Serial;
import java.io.Serializable;

/**
 * 列数据信息
 *
 * @author maurice.chen
 */
public class CanalEntryRowDataMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = 7857439908947971423L;

    private CanalEntry.Entry entry;

    private CanalEntry.RowChange rowChange;

    public CanalEntryRowDataMeta() {
    }

    public CanalEntryRowDataMeta(CanalEntry.Entry entry, CanalEntry.RowChange rowChange) {
        this.entry = entry;
        this.rowChange = rowChange;
    }

    public CanalEntry.Entry getEntry() {
        return entry;
    }

    public void setEntry(CanalEntry.Entry entry) {
        this.entry = entry;
    }

    public CanalEntry.RowChange getRowChange() {
        return rowChange;
    }

    public void setRowChange(CanalEntry.RowChange rowChange) {
        this.rowChange = rowChange;
    }
}
