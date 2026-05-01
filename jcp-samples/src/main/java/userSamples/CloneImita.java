/**
 * $RCSfile$
 * version $Revision$
 * created 27.09.2005 20:17:49 by elvira
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2009.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.JCP;

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
/**
 * текст
 */
private static final String SAMPLE_TEXT = "computing imita with cloning";

    /**
     * Использование различных алгоритмов и ключей для
     * расчета имитовставки.
     *
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
    JCPInit.initProviders(false);
    // ГОСТ Р 28147-89
    main_(JCP.GOST_CIPHER_NAME);

    // ГОСТ Р 34.12-2015 Магма
    main_(JCP.GOST_M_CIPHER_NAME);

    // ГОСТ Р 34.12-2015 Кузнечик
    main_(JCP.GOST_K_CIPHER_NAME);
}


    /**
     * Выполнение подсчета имиты.
     *
     * @param macAlg Алгоритм ключ и имитопреобразования.
     * @throws Exception
     */
    public static void main_(String macAlg) throws Exception {

        final byte[] data = SAMPLE_TEXT.getBytes();

        /* Генерирование симметричного ключа пользователя с параметрами
        шифрования из контрольной панели.*/
        final KeyGenerator keyGen = KeyGenerator.getInstance(macAlg);
        final SecretKey simm = keyGen.generateKey();

        /* Подсчет имиты на данный текст целиком */
        final Mac mac = Mac.getInstance(macAlg);
        mac.init(simm);
        mac.update(data);
        final byte[] imitaAll = mac.doFinal();

        /* Подсчет имиты на данный текст частями с импользование клонирования */
        final byte[] first = new byte[data.length / 2];
        final byte[] second = new byte[data.length - data.length / 2];
        System.arraycopy(data, 0, first, 0, first.length);
        System.arraycopy(data, first.length, second, 0, second.length);

        //подсчет первой части
        mac.reset();
        mac.update(first);

        //клонирование
        final Mac dupMac = (Mac) mac.clone();

        //подсчет второй части исходным объектом имитопреобразования
        mac.update(second);
        final byte[] imitaFirst = mac.doFinal();

        //подсчет второй части клонированным объектом имитопреобразования
        dupMac.update(second);
        final byte[] imitaSecond = dupMac.doFinal();

        // проверка результатов.
        if (imitaAll.length != imitaFirst.length ||
            imitaAll.length != imitaSecond.length)
            throw new Exception("Error in computing imita");

        for (int i = 0; i < imitaAll.length; i++)
            if (imitaAll[i] != imitaFirst[i] || imitaAll[i] != imitaSecond[i])
                throw new Exception("Error in computing imita");

        System.out.println("OK");
    }
}
