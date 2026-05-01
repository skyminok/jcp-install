package JCSP;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.spec.NameAlgIdSpecForeign;

import ru.CryptoPro.JCSP.JCSPECDSA;
import ru.CryptoPro.JCSP.JCSPEDDSA;
import ru.CryptoPro.JCSP.params.PasswordParamsSpec;

import java.security.*;
import java.security.spec.ECGenParameterSpec;

/**
 * Пример создания ключа и подписи с использованием алгоритмов криптопровайдеров ECDSA и EdDSA.
 */
public class ECDSA_EdDSA_Example {

    // Параметры примера.
    static class ExampleParameters {
        final String providerName; // имя java-криптопровайдера
        final String keyAlgorithmName; // имя алгоритма ключа
        final String curveName; // имя кривой (может быть null)
        final String keyStoreType; // имя хранилища ключей
        final String containerAlias; // имя ключевого контейнера для ключа
        final char[] containerPassword; // пароль к ключевому контейнеру
        final String signatureAlgorithmName; // имя алгоритма подписи
        ExampleParameters(String providerName, String keyAlgorithmName, String curveName, String keyStoreType,
            String containerAlias, char[] containerPassword, String signatureAlgorithmName) {
            this.providerName = providerName;
            this.keyAlgorithmName = keyAlgorithmName;
            this.curveName = curveName;
            this.keyStoreType = keyStoreType;
            this.containerAlias = containerAlias;
            this.containerPassword = containerPassword;
            this.signatureAlgorithmName = signatureAlgorithmName;
        }
    }

    // Параметры примера использования ECDSA криптопровайдера.
    static ExampleParameters ecDsaExampleParameters = new ExampleParameters(JCSPECDSA.PROVIDER_NAME, JCP.ECDSA_NAME,
        "secp256k1", "HDIMAGE", "test_cont_ecdsa", "12345678".toCharArray(), JCP.SIGN_SHA256_ECDSA_NAME);

    // Параметры примера использования EdDSA криптопровайдера.
    static ExampleParameters edDsaExampleParameters = new ExampleParameters(JCSPEDDSA.PROVIDER_NAME, JCP.EDDSA_NAME,
        null, "HDIMAGE", "test_cont_eddsa", "12345678".toCharArray(), JCP.SIGN_EDDSA_NAME);

    private static void test(ExampleParameters exampleParameters, byte[] data) throws Exception {
        // Хранилище ключей.
        KeyStore keyStore = KeyStore.getInstance(exampleParameters.keyStoreType, exampleParameters.providerName);
        keyStore.load(null, null);
        try {
            // Удаление тестового ключа.
            keyStore.deleteEntry(exampleParameters.containerAlias);
        } catch (Exception e) {}
        // Генерация ключа.
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(exampleParameters.keyAlgorithmName, exampleParameters.providerName);
        if ((exampleParameters.keyAlgorithmName.equals(JCP.ECDSA_NAME) || exampleParameters.keyAlgorithmName.equals(JCP.ECDH_NAME)) && exampleParameters.curveName != null) {
            ECGenParameterSpec spec = new ECGenParameterSpec(exampleParameters.curveName);
            kpg.initialize(spec);
        }
        // Место создания ключа.
        NameAlgIdSpecForeign placeSpec = new NameAlgIdSpecForeign(String.format("\\\\.\\%s\\%s", exampleParameters.keyStoreType, exampleParameters.containerAlias));
        kpg.initialize(placeSpec);
        // Пароль к ключу.
        PasswordParamsSpec passSpec = new PasswordParamsSpec(exampleParameters.containerPassword);
        kpg.initialize(passSpec);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey publicKey = kp.getPublic();
        // Чтение ключа из контейнера.
        // Контейнер не содержит сертификата, поэтому последний параметр JCPProtectionParameter - true.
        JCPProtectionParameter parameter = new JCPProtectionParameter(exampleParameters.containerPassword, true, true);
        JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) keyStore.getEntry(exampleParameters.containerAlias, parameter);
        PrivateKey privateKey = entry.getPrivateKey();
        // Проверка с помощью подписи.
        Signature signature = Signature.getInstance(exampleParameters.signatureAlgorithmName, exampleParameters.providerName);
        signature.initSign(privateKey);
        signature.update(data);
        byte[] sign = signature.sign();
        // Проверка подписи.
        signature = Signature.getInstance(exampleParameters.signatureAlgorithmName, exampleParameters.providerName);
        signature.initVerify(publicKey);
        signature.update(data);
        signature.verify(sign);
    }

    public static void main(String[] args) throws Exception {
        // В случае Java 11+ нужно добавить криптопровайдеры, если они не добавлены в java.security.
        // Security.addProvider(new JCSPECDSA());
        // Security.addProvider(new JCSPEDDSA());
        final byte[] message = { (byte)0xAB, (byte)0xCD, (byte)0xEF, (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78, (byte)0x90 };
        test(ecDsaExampleParameters, message);
        test(edDsaExampleParameters, message);
    }
}
