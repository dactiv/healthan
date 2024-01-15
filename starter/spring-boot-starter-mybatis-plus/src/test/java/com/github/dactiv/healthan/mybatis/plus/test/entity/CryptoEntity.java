package com.github.dactiv.healthan.mybatis.plus.test.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.dactiv.healthan.commons.id.BasicIdentification;
import com.github.dactiv.healthan.mybatis.plus.annotation.Decryption;
import com.github.dactiv.healthan.mybatis.plus.annotation.Encryption;
import com.github.dactiv.healthan.mybatis.plus.crypto.DataAesCryptoService;

@TableName(value = "tb_crypto_entity", autoResultMap = true)
public class CryptoEntity implements BasicIdentification<Integer> {

    private Integer id;

    @Decryption
    @Encryption(serviceClass = DataAesCryptoService.class)
    private String cryptoValue;

    public CryptoEntity() {
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getCryptoValue() {
        return cryptoValue;
    }

    public void setCryptoValue(String cryptoValue) {
        this.cryptoValue = cryptoValue;
    }
}
