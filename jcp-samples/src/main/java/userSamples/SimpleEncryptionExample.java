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
package userSamples;

import ru.CryptoPro.Crypto.CryptoProvider;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.CryptParamsSpec;
import ru.CryptoPro.JCP.tools.Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Пример шифрования на симметричном ключе на алгоритме 28147
 * с помощью JCP.
 *
 * В примере используются разные режимы шифрования.
 */
public class SimpleEncryptionExample {

    // Данные для зашифрования
    public static final String message = "Message for encryption and decryption";

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(false);
        String cipherAlg = JCP.GOST_CIPHER_NAME;
        String defaultProvider = JCP.PROVIDER_NAME;
        String defaultCipherProvider = CryptoProvider.PROVIDER_NAME;
        execute(cipherAlg, cipherAlg, defaultProvider, defaultCipherProvider, CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z));
        execute(cipherAlg, "GOST28147/ECB/PKCS5_PADDING", defaultProvider, defaultCipherProvider, null);
        execute(cipherAlg, "GOST28147/CBC/PKCS5_PADDING", defaultProvider, defaultCipherProvider, CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z));
        execute(cipherAlg, "GOST28147/CFB/PKCS5_PADDING", defaultProvider, defaultCipherProvider, null);
        execute(cipherAlg, "GOST28147/OFB/PKCS5_PADDING", defaultProvider, defaultCipherProvider, CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z));
    }

    /**
     * Выполнение шифрования.
     *
     * @param encryptMode Алгоритм шифрования.
     * @param providerName имя провайдера
     * @param cipherProviderName имя провайдера шифрования
     * @param cryptParams Параметры шифрования.
     * @throws Exception
     */
    public static void execute(String cipherAlg, String encryptMode, String providerName,
        String cipherProviderName, CryptParamsSpec cryptParams) throws Exception {

        System.out.println("Encrypt Mode: " + encryptMode);

        // Генерируем вектор инициализации.

        final SecureRandom rnd = SecureRandom.getInstance("CPRandom", providerName);
        byte [] iv;

        if (cipherAlg.equalsIgnoreCase(JCP.GOST_K_CIPHER_NAME)) { // для алгоритма Кузнечик - 16 байт
            iv = new byte[16];
        } // if
        else {
            iv = new byte[8];
        } // else

        rnd.nextBytes(iv);
        IvParameterSpec params = new IvParameterSpec(iv);

        // Генерируем симметричный ключ.

        KeyGenerator kg = KeyGenerator.getInstance(cipherAlg, cipherProviderName);

        // Параметры можно задавать только для алгоритма ГОСТ 28147-89.
        // Если параметры заданы - установим их.

        if (cipherAlg.equalsIgnoreCase(JCP.GOST_CIPHER_NAME) && (cryptParams != null)) {
            kg.init(cryptParams);
        } // if

        SecretKey symmetricKey = kg.generateKey();

        // Зашифровываем данные.

        Cipher encrypt = Cipher.getInstance(encryptMode, cipherProviderName);
        encrypt.init(Cipher.ENCRYPT_MODE, symmetricKey, params);

        byte[] encryptedMessage = encrypt.doFinal(message.getBytes());
        System.out.println("Source Message: " + message);

        Encoder encoder = new Encoder();
        System.out.println("Encrypted Message: " + encoder.encode(encryptedMessage));

        // Расшифровываем данные и проверяем.

        Cipher decrypt = Cipher.getInstance(encryptMode, cipherProviderName);
        decrypt.init(Cipher.DECRYPT_MODE, symmetricKey, params);

        byte[] decryptedMessage = decrypt.doFinal(encryptedMessage);
        System.out.println("Decrypted Message: " + new String(decryptedMessage));

        if (message.length() != decryptedMessage.length) {
            throw new Exception("Invalid length of encrypted or decrypted message.");
        } // if

        if (!Arrays.equals(message.getBytes(), decryptedMessage)) {
            throw new Exception("Invalid encrypted or decrypted message.");
        } // if

    }

}
