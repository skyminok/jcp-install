/**
 * $RCSfile$
 * version $Revision$
 * created 14.04.2005 17:49:03 by elvira
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2005.
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

import ru.CryptoPro.JCP.Digest.AbstractGostDigest;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.ByteArrayInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

/**
 * В данном примере осуществляется создание хеша данных в соответствии с
 * алгоритмом ГОСТ Р 34.11-94.
 */
public class Digest {
/**
 * текст
 */
private static final String SAMPLE_TEXT = "message digest example";

/**
 * @param args null
 * @throws Exception /
 */
public static void main(String[] args) throws Exception {
    JCPInit.initProviders(false);
    main_(args);
}

public static void main_(String[] args) throws Exception {

    // System.out.println("DigestInputStream (GOST R 34.11-94): ");
    // System.out.println(Constants.toHexString(computeDigestWithStream(Constants.DIGEST_ALG_2001)));
    // System.out.println("MessageDigest (GOST R 34.11-94): ");
    // System.out.println(Constants.toHexString(computeDigestWithoutStream(Constants.DIGEST_ALG_2001)));
    // System.out.println("CloneDigest with params (GOST R 34.11-94): ");
    // computeDigestWithClone(Constants.DIGEST_ALG_2001);

    System.out.println("DigestInputStream (GOST R 34.11-2012, 256): ");
    System.out.println(Constants.toHexString(computeDigestWithStream(Constants.DIGEST_ALG_2012_256)));
    System.out.println("MessageDigest (GOST R 34.11-2012, 256): ");
    System.out.println(Constants.toHexString(computeDigestWithoutStream(Constants.DIGEST_ALG_2012_256)));
    System.out.println("CloneDigest with params (GOST R 34.11-2012, 256): ");
    computeDigestWithClone(Constants.DIGEST_ALG_2012_256);

    System.out.println("DigestInputStream (GOST R 34.11-2012, 512): ");
    System.out.println(Constants.toHexString(computeDigestWithStream(Constants.DIGEST_ALG_2012_512)));
    System.out.println("MessageDigest (GOST R 34.11-2012, 512): ");
    System.out.println(Constants.toHexString(computeDigestWithoutStream(Constants.DIGEST_ALG_2012_512)));
    System.out.println("CloneDigest with params (GOST R 34.11-2012, 512): ");
    computeDigestWithClone(Constants.DIGEST_ALG_2012_512);

    System.out.println("OK");

}

/**
 * Хеширование данных с использованием класса DigestInputStream
 *
 * @param algName Алгоритм хеширования.
 * @return значение хеша
 * @throws Exception /
 */
public static byte[] computeDigestWithStream(String algName) throws Exception {
    // создание объекта хеширования данных
    final MessageDigest digest =
            MessageDigest.getInstance(algName);

    // обработка хешируемых данных
    final ByteArrayInputStream stream =
            new ByteArrayInputStream(SAMPLE_TEXT.getBytes());
    final DigestInputStream digestStream =
            new DigestInputStream(stream, digest);
    while (digestStream.available() != 0)
        digestStream.read();

    // вычисление значения хеша
    return digest.digest();
}

/**
 * Хеширование данных только при помощи класса MessageDigest
 *
 * @param algName Алгоритм хеширования.
 * @return значение хеша
 * @throws Exception /
 */
public static byte[] computeDigestWithoutStream(String algName) throws Exception {
    // создание объекта хеширования данных
    final MessageDigest digest =
            MessageDigest.getInstance(algName);

    // обработка хешируемых данных
    final byte[] data = SAMPLE_TEXT.getBytes();
    digest.update(data);

    // вычисление значения хеша
    return digest.digest();
}

/**
 * Хеширование данных с заданными параметрами хеширования при помощи операции
 * копирования объекта хеширования
 *
 * @param algName Алгоритм хеширования.
 * @throws Exception /
 */
public static void computeDigestWithClone(String algName) throws Exception {
    // ВНИМАНИЕ! для совместимости с другими продуктами КриптоПро
    // допустимо использовать только параметры по умолчанию:
    // "1.2.643.2.2.30.1"

    final OID digestOID = new OID("1.2.643.2.2.30.2");
    // создание объекта хеширования данных
    final MessageDigest digest = MessageDigest.getInstance(algName);

    // изменение параметров хеширования
    final AbstractGostDigest gostDigest = (AbstractGostDigest) digest;

    if (algName.equalsIgnoreCase(JCP.GOST_DIGEST_NAME)) {
        gostDigest.reset(digestOID);
    } // if

    final byte[] data = SAMPLE_TEXT.getBytes();
    //первая часть данных
    final byte[] firstBloc = new byte[data.length / 2];
    //вторая часть данных
    final byte[] secondBloc = new byte[data.length - firstBloc.length];

    // обработка первой части хешируемых данных
    gostDigest.update(firstBloc);

    // копирование (сохранение) объекта хеширования после обработки
    // первой части
    final MessageDigest firstDigest = (MessageDigest) gostDigest.clone();

    // обработка второй части хешируемых данных
    gostDigest.update(secondBloc);

    // вычисление значения хеша первой части данных
    final byte[] resultFirst = firstDigest.digest();
    System.out.println(Constants.toHexString(resultFirst));

    // вычисление значения хеша всего массива данных
    final byte[] resultAll = gostDigest.digest();
    System.out.println(Constants.toHexString(resultAll));
}
}
