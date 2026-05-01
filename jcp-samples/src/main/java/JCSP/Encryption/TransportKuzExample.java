/**
 * TransportKuzExample.java,v $
 * version $
 * created 01.10.2021 16:22 by afevma
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

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCSP.JCSP;

import userSamples.KeyPairGen;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Пример шифрования и экспорта/импорта сессионного ключа с
 * алгоритмом Кузнечик на ключе экспорта/импорта.
 *
 * Провайдер: Java CSP.
 *
 * @author Copyright 2004-2021 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class TransportKuzExample {

    /**
     * Запуск примера.
     *
     * @param args Параметры.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Добавление провайдеров.
        JCPInit.initProviders(true);

        encryptDecrypt(JCSP.GOST_TRANSPORT_K, JCP.GOST_K_CIPHER_NAME, JCP.GOST_EPH_DH_2012_256_NAME);

    }

    /**
     * Пример зашифрования и расшифрования с использованием алгоритма
     * экспорта сессионного ключа {@link JCSP#GOST_TRANSPORT_K}.
     *
     * @param transportAlg Алгоритм экспорта сессионного ключа.
     * @param secretKeyAlg Алгоритм сессионного ключа.
     * @param keyAlg Алгоритм ключа экспорта.
     * @throws Exception
     */
    public static void encryptDecrypt(String transportAlg, String secretKeyAlg, String keyAlg) throws Exception {

        final byte[] DATA = "Security message.".getBytes(StandardCharsets.UTF_8);
        KeyPair recipientPair = KeyPairGen.genKey(keyAlg, JCSP.PROVIDER_NAME);

        byte[][] blob = encrypt(transportAlg, secretKeyAlg, secretKeyAlg + "/CFB/NoPadding", recipientPair.getPublic(), DATA);
        byte[] decryptedText = decrypt(transportAlg, secretKeyAlg + "/CFB/NoPadding", recipientPair.getPrivate(), blob[0], blob[1], blob[2]);

        if (decryptedText.length != DATA.length) {
            throw new Exception("Error in decrypting");
        } // if

        for (int i = 0; i < decryptedText.length; i++) {
            if (DATA[i] != decryptedText[i]) {
                throw new Exception("Error in decrypting");
            } // if
        } // for

        System.out.println("OK");

    }

    /**
     * Экспорт сессионного ключа на ключе экспорта.
     *
     * @param transportAlg Алгоритм экспорта сессионного ключа.
     * @param secretKeyAlg Алгоритм сессионного ключа.
     * @param cipherAlg Алгоритм шифрования данных.
     * @param recipientPublicKey Открытый ключ получателя.
     * @param data Шифруемые данные.
     * @return набор из экспортированного сессионного ключа,
     * IV и зашифрованных данных.
     * @throws Exception
     */
    public static byte[][] encrypt(String transportAlg, String secretKeyAlg, String cipherAlg,
        PublicKey recipientPublicKey, byte[] data) throws Exception {

        KeyGenerator keyGen = KeyGenerator.getInstance(secretKeyAlg, JCSP.PROVIDER_NAME);
        SecretKey secretKey = keyGen.generateKey();

        Cipher transport = Cipher.getInstance(transportAlg, JCSP.PROVIDER_NAME);
        transport.init(Cipher.WRAP_MODE, recipientPublicKey);

        byte[] encryptedKey = transport.wrap(secretKey); // GostR3410_GostR3412_KeyTransport

        Cipher cipher = Cipher.getInstance(cipherAlg, JCSP.PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        final byte[] iv = cipher.getIV();
        final byte[] encryptedText = cipher.doFinal(data, 0, data.length);

        return new byte[][] { encryptedKey, iv, encryptedText };

    }

    /**
     * Импорт зашифрованного сессионного ключа на ключе импорта.
     *
     * @param transportAlg Алгоритм импорта сессионного ключа.
     * @param cipherAlg Алгоритм шифрования данных.
     * @param recipientPrivateKey Закрытый ключ получателя.
     * @param wrappedSecretKey Зашифрованный сессионный ключ.
     * @param iv Вектор инициализации для шифроватора данных.
     * @param encryptedText Зашифрованные данные.
     * @return расшифрованные данные.
     * @throws Exception
     */
    public static byte[] decrypt(String transportAlg, String cipherAlg,
        PrivateKey recipientPrivateKey, byte[] wrappedSecretKey, byte[] iv,
        byte[] encryptedText) throws Exception {

        Cipher transport = Cipher.getInstance(transportAlg, JCSP.PROVIDER_NAME);
        transport.init(Cipher.UNWRAP_MODE, recipientPrivateKey);

        SecretKey unwrappedSecretKey = (SecretKey) transport.unwrap(wrappedSecretKey, null, Cipher.SECRET_KEY);

        Cipher cipher = Cipher.getInstance(cipherAlg, JCSP.PROVIDER_NAME);
        AlgorithmParameterSpec parameterSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, unwrappedSecretKey, parameterSpec, null);
        return cipher.doFinal(encryptedText, 0, encryptedText.length);

    }

}
