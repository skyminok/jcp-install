/**
 * RSAWrapUnwrapSessionKey.java,v $
 * version $
 * created 16.09.2020 17:17 by afevma
 * last modified $ by $
 * (C) ООО Крипто-Про 2004-2020.
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

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.tools.HexString;

import ru.CryptoPro.JCSP.JCSPRSA;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Пример экспорта и импорта симметричного сессионного ключа
 * (AES) на ключе RSA и пробных зашифрования и расшифрования
 * данных.
 *
 * This example demonstrates export and import (wrap
 * and unwrap) of AES key on RSA key and encryption
 * and decryption with AES key (JCSPRSA).
 *
 * Providers Java CSP RSA and CSP RSA are required.
 *
 * Используется провайдер {@link ru.CryptoPro.JCSP.JCSPRSA}
 * для работы с иностранными алгоритмами.
 *
 * Требуются провайдеры Java CSP RSA и CSP RSA.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class RSAWrapUnwrapSessionKey {

    /**
     * Данные для пробного зашифрования.
     * Source data to be encrypted.
     */
    public static final byte[] DATA = {
        'S', 'e', 'c', 'u', 'r', 'i', 't', 'y',
        'S', 'e', 'c', 'u', 'r', 'i', 't', 'y',
        'S', 'e', 'c', 'u', 'r', 'i', 't', 'y',
        'S', 'e', 'c', 'u', 'r', 'i', 't', 'y',
        'S', 'e', 'c', 'u', 'r', 'i', 't', 'y',
        'S', 'e', 'c', 'u', 'r', 'i', 't', 'y',
        'S', 'e', 'c', 'u', 'r', 'i', 't', 'y',
        'S', 'e', 'c', 'u', 'r', 'i', 't', 'y',
        'S', 'e', 'c',
    };

    /**
     * Тип ключевого контейнера с ключом
     * на алгоритме RSA.
     * Key store type of RSA key.
     */
    public static final String STORE_TYPE = JCSPRSA.HD_STORE_NAME;

    /**
     * Имя провайдера для загрузки ключевого контейнера.
     * Key store provider for loading of RSA key.
     */
    public static final String STORE_PROVIDER = JCSPRSA.PROVIDER_NAME;

    /**
     * Алиас ключевого контейнера с ключом на алгоритме RSA.
     * Alias of RSA key container.
     */
    public static final String ALIAS = "rsa_test";

    /**
     * Пароль к ключевому контейнеру с ключом на алгоритме RSA.
     * Password for RSA key container.
     */
    public static final char[] PASSWORD = "123456".toCharArray();

    /**
     * Имя алгоритма симметричного сессионного ключа
     * для пробного шифрования.
     * Алгоритм ключа: AES.
     * Algorithm of secret key: AES.
     */
    public static final String SESSION_KEY_ALGORITHM = JCSPRSA.AES_NAME;

    /**
     * Имя провайдера шифрования.
     * Name of cryptographic provider.
     */
    public static final String ENCRYPTION_PROVIDER = STORE_PROVIDER;

    /**
     * Имя алгоритма пробного шифрования на симметричном
     * сессионном ключе.
     * Алгоритм шифрования: AES.
     * Encryption algorithm: AES.
     * ECB mode is used, IV is not needed.
     */
    public static final String ENCRYPTION_ALGORITHM = SESSION_KEY_ALGORITHM + "/ECB/PKCS5Padding";

    /**
     * Имя алгоритма экспорта/импорта симметричного
     * сессионного ключа.
     * Алгоритм экспорта/импорта: RSA.
     * Export/import algorithm: RSA.
     */
    public static final String WRAP_ALGORITHM = JCP.RSA_NAME;

    /**
     * Пример.
     * Example.
     *
     * @param args Аргументы. Arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.out.println("DATA: " + HexString.toHex(DATA));

        // Добавление провайдеров.
        //
        // Adding java providers.

        JCPInit.initProviders(true); // JCSP по умолчанию. // JCSP is default.

        // Загрузка хранилища ключевых контейнеров.
        //
        // Loading of key containers.

        KeyStore keyStore = KeyStore.getInstance(STORE_TYPE, STORE_PROVIDER);
        keyStore.load(null, null);

        // Получение закрытого ключа и сертификата на
        // алгоритме RSA.
        //
        // Reading a key with RSA algorithm by alias
        // and password.

        JCPProtectionParameter rsaParameter = new JCPProtectionParameter(PASSWORD);
        JCPPrivateKeyEntry rsaEntry = (JCPPrivateKeyEntry)keyStore.getEntry(ALIAS, rsaParameter);

        PrivateKey rsaPrivateKey = rsaEntry.getPrivateKey();
        X509Certificate rsaCert  = (X509Certificate) rsaEntry.getCertificate();

        System.out.println("RSA private key: " + rsaPrivateKey);
        System.out.println("RSA public key: " + rsaCert.getPublicKey());

        // Генерация симметричного сессионного ключа
        // на алгоритме AES с длиной 128.
        //
        // Generating a secret key (AES with length 128
        // bits) for key encryption.

        KeyGenerator kg = KeyGenerator.getInstance(SESSION_KEY_ALGORITHM, ENCRYPTION_PROVIDER);
        kg.init(128); // длина ключа

        SecretKey aesSecretKey = kg.generateKey();
        System.out.println("Secret key: " + aesSecretKey + " with algorithm: " + aesSecretKey.getAlgorithm());

        // Инициализация шифратора на алгоритме AES для
        // пробного шифрования на созданном симметричном
        // сессионном ключе.
        //
        // Encrypting data.

        Cipher encrypt = Cipher.getInstance(ENCRYPTION_ALGORITHM, ENCRYPTION_PROVIDER);

        // Шифрование на созданном симметричном сессионном
        // ключе (AES).

        encrypt.init(Cipher.ENCRYPT_MODE, aesSecretKey);
        byte[] encryptedText = encrypt.doFinal(DATA, 0, DATA.length);

        System.out.println("Encrypted text: " + HexString.toHex(encryptedText));

        // Инициализация шифратора на алгоритме RSA для
        // экспорта созданного раннее симметричного
        // сессионного ключа (AES).
        //
        // Exporting secret key.

        Cipher wrapper = Cipher.getInstance(WRAP_ALGORITHM, JCSPRSA.PROVIDER_NAME);

        // Экспорт симметричного сессионного ключа
        // в блоб.

        wrapper.init(Cipher.WRAP_MODE, rsaCert);
        byte[] wrapped = wrapper.wrap(aesSecretKey);

        System.out.println("WRAPPED session key: " + HexString.toHex(wrapped));

        // Инициализация де-шифратора на алгоритме RSA для
        // импорта экспортированного раннее в блоб симметричного
        // сессионного ключа (AES).
        //
        // Importing secret key.

        Cipher unWrapped = Cipher.getInstance(WRAP_ALGORITHM, ENCRYPTION_PROVIDER);

        // Импорт из блоба в симметричный сессионный ключ.

        unWrapped.init(Cipher.UNWRAP_MODE, rsaPrivateKey);
        SecretKey unWrappedSecretKey = (SecretKey) unWrapped.unwrap(wrapped, null, Cipher.SECRET_KEY);

        System.out.println("UN-WRAPPED session key: " +
            unWrappedSecretKey + " with algorithm: " +
                unWrappedSecretKey.getAlgorithm());

        // Инициализация де-шифратора на алгоритме AES для
        // пробного расшифрования на импортированном симметричном
        // сессионном ключе (AES).
        //
        // Decrypting data.

        Cipher decrypt = Cipher.getInstance(ENCRYPTION_ALGORITHM, ENCRYPTION_PROVIDER);

        // Расшифрование на импортированном симметричном
        // сессионном ключе (AES).

        decrypt.init(Cipher.DECRYPT_MODE, unWrappedSecretKey);

        // Проверка на соответствие исходных данных и
        // расшифрованных.
        //
        // Checking messages.

        byte[] decryptedText = decrypt.doFinal(encryptedText, 0, encryptedText.length);
        System.out.println("Decrypted text: " + HexString.toHex(decryptedText));

        if (DATA.length != decryptedText.length) {
            throw new Exception("Invalid decrypted data size!");
        } // if

        if (Arrays.equals(DATA, decryptedText)) {
            System.out.println("Data encrypted and decrypted successfully.");
        } // if
        else {
            throw new Exception("Invalid decrypted data!");
        } // else

    }

}
