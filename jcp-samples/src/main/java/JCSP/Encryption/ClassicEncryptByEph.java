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

import javax.security.auth.Destroyable;
import ru.CryptoPro.JCP.params.CryptParamsInterface;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCSP.Key.GostPrivateKey;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * В данном примере осуществляется зашифрование и расшифрование данных по
 * классической схеме (на симметричных ключах согласования) на эфемерных ключах.
 */
public class ClassicEncryptByEph extends Common {

    protected String getAlicaAlias() {
        return "cebe_alica";
    }

    protected char[] getAlicaPassword() {
        return "cebe_alica".toCharArray();
    }

    protected String getBobAlias() {
        return "cebe_bob";
    }

    protected char[] getBobPassword() {
        return "cebe_bob".toCharArray();
    }

    /**
     * Выполнение шифрования.
     *
     * @param pairProvider Имя провайдера для генерации ключевых пар.
     * @param agreeProvider Провайдер для формирования ключей согласования.
     * @param simProvider Провайдер для генерации симметричного ключа.
     * @param simParams Параметры для сессионного ключа.
     * @param exchKeyAlgorithm Алгоритм шифрования или ключа обмена.
     * @param exchKeyAgreeAlgorithm Алгоритм для ключей согласования.
     * @param signAlgorithm Алгоритм подписи запроса.
     * @param create true, если следует создать контейнеры и сохранить их;
     * иначе предполагается, что они существуют и их нужно загрузить.
     * @param delete true, если по завершении следует удалить контейнер.
     * @throws Exception
     */
    @Override
    public void execute(String pairProvider, String agreeProvider,
                        String simProvider, CryptParamsInterface simParams, String exchKeyAlgorithm,
                        String exchKeyAgreeAlgorithm, String signAlgorithm, boolean create,
                        boolean delete) throws Exception {

        System.out.println("Classic ephemeral encryption and decryption example.");
        byte[] data = ClassicEncrypt.text.getBytes();

        try {

            /* Генерация пользовательский ключей */

            prepare(pairProvider, exchKeyAlgorithm, signAlgorithm, create, create);

            /* Экспорт открытых ключей */

            // алиса
            byte[] alicaPublicKey = alicaPublic.getEncoded();
            System.out.println("Alica's public key was exported");

            // боб
            byte[] bobPublicKey = bobPublic.getEncoded();
            System.out.println("Bob's public key was exported");

            /* Генерация начальной синхропосылки для выработки
            ключа согласования */

            byte[] sv = new byte[randomLength];
            SecureRandom random = SecureRandom.getInstance(
                    randomAlgorithmName, defaultProvider);
            random.nextBytes(sv);

            IvParameterSpec ivspec = new IvParameterSpec(sv);
            System.out.println("Syncro for KeyAgreement was generated");

            /* Получение открытых ключей сторонами */

            // алиса
            KeyFactory alicakf = KeyFactory.getInstance(
                    alicaPublic.getAlgorithm(), defaultProvider);
            X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(bobPublicKey);
            PublicKey genBobPublicKey = alicakf.generatePublic(bobPubKeySpec);
            System.out.println("Alica has received bob's public key");

            // боб
            KeyFactory bobkf = KeyFactory.getInstance(
                    bobPublic.getAlgorithm(), defaultProvider);
            X509EncodedKeySpec alicaPubKeySpec = new X509EncodedKeySpec(alicaPublicKey);
            PublicKey genAlicaPublicKey = bobkf.generatePublic(alicaPubKeySpec);
            System.out.println("Bob has received alica's public key");

            /* Выработка ключа согласования алисы со сгенеренным SV */

            KeyAgreement alicaKeyAgree =
                    KeyAgreement.getInstance(exchKeyAgreeAlgorithm, agreeProvider);
            alicaKeyAgree.init(alicaPrivate, ivspec, null);
            alicaKeyAgree.doPhase(genBobPublicKey, true);

            SecretKey alisaAgree = alicaKeyAgree.generateSecret(MacAlgorithm);
            System.out.println("Alica's key agreement was performed");

            /* Зашифрование текста на ключе согласования алисы */

            Cipher cipher = Cipher.getInstance(CipherAlgorithm, defaultProvider);
            cipher.init(Cipher.ENCRYPT_MODE, alisaAgree, (AlgorithmParameterSpec) null,
                    (SecureRandom) null);

            // передача вектора инициализации бобу
            byte[] iv = cipher.getIV();
            byte[] encryptedText = cipher.doFinal(data, 0, data.length);
            System.out.println("Alica's encrypting was performed");

            /* Выработка ключа согласования боба с тем же SV. */

            KeyAgreement bobKeyAgree =
                    KeyAgreement.getInstance(exchKeyAgreeAlgorithm, agreeProvider);
            bobKeyAgree.init(bobPrivate, ivspec, null);
            bobKeyAgree.doPhase(genAlicaPublicKey, true);

            SecretKey bobAgree = bobKeyAgree.generateSecret(MacAlgorithm);
            System.out.println("Bob's key agreement was performed");

            /*Расшифрование текста на ключе согласования боба. IV передан от алисы*/
            cipher = Cipher.getInstance(CipherAlgorithm, defaultProvider);
            cipher.init(Cipher.DECRYPT_MODE, bobAgree, new IvParameterSpec(iv), null);

            byte[] decryptedText = cipher.doFinal(encryptedText, 0,
                    encryptedText.length);
            System.out.println("Bob's decrypting was performed");

            // проверка результата.
            if (decryptedText.length != data.length) {
                throw new Exception("Error in crypting");
            } // if

            for (int i = 0; i < decryptedText.length; i++) {
                if (data[i] != decryptedText[i]) {
                    throw new Exception("Error in crypting");
                }
            } // for

            System.out.println("Example is passed. OK.");

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {

            if (alicaPrivate != null) {
                if (alicaPrivate instanceof Destroyable) {
                    ((Destroyable) alicaPrivate).destroy();
                }
            }

            if (bobPrivate != null) {
                if (bobPrivate instanceof Destroyable) {
                    ((Destroyable) bobPrivate).destroy();
                }
            }

            // Удаление контейнеров.
            clear(delete);

        }

    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        main_different_encrypt();
    }

    /**
     * Использование различных алгоритмов и ключей для
     * зашифрования.
     *
     * @throws Exception
     */
    public static void main_different_encrypt()
            throws Exception {

        ClassicEncryptByEph classicEncryptByEph = new ClassicEncryptByEph();

        // ГОСТ Р 34.10-2001 DH EPH
        //classicEncryptByEph.execute(JCSP.PROVIDER_NAME, JCSP.PROVIDER_NAME,
        //        JCSP.PROVIDER_NAME, null, JCP.GOST_EL_DH_EPH_NAME, JCP.GOST_EL_DH_NAME,
        //        JCP.GOST_EL_SIGN_NAME, false, false);

        // ГОСТ Р 34.10-2012 (256) DH EPH
        classicEncryptByEph.execute(JCSP.PROVIDER_NAME, JCSP.PROVIDER_NAME,
                JCSP.PROVIDER_NAME, null, JCP.GOST_EPH_DH_2012_256_NAME,
                JCP.GOST_DH_2012_256_NAME, JCP.GOST_SIGN_2012_256_NAME,
                false, false);

        // ГОСТ Р 34.10-2012 (512) DH EPH
        classicEncryptByEph.execute(JCSP.PROVIDER_NAME, JCSP.PROVIDER_NAME,
                JCSP.PROVIDER_NAME, null, JCP.GOST_EPH_DH_2012_512_NAME,
                JCP.GOST_DH_2012_512_NAME, JCP.GOST_SIGN_2012_512_NAME,
                false, false);

    }

}
