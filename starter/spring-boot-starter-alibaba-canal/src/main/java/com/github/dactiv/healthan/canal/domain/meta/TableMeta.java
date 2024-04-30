package com.github.dactiv.healthan.canal.domain.meta;

import java.io.Serializable;
import java.util.List;

/**
 * 表元数据信息
 *
 * @author maurice.chen
 */
public class TableMeta implements Serializable {

    private static final long serialVersionUID = -3280652379059502703L;

    /**
     * 所属数据库
     */
    private String database;

    /**
     * 表名称
     */
    private String name;

    /**
     * 备注信息
     */
    private String comment;

    /**
     * 表列信息
     */
    private List<TableColumnInfoMeta> columnInfoMetas;

    public TableMeta() {
    }

    public TableMeta(String database, String name, String comment, List<TableColumnInfoMeta> columnInfoMetas) {
        this.database = database;
        this.name = name;
        this.comment = comment;
        this.columnInfoMetas = columnInfoMetas;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<TableColumnInfoMeta> getColumnInfoMetas() {
        return columnInfoMetas;
    }

    public void setColumnInfoMetas(List<TableColumnInfoMeta> columnInfoMetas) {
        this.columnInfoMetas = columnInfoMetas;
    }
}
