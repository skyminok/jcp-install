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
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCSP.JCSP;

import userSamples.Constants;
import userSamples.KeyPairGen;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Высокоуровневый(!) пример шифрования с использованием секретного
 * ключа на алгоритме Кузнечик и его экспорта/импорта на ключе
 * согласования с открытом ключом получателя на алгоритме ГОСТ
 * 2012 (XXX) DH.
 *
 * Провайдер: Java CSP.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class KuznechikEncryptTransportExample {

    /**
     * Текст.
     */
    private static final String SAMPLE_TEXT = "Classic encryption/decryption";

    /**
     * Алгоритм ключа шифрования.
     */
    private static final String SECRET_KEY_ALGORITHM = JCP.GOST_K_CIPHER_NAME;

    /**
     * Алгоритм шифрования данных.
     */
    private static final String CIPHER_ALGORITHM = SECRET_KEY_ALGORITHM + "/CFB/NoPadding";

    /**
     * Алгоритм экспорта/импорта секретного ключа.
     */
    private static final String WRAP_ALGORITHM = JCSP.GOST_TRANSPORT_K;

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
        // Добавление провайдеров.
        JCPInit.initProviders(true);
        main_(Constants.EXCH_KEY_PAIR_ALG_2012_256);
        main_(Constants.EXCH_KEY_PAIR_ALG_2012_512);

    }

    /**
     * Шифрование на симметричном ключе.
     *
     * @param recipientAlg Алгоритм ключа получателя.
     * @throws Exception
     */
    public static void main_(String recipientAlg) throws Exception {

        final byte[] data = SAMPLE_TEXT.getBytes();

        // На стороне отправителя должен присутствовать:
        // - открытый ключ получателя (сертификат)
        // На стороне получателя должен присутствовать:
        // - закрытый ключ получателя

        // Генерирование ключей получателя.

        final KeyPair recipientPair = KeyPairGen.genKey(recipientAlg, PROVIDER_NAME); // ключи получателя

        // Генерирование самоподписанного сертификата получателя.

        final Certificate recipientCert = KeyPairGen.genSelfCert(
            recipientPair, "CN=RECIPIENT_CERTIFICATE, O=CryptoPro, C=RU",
                PROVIDER_NAME);

        // Генерирование симметричного ключа отправителем.

        final KeyGenerator keyGen = KeyGenerator.getInstance(SECRET_KEY_ALGORITHM, PROVIDER_NAME);
        final SecretKey secretKey = keyGen.generateKey();

        /* Зашифрование текста на секретном ключе отправителя */

        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        final byte[] iv = cipher.getIV(); // передача вектора инициализации получателю
        final byte[] encryptedText = cipher.doFinal(data, 0, data.length);

        // Зашифрование симметричного ключа на ключе согласования отправителя.

        cipher = Cipher.getInstance(WRAP_ALGORITHM, PROVIDER_NAME);
        cipher.init(Cipher.WRAP_MODE, recipientCert.getPublicKey());
        final byte[] wrappedSecretKey = cipher.wrap(secretKey);

        // Расшифрование на стороне получателя.

        // Расшифрование получателем симметричного ключа.

        cipher = Cipher.getInstance(WRAP_ALGORITHM, PROVIDER_NAME);
        cipher.init(Cipher.UNWRAP_MODE, recipientPair.getPrivate());

        final SecretKey unwrappedSecretKey = (SecretKey) cipher.unwrap(wrappedSecretKey, null, Cipher.SECRET_KEY);

        // Расшифрование получателем текста на расшифрованном
        // симметричном ключе. IV передан от отправителя.

        cipher = Cipher.getInstance(CIPHER_ALGORITHM, PROVIDER_NAME);
        AlgorithmParameterSpec parameterSpec = new IvParameterSpec(iv); // IV передан отправителем

        cipher.init(Cipher.DECRYPT_MODE, unwrappedSecretKey, parameterSpec, null);
        final byte[] decryptedText = cipher.doFinal(encryptedText, 0, encryptedText.length);

        // Проверка результата.

        if (decryptedText.length != data.length) {
            throw new Exception("Error in decrypting");
        } // if

        for (int i = 0; i < decryptedText.length; i++) {
            if (data[i] != decryptedText[i]) {
                throw new Exception("Error in decrypting");
            } // if
        } // for

        System.out.println("OK");

    }

}
