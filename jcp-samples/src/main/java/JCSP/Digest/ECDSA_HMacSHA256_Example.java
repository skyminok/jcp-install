package JCSP.Digest;

import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.JCSPECDSA;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Пример создания секретного ключа на алгоритме AES (256) и вычисления HMac SHA256 с использованием
 * криптопровайдера ECDSA.
 */
public class ECDSA_HMacSHA256_Example {

    interface SecretKeyGenerator {
        SecretKey create();
    }

    // Параметры примера.
    static class ExampleParameters {
        final String providerName; // имя java-криптопровайдера
        final SecretKey secretKey; // ключ
        final String macAlgorithmName; // имя алгоритма HMac
        ExampleParameters(String providerName, SecretKeyGenerator secretKeyGenerator, String macAlgorithmName) {
            this.providerName = providerName;
            this.secretKey = secretKeyGenerator.create();
            this.macAlgorithmName = macAlgorithmName;
        }
    }

    private static void test(ExampleParameters exampleParameters, byte[] data) throws Exception {
        // Вычисление HMac.
        Mac mac = Mac.getInstance(exampleParameters.macAlgorithmName, exampleParameters.providerName);
        mac.init(exampleParameters.secretKey);
        mac.update(data);
        byte[] hMac = mac.doFinal();
        if (mac.getMacLength() != hMac.length) {
            throw new Exception("Invalid HMac length.");
        }
    }

    // Генерация ключа.
    static class GeneratedSecretKey implements SecretKeyGenerator {
        final String providerName;
        final String keyAlgorithmName;
        final int keyLength;
        GeneratedSecretKey(String providerName, String keyAlgorithmName, int keyLength) {
            this.providerName = providerName;
            this.keyAlgorithmName = keyAlgorithmName;
            this.keyLength = keyLength;
        }
        @Override
        public SecretKey create() {
            try {
                KeyGenerator kg = KeyGenerator.getInstance(keyAlgorithmName, providerName);
                if (keyLength > 0) {
                    kg.init(keyLength);
                }
                return kg.generateKey();
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    // Создание ключа из массива байтов.
    static class ByteArraySecretKey implements SecretKeyGenerator {
        final byte[] key;
        final String keyAlgorithmName;
        ByteArraySecretKey(byte[] key, String keyAlgorithmName) {
            this.key = key.clone();
            this.keyAlgorithmName = keyAlgorithmName;
        }
        @Override
        public SecretKey create() {
            return new SecretKeySpec(key, keyAlgorithmName);
        }
    }

    static ExampleParameters sha256ExampleParameters = new ExampleParameters(JCSPECDSA.PROVIDER_NAME, () -> {
        try {
            // AES 256.
            // return new GeneratedSecretKey(JCSPECDSA.PROVIDER_NAME, JCSPECDSA.AES_NAME, 256).create();
            return new ByteArraySecretKey(new byte[32], JCSPECDSA.AES_NAME).create();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }, JCSP.HMAC_SHA256_NAME);

    public static void main(String[] args) throws Exception {
        // В случае Java 11+ нужно добавить криптопровайдеры, если они не добавлены в java.security.
        // Security.addProvider(new JCSPECDSA());
        final byte[] message = { (byte)0xAB, (byte)0xCD, (byte)0xEF, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90 };
        test(sha256ExampleParameters, message);
    }
}
