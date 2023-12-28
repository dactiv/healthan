package com.github.dactiv.healthan.mybatis.plus;

import com.github.dactiv.healthan.mybatis.plus.annotation.Decryption;
import com.github.dactiv.healthan.mybatis.plus.annotation.Encryption;

/**
 * 加解密 null 类实现，用于在 {@link Decryption}
 * 和 {@link Encryption} 中 default 编译不报错使用
 *
 * @author maurice.chen
 */
public class CryptoNullClass implements DecryptService, EncryptService {

    @Override
    public String decrypt(String cipherText) {
        throw new UnsupportedOperationException("CryptoNullClass 为空对象在注解使用，不支持解密操作");
    }

    @Override
    public String encrypt(String plainText) {
        throw new UnsupportedOperationException("CryptoNullClass 为空对象在注解使用，不支持加密操作");
    }
}
