package com.github.dactiv.healthan.canal.domain.meta;

import java.io.Serializable;

/**
 * 表列信息元数据
 *
 * @author maurice.chen
 */
public class TableColumnInfoMeta implements Serializable {

    public static final String MYSQL_COLUMN_NAME = "COLUMN_NAME";

    public static final String MYSQL_COLUMN_COMMENT = "COLUMN_COMMENT";

    private static final long serialVersionUID = 485451195258828391L;

    private String name;

    private String comment;

    private String id;

    public TableColumnInfoMeta() {
    }

    public TableColumnInfoMeta(String name, String comment, String id) {
        this.name = name;
        this.comment = comment;
        this.id = id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "TableColumnInfoMeta{" +
                "name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
