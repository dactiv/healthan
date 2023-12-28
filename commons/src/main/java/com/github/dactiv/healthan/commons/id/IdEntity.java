package com.github.dactiv.healthan.commons.id;



/**
 * 主键实体
 *
 * @param <T> 主键类型
 */
public class IdEntity<T> implements BasicIdentification<T> {

    
    private static final long serialVersionUID = -1430010950223063423L;

    /**
     * id 字段名称
     */
    public static final String ID_FIELD_NAME = "id";

    /**
     * 主键 id
     */
    private T id;

    /**
     * 创建一个主键实体
     */
    public IdEntity() {
    }

    /**
     * 创建一个主键实体
     *
     * @param id 主键 id
     */
    public IdEntity(T id) {
        this.id = id;
    }

    /**
     * 获取主键 id
     *
     * @return 主键 id
     */
    public T getId() {
        return id;
    }

    /**
     * 设置主键 id
     *
     * @param id 主键 id
     */
    public void setId(T id) {
        this.id = id;
    }

    /**
     * 创建一个主键实体
     *
     * @param id 主键 id
     */
    public static <T> IdEntity<T> of(T id) {
        return new IdEntity<>(id);
    }
}
