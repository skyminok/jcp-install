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
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.HexString;

import ru.CryptoPro.JCSP.JCSPRSA;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Пример экспорта и импорта симметричного ключа AES
 * на симметричном ключе AES и пробных зашифрования
 * и расшифрования данных.
 *
 * This example demonstrates export and import (wrap
 * and unwrap) of AES key on another AES key and
 * encryption and decryption with AES key (JCSPRSA).
 *
 * Providers Java CSP RSA and CSP RSA are required.
 *
 * Используется провайдер {@link JCSPRSA}
 * для работы с иностранными алгоритмами.
 *
 * Требуются провайдеры Java CSP RSA и CSP RSA.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class AESWrapUnwrapKey {

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
        'S', 'e', 'c', 'u', 'r', 'i', 't', 'y'
    };

    /**
     * Имя алгоритма симметричного ключа
     * для пробного шифрования.
     * Алгоритм ключа: AES.
     * Algorithm of secret key: AES.
     */
    public static final String SECRET_KEY_ALGORITHM = JCSPRSA.AES_NAME;

    /**
     * Имя провайдера шифрования.
     * Name of cryptographic provider.
     */
    public static final String ENCRYPTION_PROVIDER = JCSPRSA.PROVIDER_NAME;

    /**
     * Имя алгоритма пробного шифрования на симметричном
     * сессионном ключе.
     * Алгоритм шифрования: AES.
     * Encryption algorithm: AES.
     * CBC mode is used, IV is required.
     * NoPadding is used, encrypting data must have a length
     * which multiples of block size.
     */
    public static final String ENCRYPTION_ALGORITHM = SECRET_KEY_ALGORITHM + "/CBC/NoPadding";

    /**
     * Имя алгоритма экспорта/импорта симметричного
     * сессионного ключа.
     * Алгоритм экспорта/импорта: AES.
     * Export/import algorithm: AES.
     */
    public static final String WRAP_ALGORITHM = JCSPRSA.AES_NAME;

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

        // Генерация симметричного ключа на алгоритме AES
        // с длиной 128 для зашифрования данных.
        //
        // Generating a secret key (AES with length 128
        // bits) for data encryption.

        KeyGenerator kg = KeyGenerator.getInstance(SECRET_KEY_ALGORITHM, ENCRYPTION_PROVIDER);
        kg.init(128); // длина ключа // key length

        SecretKey aesEncSecretKey = kg.generateKey();

        System.out.println("Secret encrypt key: " + aesEncSecretKey +
            " with algorithm: " + aesEncSecretKey.getAlgorithm());

        // Генерация симметричного ключа на алгоритме AES
        // с длиной 128 для зашифрования ключа.
        //
        // Generating a secret key (AES with length 128
        // bits) for key encryption.

        KeyGenerator kgWrap = KeyGenerator.getInstance(SECRET_KEY_ALGORITHM, ENCRYPTION_PROVIDER);
        kgWrap.init(128); // длина ключа // key length

        SecretKey aesWrapSecretKey = kgWrap.generateKey();

        System.out.println("Secret wrapping key: " + aesEncSecretKey +
            " with algorithm: " + aesEncSecretKey.getAlgorithm());

        // Генерация вектора инициализации для зашифрования данных.
        //
        // Creating initialization vector (IV) for encryption.
        // CBC mode is used, IV is required.

        final SecureRandom rnd = SecureRandom.getInstance(JCP.CP_RANDOM, JCSPRSA.PROVIDER_NAME);
        byte[] iv_enc = new byte[16]; // for AES key with length 128

        rnd.nextBytes(iv_enc);
        IvParameterSpec params_enc = new IvParameterSpec(iv_enc);

        // Инициализация шифратора на алгоритме AES для
        // пробного шифрования на созданном симметричном
        // ключе.
        //
        // Encrypting data.

        Cipher encrypt = Cipher.getInstance(ENCRYPTION_ALGORITHM, ENCRYPTION_PROVIDER);

        // Шифрование на созданном симметричном сессионном
        // ключе (AES).

        encrypt.init(Cipher.ENCRYPT_MODE, aesEncSecretKey, params_enc);
        byte[] encryptedText = encrypt.doFinal(DATA, 0, DATA.length);

        System.out.println("Encrypted text: " + HexString.toHex(encryptedText));

        // Инициализация шифратора на алгоритме AES для
        // экспорта созданного раннее симметричного
        // ключа (AES).
        //
        // Exporting secret key.

        Cipher wrapper = Cipher.getInstance(WRAP_ALGORITHM, JCSPRSA.PROVIDER_NAME);

        // Экспорт симметричного ключа в блоб.

        wrapper.init(Cipher.WRAP_MODE, aesWrapSecretKey);
        byte[] wrapped = wrapper.wrap(aesEncSecretKey);

        System.out.println("WRAPPED key: " + HexString.toHex(wrapped));

        // Инициализация де-шифратора на алгоритме AES для
        // импорта экспортированного раннее в блоб симметричного
        // сессионного ключа (AES).
        //
        // Importing secret key.

        Cipher unWrapped = Cipher.getInstance(WRAP_ALGORITHM, ENCRYPTION_PROVIDER);

        // Импорт из блоба в симметричный ключ.

        unWrapped.init(Cipher.UNWRAP_MODE, aesWrapSecretKey);
        SecretKey unWrappedSecretKey = (SecretKey) unWrapped.unwrap(wrapped, null, Cipher.SECRET_KEY);

        System.out.println("UN-WRAPPED key: " +
            unWrappedSecretKey + " with algorithm: " +
                unWrappedSecretKey.getAlgorithm());

        // Инициализация де-шифратора на алгоритме AES для
        // пробного расшифрования на импортированном симметричном
        // сессионном ключе (AES).
        //
        // Decrypting data.

        Cipher decrypt = Cipher.getInstance(ENCRYPTION_ALGORITHM, ENCRYPTION_PROVIDER);

        // Расшифрование на импортированном симметричном
        // ключе (AES).

        decrypt.init(Cipher.DECRYPT_MODE, unWrappedSecretKey, params_enc);

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
