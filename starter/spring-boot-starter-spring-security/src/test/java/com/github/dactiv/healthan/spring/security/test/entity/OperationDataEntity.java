package com.github.dactiv.healthan.spring.security.test.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.healthan.mybatis.plus.baisc.support.IntegerVersionEntity;

@TableName(value = "tb_operation_data", autoResultMap = true)
public class OperationDataEntity extends IntegerVersionEntity<Integer> {

    private String name;

    public OperationDataEntity() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
