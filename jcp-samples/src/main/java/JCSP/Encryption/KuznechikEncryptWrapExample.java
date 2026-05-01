/**
 * KuznechikEncryptWrapExample.java,v $
 * version $
 * created 22.10.2020 16:22 by afevma
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
import ru.CryptoPro.JCP.params.Kexp15ParamsSpec;
import ru.CryptoPro.JCP.tools.Array;

import ru.CryptoPro.JCSP.JCSP;

import userSamples.Constants;
import userSamples.KeyPairGen;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Низкоуровневый(!) пример шифрования с использованием секретного
 * ключа на алгоритме Кузнечик и его экспорта/импорта на ключе
 * согласования с открытом ключом получателя на алгоритме ГОСТ
 * 2012 (XXX) DH.
 *
 * Провайдер: Java CSP.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 * @see KuznechikEncryptTransportExample
 */
public class KuznechikEncryptWrapExample {

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
    private static final String WRAP_ALGORITHM = SECRET_KEY_ALGORITHM + "/KEXP_2015_K_EXPORT/NoPadding";

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

        main_( // ГОСТ 2012 (256)
            Constants.EXCH_KEY_PAIR_ALG_2012_256,
            JCP.GOST_EPH_DH_2012_256_NAME,
            SECRET_KEY_ALGORITHM,
            JCP.GR3412_2015_K_BLOCKLEN,
            CIPHER_ALGORITHM,
            WRAP_ALGORITHM
        );

        main_( // ГОСТ 2012 (512)
            Constants.EXCH_KEY_PAIR_ALG_2012_512,
            JCP.GOST_EPH_DH_2012_512_NAME,
            SECRET_KEY_ALGORITHM,
            JCP.GR3412_2015_K_BLOCKLEN,
            CIPHER_ALGORITHM,
            WRAP_ALGORITHM
        );

    }

    /**
     * Шифрование на симметричном ключе.
     *
     * @param recipientAlg Алгоритм ключа получателя.
     * @throws Exception
     */
    public static void main_(String recipientAlg, String senderAlg, String secretKeyAlg,
        int kekBlockSize, String cipherAlg, String wrapAlg) throws Exception {

        final byte[] data = SAMPLE_TEXT.getBytes();

        // На стороне отправителя должен присутствовать:
        // - открытый ключ получателя (сертификат)
        // На стороне получателя должен присутствовать:
        // - закрытый ключ получателя
        //

        // Генерирование ключей сторон.

        final KeyPair senderEphPair = KeyPairGen.genKey(senderAlg, PROVIDER_NAME); // эфемерный ключ отправителя
        final KeyPair recipientPair = KeyPairGen.genKey(recipientAlg, PROVIDER_NAME); // ключи получателя

        // Генерирование самоподписанного сертификата получателя.

        final Certificate recipientCert = KeyPairGen.genSelfCert(
            recipientPair, "CN=RECIPIENT_CERTIFICATE, O=CryptoPro, C=RU",
                PROVIDER_NAME);

        // Генерирование начальной синхропосылки для
        // выработки ключа согласования и ключа шифрования.

        final SecureRandom random = SecureRandom.getInstance(Constants.RANDOM_ALG, PROVIDER_NAME);
        final byte[] UKM = new byte[JCP.CMS_GR3412_UKM_LEN];

        random.nextBytes(UKM);
        byte[] bUKM = new byte[JCP.CMS_GR3412_KEG_UKM_LEN];

        // Ставим UKM для VKO далее в рамках KEG — первые 16 байт ukm.
        // Согласно документу на TLS-2015.

        for (int i = 0; i < JCP.CMS_GR3412_KEG_UKM_LEN; ++i) {
            bUKM[i] = UKM[JCP.CMS_GR3412_KEG_UKM_LEN - i - 1];
        } // for

        IvParameterSpec agreeSpec = new IvParameterSpec(bUKM); // для ключа согласования
        byte[] expUkm = new byte[kekBlockSize / 2];

        Array.copy(UKM, JCP.CMS_GR3412_KEXP15_IV_OFFSET, expUkm, 0, expUkm.length);
        byte[] extendedUkm = null;

        if (recipientAlg.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME) ||
            recipientAlg.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME)) {

            extendedUkm = new byte[JCP.CMS_GR3412_KEG_SEED_LEN];
            Array.copy(UKM, JCP.CMS_GR3412_KEG_UKM_LEN, extendedUkm, 0, JCP.CMS_GR3412_KEG_SEED_LEN);

        } // if

        Kexp15ParamsSpec kExpSpec = new Kexp15ParamsSpec(expUkm, extendedUkm); // для ключа шифрования (Кузнечик)

        // Выработка ключа согласования отправителя.

        final KeyAgreement senderKeyAgree = KeyAgreement.getInstance(recipientAlg, PROVIDER_NAME);
        senderKeyAgree.init(senderEphPair.getPrivate(), agreeSpec);

        senderKeyAgree.doPhase(recipientCert.getPublicKey(), true);
        final SecretKey alisaAgree = senderKeyAgree.generateSecret(secretKeyAlg);

        // Генерирование симметричного ключа отправителем.

        final KeyGenerator keyGen = KeyGenerator.getInstance(secretKeyAlg, PROVIDER_NAME);
        final SecretKey secretKey = keyGen.generateKey();

        // Зашифрование текста на секретном ключе отправителя.

        Cipher cipher = Cipher.getInstance(cipherAlg, PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        final byte[] iv = cipher.getIV(); // передача вектора инициализации получателю
        final byte[] encryptedText = cipher.doFinal(data, 0, data.length);

        // Зашифрование симметричного ключа на ключе согласования отправителя.

        cipher = Cipher.getInstance(wrapAlg, PROVIDER_NAME);
        cipher.init(Cipher.WRAP_MODE, alisaAgree, kExpSpec);

        final byte[] wrappedSecretKey = cipher.wrap(secretKey); // GostKeyTransportKExp15

        // Расшифрование на стороне получателя.

        // Выработка ключа согласования получателем.

        final KeyAgreement recipientKeyAgree = KeyAgreement.getInstance(recipientAlg, PROVIDER_NAME);
        recipientKeyAgree.init(recipientPair.getPrivate(), agreeSpec); // agreeSpec передан отправителем

        recipientKeyAgree.doPhase(senderEphPair.getPublic(), true);
        final SecretKey recipientAgree = recipientKeyAgree.generateSecret(SECRET_KEY_ALGORITHM);

        // Расшифрование получателем симметричного ключа.

        cipher = Cipher.getInstance(wrapAlg, PROVIDER_NAME);
        cipher.init(Cipher.UNWRAP_MODE, recipientAgree, kExpSpec); // kExpSpec передан отправителем

        final SecretKey unwrappedSecretKey = (SecretKey) cipher.unwrap(wrappedSecretKey, null, Cipher.SECRET_KEY);

        // Расшифрование получателем текста на расшифрованном
        // симметричном ключе. IV передан от отправителя.

        cipher = Cipher.getInstance(cipherAlg, PROVIDER_NAME);
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
