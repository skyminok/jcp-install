/**
 * KuznechikEncryptTransportExample.java,v $
 * version $
 * created 22.10.2020 17:40 by afevma
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
import ru.CryptoPro.JCSP.JCSP;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Пример экспорта/импорта секретного ключа Магма/Кузнечик на другом
 * секретном ключе Магма/Кузнечик.
 * Для шифрования на ключе Магма по умолчанию используется режим MGM_M_EXPORT.
 * Для шифрования на ключе Кузнечик по умолчанию используется режим MGM_K_EXPORT.
 * В примере также производится зашифрование/расшифрование данных.
 *
 * Провайдер: Java CSP.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class KuznechikExportExample {

    /**
     * Текст.
     */
    private static final String SAMPLE_TEXT = "Classic encryption/decryption";

    /**
     * Имя провайдера.
     */
    private static final String PROVIDER_NAME = JCSP.PROVIDER_NAME;

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Экспорт ключа Магма на ключе Магма
        main_(JCP.GOST_M_CIPHER_NAME, JCP.GOST_M_CIPHER_NAME, 8, 8);
        // Экспорт ключа Кузнечик на ключе Магма
        main_(JCP.GOST_K_CIPHER_NAME, JCP.GOST_M_CIPHER_NAME, 16, 8);
        // Экспорт ключа Магма на ключе Кузнечик
        main_(JCP.GOST_M_CIPHER_NAME, JCP.GOST_K_CIPHER_NAME, 8, 16);
        // Экспорт ключа Кузнечик на ключе Кузнечик
        main_(JCP.GOST_K_CIPHER_NAME, JCP.GOST_K_CIPHER_NAME,16, 16);
    }

    /**
     * Генерация двух секретных ключей.
     * Заширование данных на первом ключе.
     * Зашифрование первого ключа на втором ключе.
     * Расшифрование ключа и данных.
     *
     * @param encryptDataAlg Алгоритм ключа для шифрования данных.
     * @param wrapAlg Алгоритм ключа для шифрования ключа.
     * @param ivCipherSize длина IV для шифрованаия
     * @param ivWrapSize длина IV для экспорта
     * @throws Exception
     */
    public static void main_(String encryptDataAlg,  String wrapAlg,
                             int ivCipherSize, int ivWrapSize) throws Exception {

        final byte[] DATA = SAMPLE_TEXT.getBytes();

        final SecureRandom rnd = SecureRandom.getInstance(
                JCP.CP_RANDOM, PROVIDER_NAME);

        // Генерация начальной синхропосылки для шифрования данных.
        byte [] iv = new byte[ivCipherSize];
        rnd.nextBytes(iv);

        // Генерация начальной синхропосылки для экспорта ключа.
        byte [] iv_wrap = new byte[ivWrapSize];
        rnd.nextBytes(iv_wrap);

        IvParameterSpec params = new IvParameterSpec(iv);
        IvParameterSpec params_wrap = new IvParameterSpec(iv_wrap);

        // 1. Генерируем симметричный ключ для шифрования данных.
        KeyGenerator kg = KeyGenerator.getInstance(encryptDataAlg, PROVIDER_NAME);
        SecretKey cipherKey = kg.generateKey();

        // 2. Генерируем симметричный ключ для экспорта.
        KeyGenerator kgExp = KeyGenerator.getInstance(wrapAlg, PROVIDER_NAME);
        SecretKey wrapperKey = kgExp.generateKey();

        // 3. Шифруем данные.
        Cipher encrypt = Cipher.getInstance(encryptDataAlg, PROVIDER_NAME);
        encrypt.init(Cipher.ENCRYPT_MODE, cipherKey, params);
        byte[] encryptedMessage = encrypt.doFinal(DATA);

        // 4. Экспортируем ключ
        Cipher wrapper = Cipher.getInstance(wrapAlg, PROVIDER_NAME);
        wrapper.init(Cipher.WRAP_MODE, wrapperKey, params_wrap);
        byte[] wrapped = wrapper.wrap(cipherKey);

        // 4. Импортируем ключ
        wrapper.init(Cipher.UNWRAP_MODE, wrapperKey);
        final SecretKey key_ =
                (SecretKey) wrapper.unwrap(wrapped, null, Cipher.SECRET_KEY);

        // 4. Расшифруем данные и проверим.
        Cipher decrypt = Cipher.getInstance(encryptDataAlg, PROVIDER_NAME);
        decrypt.init(Cipher.DECRYPT_MODE, key_, params);
        byte[] decryptedMessage = decrypt.doFinal(encryptedMessage);

        if (DATA.length != decryptedMessage.length) {
            throw new Exception("Invalid length of encrypted or decrypted message.");
        } // if

        if (!Arrays.equals(DATA, decryptedMessage)) {
            throw new Exception("Invalid encrypted or decrypted message.");
        } // if

        System.out.println("ok");

    }

}
