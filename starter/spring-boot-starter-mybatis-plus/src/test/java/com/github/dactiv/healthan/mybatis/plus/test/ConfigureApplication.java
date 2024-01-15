package com.github.dactiv.healthan.mybatis.plus.test;

import com.github.dactiv.healthan.crypto.algorithm.cipher.AesCipherService;
import com.github.dactiv.healthan.crypto.algorithm.cipher.OperationMode;
import com.github.dactiv.healthan.mybatis.plus.crypto.DataAesCryptoService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConfigureApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigureApplication.class, args);
    }

    @Bean("aesEcbCryptoService")
    public DataAesCryptoService aesEcbCryptoService() {
        AesCipherService aesCipherService = new AesCipherService();
        aesCipherService.setInitializationVectorSize(0);
        aesCipherService.setMode(OperationMode.ECB);
        aesCipherService.setRandomNumberGenerator(null);
        return new DataAesCryptoService(aesCipherService, "jmUFt7sqMPXf+c8w69OpIg==");
    }

    @Bean
    public AesCipherService getEcbPkcs5AesCipherService() {

        AesCipherService aesCipherService = new AesCipherService();
        aesCipherService.setInitializationVectorSize(0);
        aesCipherService.setMode(OperationMode.ECB);
        aesCipherService.setRandomNumberGenerator(null);

        return aesCipherService;
    }
}
