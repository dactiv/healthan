
package com.github.dactiv.healthan.crypto.algorithm.test.hash;

import com.github.dactiv.healthan.crypto.algorithm.SimpleByteSource;
import com.github.dactiv.healthan.crypto.algorithm.exception.CodecException;
import com.github.dactiv.healthan.crypto.algorithm.exception.UnknownAlgorithmException;
import com.github.dactiv.healthan.crypto.algorithm.hash.Hash;
import com.github.dactiv.healthan.crypto.algorithm.hash.HashAlgorithmMode;
import com.github.dactiv.healthan.crypto.algorithm.test.TestData;
import org.junit.Assert;
import org.junit.Test;

/**
 * hash 单元测试
 *
 * @author maurice
 */
public class HashTest {

    @Test
    public void testError() {
        try {
            new Hash(null, null);
        } catch (Exception e) {
            Assert.assertTrue((e instanceof IllegalArgumentException));
        }

        try {
            new Hash("", null);
        } catch (Exception e) {
            Assert.assertTrue((e instanceof IllegalArgumentException));
        }

        try {
            new Hash("33940", null);
        } catch (Exception e) {
            Assert.assertTrue((e instanceof IllegalArgumentException));
        }

        try {
            new Hash("33940", "");
        } catch (Exception e) {
            Assert.assertTrue((e instanceof UnknownAlgorithmException));
        }

        try {
            new Hash("33940", "45345");
        } catch (Exception e) {
            Assert.assertTrue((e instanceof UnknownAlgorithmException));
        }
    }

    @Test
    public void testSuccess() {
        assertAlgorithmName(HashAlgorithmMode.MD5.getName(), 32);
        assertAlgorithmName(HashAlgorithmMode.SHA1.getName(), 40);
        assertAlgorithmName(HashAlgorithmMode.SHA256.getName(), 64);
        assertAlgorithmName(HashAlgorithmMode.SHA384.getName(), 96);
        assertAlgorithmName(HashAlgorithmMode.SHA512.getName(), 128);
    }

    private void assertAlgorithmName(String algorithmName, int actualLength) {
        Hash stringSource = new Hash(algorithmName, TestData.TEXT, TestData.SALT, 2);
        Hash stringTarget = new Hash(algorithmName, TestData.TEXT, TestData.SALT, 2);

        TestData.assertHash(stringSource, stringTarget, actualLength);

        Hash byteSource = new Hash(algorithmName, TestData.TEXT.getBytes(), TestData.SALT, 2);
        Hash byteTarget = new Hash(algorithmName, TestData.TEXT.getBytes(), TestData.SALT, 2);

        TestData.assertHash(byteSource, byteTarget, actualLength);

        Hash charSource = new Hash(algorithmName, TestData.TEXT.toCharArray(), TestData.SALT, 2);
        Hash charTarget = new Hash(algorithmName, TestData.TEXT.toCharArray(), TestData.SALT, 2);

        TestData.assertHash(charSource, charTarget, actualLength);

        Hash byteObjectSource = new Hash(algorithmName, new SimpleByteSource(TestData.TEXT), TestData.SALT, 2);
        Hash byteObjectTarget = new Hash(algorithmName, new SimpleByteSource(TestData.TEXT), TestData.SALT, 2);

        TestData.assertHash(byteObjectSource, byteObjectTarget, actualLength);

        try {
            new Hash(algorithmName, new Object());
        } catch (Exception e) {
            Assert.assertTrue((e instanceof CodecException));
        }
    }

}
