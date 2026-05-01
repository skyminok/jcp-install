/**
 * SimpleWrapKuzExample.java,v $
 * version $
 * created 16.10.2020 16:39 by afevma
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
import ru.CryptoPro.JCP.tools.Encoder;

import ru.CryptoPro.JCSP.JCSP;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Пример шифрования на симметричном ключе на алгоритме
 * ГОСТ Р 34.12-2015 Кузнечик и его экспорта/импорта
 * с помощью Java CSP на ключе обмена на алгоритме
 * ГОСТ 2012 (256) DH.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class SimpleWrapKuzExample {

    public static final String message = "Message for encryption and decryption"; // данные для зашифрования
    public static final String cipherAlg = JCP.GOST_K_CIPHER_NAME; // алгоритм секретного ключа
    public static final String encryptionAlg = "GOST3412_2015_K/ECB/PKCS5_PADDING"; // алгоритм шифрования данных
    public static final String wrapAlg = JCSP.GOST_TRANSPORT_K; // алгоритм экспорта/импорта секретного ключа
    public static final String defaultProvider = JCSP.PROVIDER_NAME;
    public static final String defaultStoreType = JCSP.HD_STORE_NAME;
    public static final String alias = "key2012_256"; // алиас ключевого контейнера с алгоритмом ГОСТ 2012 (256)
    public static final char[] password = "123456".toCharArray(); // пароль к контейнеру

    /**
     * Запуск примера.
     * Ключевой контейнер {@link #alias} с паролем {@link #password}
     * должен существовать в хранилище {@link #defaultStoreType}.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Добавление провайдеров. Java CSP по умолчанию.

        JCPInit.initProviders(true);

        // Генерируем симметричный ключ.

        KeyGenerator kg = KeyGenerator.getInstance(cipherAlg, defaultProvider);
        SecretKey secretKey = kg.generateKey();

        // Читаем ключ и сертификат получателя из ключевого контейнера.

        KeyStore keyStore = KeyStore.getInstance(defaultStoreType, defaultProvider);
        keyStore.load(null, null);

        JCPProtectionParameter parameter = new JCPProtectionParameter(password);
        JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) keyStore.getEntry(alias, parameter);

        X509Certificate recipientCert  = (X509Certificate) entry.getCertificate();
        PrivateKey recipientPrivateKey = entry.getPrivateKey();

        // Экспорт секретного ключа в адрес получателя на
        // его открытом ключе.

        Cipher wrapCipher = Cipher.getInstance(wrapAlg, defaultProvider);
        wrapCipher.init(Cipher.WRAP_MODE, recipientCert);

        final byte[] wrappedSecretKey = wrapCipher.wrap(secretKey);
        Encoder encoder = new Encoder();

        System.out.println("Wrapped secret key: " + encoder.encode(wrappedSecretKey));

        // Импорт блоба секретного ключа на закрытом ключе
        // получателя.

        Cipher unwrapCipher = Cipher.getInstance(wrapAlg, defaultProvider);
        unwrapCipher.init(Cipher.UNWRAP_MODE, recipientPrivateKey);

        SecretKey unwrappedKey = (SecretKey) unwrapCipher.unwrap(wrappedSecretKey, null, Cipher.SECRET_KEY);

        // Пробное зашифрование на исходном и полученном секретных
        // ключах для проверки.

        check(secretKey, unwrappedKey);
        System.out.println("Completed.");

    }

    /**
     * Проверка шифрованием на ключах - исходном и
     * импортированном.
     *
     * @param srcSecretKey Исходный секретный ключ.
     * @param unWrappedSecretKey Импортированный секретный
     * ключ.
     * @throws Exception
     */
    private static void check(SecretKey srcSecretKey, SecretKey
        unWrappedSecretKey) throws Exception {

        final SecureRandom rnd = SecureRandom.getInstance(JCP.CP_RANDOM, defaultProvider);
        byte [] iv = new byte[16];

        rnd.nextBytes(iv);
        IvParameterSpec params = new IvParameterSpec(iv);

        // Шифруем данные на одном секретном ключе.

        Cipher encrypt = Cipher.getInstance(encryptionAlg, defaultProvider);
        encrypt.init(Cipher.ENCRYPT_MODE, srcSecretKey, params);

        byte[] encryptedMessage = encrypt.doFinal(message.getBytes());
        System.out.println("Source Message: " + message);

        Encoder encoder = new Encoder();
        System.out.println("Encrypted Message: " + encoder.encode(encryptedMessage));

        // Расшифруем данные и проверим на другом секретном ключе.

        Cipher decrypt = Cipher.getInstance(encryptionAlg, defaultProvider);
        decrypt.init(Cipher.DECRYPT_MODE, unWrappedSecretKey, params);

        byte[] decryptedMessage = decrypt.doFinal(encryptedMessage);
        System.out.println("Decrypted Message: " + new String(decryptedMessage));

        // Проверка.

        if (message.length() != decryptedMessage.length) {
            throw new Exception("Invalid length of encrypted or decrypted message.");
        } // if

        if (!Arrays.equals(message.getBytes(), decryptedMessage)) {
            throw new Exception("Invalid encrypted or decrypted message.");
        } // if

    }

}
