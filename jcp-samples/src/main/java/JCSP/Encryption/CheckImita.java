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

import ru.CryptoPro.Crypto.Cipher.GostCoreCipher;
import ru.CryptoPro.JCP.params.CryptParamsInterface;
import ru.CryptoPro.JCP.params.CryptParamsSpec;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.Key.GostPrivateKey;
import ru.CryptoPro.JCP.JCP;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.Destroyable;
import java.security.*;

/**
 * В данном примере осуществляется имитопреобразование в соответствии с
 * алгоритмом ГОСТ Р 28147-89 на ключах согласования сторон.
 */
public class CheckImita extends Common {

    protected String getAlicaAlias() {
        return "ci_alica";
    }

    protected char[] getAlicaPassword() {
        return "ci_alica".toCharArray();
    }

    protected String getBobAlias() {
        return "ci_bob";
    }

    protected char[] getBobPassword() {
        return "ci_bob".toCharArray();
    }

    /**
     * Выполнение расчета имитовставки.
     *
     * @param pairProvider Имя провайдера для генерации ключевых пар.
     * @param agreeProvider Провайдер для формирования ключей согласования.
     * @param simProvider Провайдер для генерации симметричного ключа.
     * @param simParams Параметры для сессионного ключа.
     * @param exchKeyAlgorithm Алгоритм шифрования или ключа обмена.
     * @param wrapAlgorithm Алгоритм зашифрования симметричного ключа.
     * @param signAlgorithm Алгоритм подписи запроса.
     * @param create true, если следует создать контейнеры и сохранить их;
     * иначе предполагается, что они существуют и их нужно загрузить.
     * @param delete true, если по завершении следует удалить контейнер.
     * @throws Exception
     */
    @Override
    public void execute(String pairProvider, String agreeProvider,
        String simProvider, CryptParamsInterface simParams, String
        exchKeyAlgorithm, String wrapAlgorithm, String signAlgorithm,
        boolean create, boolean delete) throws Exception {

        System.out.println("Example of computing imita by two users.");
        byte[] data = text.getBytes();

        System.out.println("Key pair provider: " + pairProvider +
            "\nagree key provider: " + agreeProvider +
            "\nsymmetric key provider: " + simProvider +
            "\nsymmetric parameters: " + simParams +
            "\nexchange key algorithm: " + exchKeyAlgorithm +
            "\nwrap key algorithm: " + wrapAlgorithm +
            "\nsignature algorithm: " + signAlgorithm +
            "\ncreate pair and container: " + create +
            "\ndelete container: " + delete);

        try {

            /* Чтение пользовательский ключей и сертификаторв открытых ключей */

            prepare(pairProvider, exchKeyAlgorithm, signAlgorithm, create, create);

            /* Генерация начальной синхропосылки для выработки
            ключа согласования */

            byte[] sv = new byte[randomLength];
            SecureRandom random = SecureRandom.getInstance(
                randomAlgorithmName, defaultProvider);
            random.nextBytes(sv);

            IvParameterSpec ivspec = new IvParameterSpec(sv);
            System.out.println("Syncro for KeyAgreement was generated");

            /* Выработка ключа согласования алисы со сгенеренным SV */

            KeyAgreement alicaKeyAgree =
                KeyAgreement.getInstance(exchKeyAlgorithm, agreeProvider);
            alicaKeyAgree.init(alicaPrivate, ivspec, null);
            alicaKeyAgree.doPhase(bobPublic, true);

            SecretKey alisaAgree = alicaKeyAgree.generateSecret(MacAlgorithm);
            System.out.println("Alica's key agreement was performed");

            /* Генерация симметричного ключа алисой. */

            KeyGenerator keyGen = KeyGenerator.getInstance(MacAlgorithm, simProvider);

            if (simParams != null) {
                keyGen.init(simParams);
            } // if

            SecretKey simKey = keyGen.generateKey();

            /* Зашифрование текста на симметричном ключе алисы */

            Cipher cipher = Cipher.getInstance(wrapAlgorithm, defaultProvider);

            /* Зашифрование симметричного ключа на ключе согласования алисы. */

            cipher.init(Cipher.WRAP_MODE, alisaAgree);
            byte[] wrappedKey = cipher.wrap(simKey);
            System.out.println("Alica's session key wrapping was performed");

            /* Подсчет имиты на симметричном ключе алисы */

            Mac mac = Mac.getInstance(MacAlgorithm, defaultProvider);
            mac.init(simKey);
            mac.update(data);

            byte[] alicaImita = mac.doFinal();
            System.out.println("Alica's imita was computed");

            /* Выработка ключа согласования боба с тем же SV. */

            KeyAgreement bobKeyAgree =
                KeyAgreement.getInstance(exchKeyAlgorithm, agreeProvider);
            bobKeyAgree.init(bobPrivate, ivspec, null);
            bobKeyAgree.doPhase(alicaPublic, true);

            SecretKey bobAgree = bobKeyAgree.generateSecret(MacAlgorithm);
            System.out.println("Bob's key agreement was performed");

            /* Расшифрование бобом симметричного ключа. */
            cipher = Cipher.getInstance(wrapAlgorithm, defaultProvider);
            cipher.init(Cipher.UNWRAP_MODE, bobAgree);
            simKey = (SecretKey) cipher.unwrap(wrappedKey, null, Cipher.SECRET_KEY);
            System.out.println("Bob's session key unwrapping was performed");

            /* Подсчет имиты  бобом на симметричном ключе */

            mac = Mac.getInstance(MacAlgorithm, defaultProvider);
            mac.init(simKey);
            mac.update(data);

            byte[] bobImita = mac.doFinal();
            System.out.println("Bob's imita was computed");

            // проверка результатов.
            if (alicaImita.length != bobImita.length) {
                throw new Exception("Error in computing imita");
            } // if

            for (int i = 0; i < alicaImita.length; i++) {
                if (alicaImita[i] != bobImita[i]) {
                    throw new Exception("Error in computing imita");
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
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        main_wrap_default(true);
        main_wrap_pro_export(true);
        main_wrap_pro12_export(true);
    }

    /**
     * Использование SIMPLE_EXPORT.
     *
     * @param exchange True, если используются ключи обмена.
     * @throws Exception
     */
    public static void main_wrap_default(boolean exchange) throws Exception {
        main_different_wrap("GOST28147/" + GostCoreCipher.STR_SIMPLE_EXPORT + "/NoPadding", exchange);
    }

    /**
     * Использование PRO_EXPORT.
     *
     * @param exchange True, если используются ключи обмена.
     * @throws Exception
     */
    public static void main_wrap_pro_export(boolean exchange) throws Exception {
        main_different_wrap("GOST28147/" + GostCoreCipher.STR_PRO_EXPORT + "/NoPadding", exchange);
    }

    /**
     * Использование PRO12_EXPORT.
     *
     * @param exchange True, если используются ключи обмена.
     * @throws Exception
     */
    public static void main_wrap_pro12_export(boolean exchange) throws Exception {
        main_different_wrap("GOST28147/" + GostCoreCipher.STR_PRO12_EXPORT + "/NoPadding", exchange);
    }

    /**
     * Использование различных алгоритмов экспорта/импорта
     * сессионного ключа.
     *
     * @param wrapAlgorithm Алгоритм экспорта/импорта ключа.
     * @param exchange True, если используются ключи обмена.
     * @throws Exception
     */
    public static void main_different_wrap(String wrapAlgorithm,
        boolean exchange) throws Exception {

        CheckImita checkImita = new CheckImita();

        // ГОСТ Р 34.10-2001
        // checkImita.execute(JCSP.PROVIDER_NAME, JCSP.PROVIDER_NAME,
        //     JCSP.PROVIDER_NAME, null,
        //         exchange ? JCP.GOST_EL_DH_NAME : JCP.GOST_EL_DEGREE_NAME,
        //             wrapAlgorithm, JCP.GOST_EL_SIGN_NAME, true, true);

            // ГОСТ Р 34.10-2012 (256)
            checkImita.execute(JCSP.PROVIDER_NAME, JCSP.PROVIDER_NAME,
                    JCSP.PROVIDER_NAME, CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z),
                    exchange ? JCP.GOST_DH_2012_256_NAME : JCP.GOST_EL_2012_256_NAME,
                    wrapAlgorithm, JCP.GOST_SIGN_2012_256_NAME, true, true);

            // ГОСТ Р 34.10-2012 (512)
            checkImita.execute(JCSP.PROVIDER_NAME, JCSP.PROVIDER_NAME,
                    JCSP.PROVIDER_NAME, CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z),
                    exchange ? JCP.GOST_DH_2012_512_NAME : JCP.GOST_EL_2012_512_NAME,
                    wrapAlgorithm, JCP.GOST_SIGN_2012_512_NAME, true, true);

    }
}
