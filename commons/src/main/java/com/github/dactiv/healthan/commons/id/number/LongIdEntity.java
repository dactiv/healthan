package com.github.dactiv.healthan.commons.id.number;

import com.github.dactiv.healthan.commons.id.IdEntity;

import java.util.Date;

/**
 * 长整型主键实体
 *
 * @author maurice.chen
 */
public class LongIdEntity extends IdEntity<Long> implements NumberIdEntity<Long> {
    /**
     * 创建时间
     */
    private Date creationTime = new Date();

    /**
     * 创建一个长整型主键实体
     */
    public LongIdEntity() {
    }

    /**
     * 创建一个长整型主键实体
     *
     * @param id 主键 id
     * @param creationTime 创建时间
     */
    public LongIdEntity(Long id, Date creationTime) {
        super(id);
        this.creationTime = creationTime;
    }

    @Override
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * 设置创建时间
     *
     * @param creationTime 创建时间
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * 创建一个整型主键实体
     *
     * @param id 主键 id
     * @param creationTime 创建时间
     */
    public static IntegerIdEntity of(Integer id, Date creationTime) {
        return new IntegerIdEntity(id, creationTime);
    }
}
