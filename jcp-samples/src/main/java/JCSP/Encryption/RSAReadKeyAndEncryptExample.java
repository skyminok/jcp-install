/**
 * RSAReadKeyAndEncryptExample.java,v $
 * version $
 * created 05.06.2021 14:34 by afevma
 * last modified $ by $
 * (C) ООО Крипто-Про 2004-2021.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.Encryption;

import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.tools.HexString;

import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.JCSPRSA;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Пример шифрования на ключе с алгоритмом RSA.
 * Требуются провайдеры Java CSP RSA и CSP RSA.
 *
 * This example demonstrates encryption and decryption
 * with RSA key.
 * Providers Java CSP RSA and CSP RSA are required.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class RSAReadKeyAndEncryptExample {

    // Данные для шифрования размером не больше длины ключа в байтах.
    // Source data to be encrypted with size less than the key size.
    private static final byte[] DATA = "security".getBytes();

    // Тип хранилища.
    // Key store type.
    private static final String KEY_STORE_TYPE = JCSP.HD_STORE_NAME;

    // Алиас ключа.
    // Key alias.
    private static final String ALIAS = "rsa-2048-test";

    // Пароль к ключу.
    // Password to key store.
    private static final char[] PASSWORD = "123".toCharArray();

    // Алгоритм шифрования.
    // Encryption algorithm.
    private static final String ENCRYPTION_ALGORITHM = "RSA/0/X509Padding";

    // Сертификат с открытым ключом для зашифрования данных.
    // Открытый ключ образует пару с закрытым ключом в ключевом
    // контейнере {@link #ALIAS}.
    // A certificate with public key for data encrypting.
    // The public key is paired to the private key of the key
    // container {@link #ALIAS}.
    private static final String RSA_CERT =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIID0DCCA3+gAwIBAgITEgBUkYzGnc2HhrBikwABAFSRjDAIBgYqhQMCAgMwfzEj\n" +
        "MCEGCSqGSIb3DQEJARYUc3VwcG9ydEBjcnlwdG9wcm8ucnUxCzAJBgNVBAYTAlJV\n" +
        "MQ8wDQYDVQQHEwZNb3Njb3cxFzAVBgNVBAoTDkNSWVBUTy1QUk8gTExDMSEwHwYD\n" +
        "VQQDExhDUllQVE8tUFJPIFRlc3QgQ2VudGVyIDIwHhcNMjEwNjA1MTEzMTI3WhcN\n" +
        "MjEwOTA1MTE0MTI3WjAYMRYwFAYDVQQDDA1yc2EtMjA0OC10ZXN0MIIBIjANBgkq\n" +
        "hkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzK9l751GjPbHcevQTEvUiI/PBzTLNOpR\n" +
        "5PiAtoP4Q2OuEmUHe5+DGe+uXHUHKa71KddSV1MONngLQxHSdkV649YKthqXF8Jk\n" +
        "kHM9HidrE7G05Ko2oizI4PamCBJWYx+2F+yqNiFQOd5ISSrZCxhogy8LwlRtJhz4\n" +
        "HgoC/f7G3Y3PM3T3fX3eSBITbDlhWOjxWcSyhDelsw4IeFLPEllGIDcXQ9tm89vb\n" +
        "Yru2DnRKgvmo05B5f2KOqdCLxuV1fBa60Zg9of3gcPAoCDX1ppa7V3xpjRRk1FJH\n" +
        "EDVWcta383yCVo0ErFcXff+P9SFP9k4MW/xwb0fSj/CSRZ7BufKzwwIDAQABo4IB\n" +
        "djCCAXIwDgYDVR0PAQH/BAQDAgTwMBMGA1UdJQQMMAoGCCsGAQUFBwMCMB0GA1Ud\n" +
        "DgQWBBQN+y8CWFmE+J0jhob6cHnWXl4fVjAfBgNVHSMEGDAWgBROgz4Uae/sXXqV\n" +
        "K18R/jcyFklVKzBcBgNVHR8EVTBTMFGgT6BNhktodHRwOi8vdGVzdGNhLmNyeXB0\n" +
        "b3Byby5ydS9DZXJ0RW5yb2xsL0NSWVBUTy1QUk8lMjBUZXN0JTIwQ2VudGVyJTIw\n" +
        "MigxKS5jcmwwgawGCCsGAQUFBwEBBIGfMIGcMGQGCCsGAQUFBzAChlhodHRwOi8v\n" +
        "dGVzdGNhLmNyeXB0b3Byby5ydS9DZXJ0RW5yb2xsL3Rlc3QtY2EtMjAxNF9DUllQ\n" +
        "VE8tUFJPJTIwVGVzdCUyMENlbnRlciUyMDIoMSkuY3J0MDQGCCsGAQUFBzABhiho\n" +
        "dHRwOi8vdGVzdGNhLmNyeXB0b3Byby5ydS9vY3NwL29jc3Auc3JmMAgGBiqFAwIC\n" +
        "AwNBAMkXzD/pwhotVF3bbbXLQr5T/BQU+GUOz3HcMbN8ciW6iyyFjbi1i4kQgO7j\n" +
        "N9+CRrfRk/gMggJVGOTRyOR67wE=\n" +
        "-----END CERTIFICATE-----\n";

    /**
     * Зашифрование.
     *
     * Encryption.
     *
     * @param rsaPublicKey Открытый ключ. Public key.
     * @param data Данные для зашифрования. Data to be encrypted.
     * @return зашифрованные данные/encrypted data.
     * @throws Exception
     */
    public static byte[] encrypt(PublicKey rsaPublicKey, byte[] data) throws Exception {

        System.out.println("Encrypting using " + ENCRYPTION_ALGORITHM + "...");

        Cipher encCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, JCSPRSA.PROVIDER_NAME);
        encCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);

        byte[] encrypted = encCipher.doFinal(data, 0, data.length);
        System.out.println("ENCRYPTED: " + HexString.toHexNoSpaces(encrypted));

        return encrypted;

    }

    /**
     * Расшифрование.
     *
     * Decryption.
     *
     * @param rsaPrivateKey Закрытый ключ. Private key.
     * @param encrypted Зашифрованные данные. Encrypted data.
     * @return расшифрованные данные/decrypted data.
     * @throws Exception
     */
    public static byte[] decrypt(PrivateKey rsaPrivateKey, byte[] encrypted) throws Exception {

        System.out.println("Decrypting using " + ENCRYPTION_ALGORITHM + "...");

        Cipher decCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM, JCSPRSA.PROVIDER_NAME);
        decCipher.init(Cipher.DECRYPT_MODE, rsaPrivateKey);

        byte[] decrypted = decCipher.doFinal(encrypted, 0, encrypted.length);
        System.out.println("DECRYPTED: " + HexString.toHexNoSpaces(decrypted));

        return decrypted;

    }

    /**
     * Запуск примера.
     *
     * Function runs the example.
     *
     * @param args Аргументы. Arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.out.println("DATA: " + HexString.toHexNoSpaces(DATA));
        System.out.println("Reading certificate...");

        // Зашифрование с помощью открытого ключа из сертификата.
        //
        // Encrypting using a public key from the certificate.

        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        X509Certificate rsaCertificate = (X509Certificate) factory
            .generateCertificate(new ByteArrayInputStream(RSA_CERT.getBytes()));

        byte[] encrypted = encrypt(rsaCertificate.getPublicKey(), DATA);

        // Загрузка хранилища с ключом (и сертификатом).
        //
        // Loading a key store.

        System.out.println("Loading key store...");
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE, JCSPRSA.PROVIDER_NAME);

        System.out.println("Reading RSA key...");
        keyStore.load(null, null);

        // Допускаем чтение только закрытого ключа,
        // без сертификата.
        //
        // Allow reading a key  without certificate.

        JCPProtectionParameter parameter = new JCPProtectionParameter(PASSWORD, true, true); // 3. allow = true
        System.out.println("Getting public key...");

        JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry)keyStore.getEntry(ALIAS, parameter);
        PrivateKey rsaPrivateKey = entry.getPrivateKey();

        // Расшифрование зашифрованного сообщения с помощью закрытого
        // ключа.
        //
        // Decrypting encrypted message using the private key.

        byte[] decrypted = decrypt(rsaPrivateKey, encrypted);

        if (DATA.length != decrypted.length || !Arrays.equals(DATA, decrypted)) {
            throw new Exception("Decrypting failed.");
        } // if

        System.out.println("Completed.");

    }

}
