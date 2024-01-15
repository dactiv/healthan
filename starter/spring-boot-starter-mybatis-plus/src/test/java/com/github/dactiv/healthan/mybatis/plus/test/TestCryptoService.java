package com.github.dactiv.healthan.mybatis.plus.test;

import com.github.dactiv.healthan.mybatis.plus.crypto.DataAesCryptoService;
import com.github.dactiv.healthan.mybatis.plus.test.entity.CryptoEntity;
import com.github.dactiv.healthan.mybatis.plus.test.service.CryptoEntityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestCryptoService {

    @Autowired
    private CryptoEntityService cryptoEntityService;

    @Autowired
    private DataAesCryptoService dataAesCryptoService;

    @Test
    public void testEncrypt() {
        CryptoEntity entity = new CryptoEntity();
        entity.setCryptoValue("18776974353");
        cryptoEntityService.save(entity);
        Assertions.assertEquals(dataAesCryptoService.decrypt(entity.getCryptoValue()), "18776974353");

        entity = cryptoEntityService.lambdaQuery().eq(CryptoEntity::getCryptoValue, "18776974353").eq(CryptoEntity::getId, "1").one();
        Assertions.assertNotNull(entity);
        Assertions.assertEquals(entity.getCryptoValue(), "18776974353");
    }
}
