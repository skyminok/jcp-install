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

import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCSP.JCSP;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

/**
 * В данном примере осуществляется имитопреобразование в соответствии с
 * алгоритмами ГОСТ Р 28147-89, ГОСТ Р 34.12-2015 Магма и ГОСТ Р 34.12-2015 Кузнечик
 * на симметричном ключе шифрования различными способами:
 * подсчет имиты на все данные, имитопреобразование частями и
 * имитопреобразование с использованием клонирования.
 */
public class CloneImita {

    public static final String text =
            "Example of computing imita with cloning.";

    /**
     * Выполнение подсчета имиты.
     *
     * @param providerName Провайдер для генерации симметричного ключа и выработки имиты.
     * @param algorithm Алгоритм ключ и имитопреобразования.
     * @throws Exception
     */
    public static void execute(String providerName, String algorithm) throws Exception {

        System.out.println("Example of computing imita with cloning.");

        byte[] data = CloneImita.text.getBytes();

        /* Генерация симметричного ключа пользователя с параметрами
        шифрования из контрольной панели. */

        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm, providerName);
        SecretKey simm = keyGen.generateKey();
        System.out.println("Session key was generated");

        /* Подсчет имиты на данный текст целиком */

        Mac mac = Mac.getInstance(algorithm, providerName);
        mac.init(simm);
        mac.update(data);

        byte[] imitaAll = mac.doFinal();
        System.out.println("Imita of all data was computed");

        /* Подсчет имиты на данный текст частями с использование клонирования */

        byte[] first = new byte[data.length / 2];
        byte[] second = new byte[data.length - data.length / 2];

        System.arraycopy(data, 0, first, 0, first.length);
        System.arraycopy(data, first.length, second, 0, second.length);

        //подсчет первой части
        mac.reset();
        mac.update(first);
        System.out.println("Imita of first part was computed");

        //клонирование
        Mac dupMac = (Mac) mac.clone();
        System.out.println("Imita object was cloned");

        //подсчет второй части исходным объектом имитопреобразования
        mac.update(second);
        byte[] imitaFirst = mac.doFinal();
        System.out.println("Imita of second part was computed " +
                "with current imita object");

        //подсчет второй части клонированным объектом имитопреобразования
        dupMac.update(second);
        byte[] imitaSecond = dupMac.doFinal();
        System.out.println("Imita of second part was computed " +
                "with cloned imita object");

        // проверка результатов.
        if ((imitaAll.length != imitaFirst.length) ||
                (imitaAll.length != imitaSecond.length)) {
            throw new Exception("Error in computing imita");
        } // if

        for (int i = 0; i < imitaAll.length; i++) {
            if ((imitaAll[i] != imitaFirst[i])
                    || (imitaAll[i] != imitaSecond[i])) {
                throw new Exception("Error in computing imita");
            }
        } // for

        System.out.println("Example is passed. OK.");
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(true);
        main_different_imita();
    }

    /**
     * Использование различных алгоритмов и ключей для
     * расчета имитовставки.
     *
     * @throws Exception
     */
    public static void main_different_imita()
        throws Exception {

        // ГОСТ Р 28147-89
        execute(JCSP.PROVIDER_NAME, JCP.GOST_CIPHER_NAME);

        // ГОСТ Р 34.12-2015 Магма
        execute(JCSP.PROVIDER_NAME, JCP.GOST_M_CIPHER_NAME);

        // ГОСТ Р 34.12-2015 Кузнечик
        execute(JCSP.PROVIDER_NAME, JCP.GOST_K_CIPHER_NAME);

    }

}
