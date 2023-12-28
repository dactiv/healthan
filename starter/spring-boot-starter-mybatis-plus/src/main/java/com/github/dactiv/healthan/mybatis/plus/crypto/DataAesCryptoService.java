package com.github.dactiv.healthan.mybatis.plus.crypto;

import com.github.dactiv.healthan.crypto.algorithm.Base64;
import com.github.dactiv.healthan.crypto.algorithm.ByteSource;
import com.github.dactiv.healthan.crypto.algorithm.CodecUtils;
import com.github.dactiv.healthan.crypto.algorithm.cipher.AesCipherService;
import com.github.dactiv.healthan.mybatis.plus.DecryptService;
import com.github.dactiv.healthan.mybatis.plus.EncryptService;

/**
 * aes 的数据加解密服务实现
 *
 * @author maurice.chen
 */
public class DataAesCryptoService implements DecryptService, EncryptService {

    private final AesCipherService aesCipherService;

    private final String key;

    public DataAesCryptoService(AesCipherService aesCipherService, String key) {
        this.aesCipherService = aesCipherService;
        this.key = key;
    }

    @Override
    public String decrypt(String cipherText) {
        ByteSource byteSource = aesCipherService.decrypt(Base64.decode(cipherText), Base64.decode(key));
        return byteSource.obtainString();
    }

    @Override
    public String encrypt(String plainText) {
        ByteSource byteSource = aesCipherService.encrypt(CodecUtils.toBytes(plainText), Base64.decode(key));
        return byteSource.getBase64();
    }
}
