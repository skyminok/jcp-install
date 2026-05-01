/**
 * Copyright 2004-2012 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.Encryption;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.OmacParamsSpec;

import ru.CryptoPro.JCSP.JCSP;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;

/**
 * Пример шифрования для режима OMAC_CTR на симметричных
 * ключах ГОСТ Р 34.12-2015 Кузнечик и Магма с помощью
 * Java CSP.
 */
public class SimpleOmacExample {

    public static final String message = "Message for encryption and decryption"; // данные для зашифрования
    public static final String defaultProvider = JCSP.PROVIDER_NAME;

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Добавление провайдеров. Java CSP по умолчанию.
        JCPInit.initProviders(true);

        execute(JCP.GOST_M_CIPHER_NAME, "GOST3412_2015_M/OMAC_CTR/NoPadding", 12);
        execute(JCP.GOST_K_CIPHER_NAME, "GOST3412_2015_K/OMAC_CTR/NoPadding", 16);

    }

    /**
     * Функция шифрования.
     *
     * @param keyGenMode Алгоритм ключа.
     * @param encryptMode Алгоритм шифрования.
     * @param ivSize Длина IV в байтах.
     * @throws Exception
     */
    public static void execute(String keyGenMode, String encryptMode,
        int ivSize) throws Exception {

        System.out.println("Encrypt Mode: " + ((encryptMode != null) ? encryptMode : "default"));
        final SecureRandom rnd = SecureRandom.getInstance(JCP.CP_RANDOM, defaultProvider);

        byte [] iv = new byte[ivSize];

        rnd.nextBytes(iv);
        IvParameterSpec params = new IvParameterSpec(iv);

        // Генерируем симметричный ключ.

        KeyGenerator kg = KeyGenerator.getInstance(keyGenMode, defaultProvider);
        SecretKey symmetricKey = kg.generateKey();

        // Шифруем данные.

        Cipher encrypt = Cipher.getInstance(encryptMode, defaultProvider);
        encrypt.init(Cipher.ENCRYPT_MODE, symmetricKey, params);
        byte[] encryptedMessage = encrypt.doFinal(message.getBytes());

        // Получаем зашифрованную имиту. Ее можно
        // сохранить и использовать при расшифровании.

        byte[] omac = null;
        AlgorithmParameters omacParams = encrypt.getParameters();
        OmacParamsSpec spec;

        if (omacParams != null && omacParams.getAlgorithm().equalsIgnoreCase(JCP.GOST_OMAC_NAME)) {

            try {
                spec = omacParams.getParameterSpec(OmacParamsSpec.class);
                omac = spec.getOmacValue();
            } catch (InvalidParameterSpecException e) {
                throw new IOException(e);
            }

        } // if

        // Расшифруем данные и проверим. Используем имиту.

        spec = new OmacParamsSpec(omac, iv);
        Cipher decrypt = Cipher.getInstance(encryptMode, defaultProvider);

        decrypt.init(Cipher.DECRYPT_MODE, symmetricKey, spec);
        byte[] decryptedMessage = decrypt.doFinal(encryptedMessage);

        System.out.println("Decrypted Message: " + new String(decryptedMessage));

        // Проверка.

        if (message.length() != decryptedMessage.length) {
            throw new Exception("Invalid length of encrypted or" +
                " decrypted message.");
        } // if

        if (!Arrays.equals(message.getBytes(), decryptedMessage)) {
            throw new Exception("Invalid encrypted or decrypted message.");
        } // if

        System.out.println("Completed.");

    }

}
