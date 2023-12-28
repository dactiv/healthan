package com.github.dactiv.healthan.mybatis.plus.crypto;

import com.github.dactiv.healthan.crypto.algorithm.Base64;
import com.github.dactiv.healthan.crypto.algorithm.ByteSource;
import com.github.dactiv.healthan.crypto.algorithm.CodecUtils;
import com.github.dactiv.healthan.crypto.algorithm.cipher.RsaCipherService;
import com.github.dactiv.healthan.mybatis.plus.DecryptService;
import com.github.dactiv.healthan.mybatis.plus.EncryptService;

public class DataRsaCryptoService implements DecryptService, EncryptService {

    private final RsaCipherService rsaCipherService;

    private final String publicKey;

    private final String privateKey;

    public DataRsaCryptoService(RsaCipherService rsaCipherService, String publicKey, String privateKey) {
        this.rsaCipherService = rsaCipherService;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public String decrypt(String cipherText) {
        ByteSource byteSource = rsaCipherService.decrypt(Base64.decode(cipherText), Base64.decode(privateKey));
        return byteSource.obtainString();
    }

    @Override
    public String encrypt(String plainText) {
        ByteSource byteSource = rsaCipherService.encrypt(CodecUtils.toBytes(plainText), Base64.decode(publicKey));
        return byteSource.getBase64();
    }
}
