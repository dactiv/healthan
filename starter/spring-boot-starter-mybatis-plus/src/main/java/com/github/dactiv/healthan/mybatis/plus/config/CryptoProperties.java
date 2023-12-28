package com.github.dactiv.healthan.mybatis.plus.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 加解密配置
 *
 * @author maurice.chen
 *
 */
@ConfigurationProperties("healthan.mybatis.plus.crypto")
public class CryptoProperties {

    /**
     * aes 加密 key
     */
    private String dataAesCryptoKey;

    /**
     * rsa 加密 key
     */
    private String dataRasCryptoPublicKey;

    /**
     * rsa 解密 key
     */
    private String dataRasCryptoPrivateKey;

    public CryptoProperties() {

    }

    public String getDataAesCryptoKey() {
        return dataAesCryptoKey;
    }

    public void setDataAesCryptoKey(String dataAesCryptoKey) {
        this.dataAesCryptoKey = dataAesCryptoKey;
    }

    public String getDataRasCryptoPublicKey() {
        return dataRasCryptoPublicKey;
    }

    public void setDataRasCryptoPublicKey(String dataRasCryptoPublicKey) {
        this.dataRasCryptoPublicKey = dataRasCryptoPublicKey;
    }

    public String getDataRasCryptoPrivateKey() {
        return dataRasCryptoPrivateKey;
    }

    public void setDataRasCryptoPrivateKey(String dataRasCryptoPrivateKey) {
        this.dataRasCryptoPrivateKey = dataRasCryptoPrivateKey;
    }
}
