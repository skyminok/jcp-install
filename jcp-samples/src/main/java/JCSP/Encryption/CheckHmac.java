/**
 * $RCSfileCheckHmac.java,v $ version $Revision$ created 19.06.2019 17:22 by
 * elvira last modified $Date$ by $Author$
 * <p>
 * Copyright 2004-2019 Crypto-Pro. All rights reserved. Этот файл содержит
 * информацию, являющуюся собственностью компании Крипто-Про.
 * <p>
 * Любая часть этого файла не может быть скопирована, исправлена, переведена на
 * другие языки, локализована или модифицирована любым способом,
 * откомпилирована, передана по сети с или на любую компьютерную систему без
 * предварительного заключения соглашения с компанией Крипто-Про.
 */
package JCSP.Encryption;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.tools.Array;

import ru.CryptoPro.JCSP.JCSP;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * В данном примере высчитывается и проверяется HMAC для разных ГОСТ алгоритмов.
 *
 * @author Copyright 2004-2019 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class CheckHmac {

    // данные для подсчета MAC
    public static String DATA = "This is text for mac computation";

    public static void main(String[] args) throws Exception {

        // проверка алгоритма MAC_3411_94
        CheckMac("HMAC_GOSTR3411", JCP.GOST_DIGEST_NAME);

        // проверка алгоритма MAC_3411_2012_256
        CheckMac("HMAC_GOSTR3411_2012_256", JCP.GOST_DIGEST_2012_256_NAME);

        // проверка алгоритма MAC_3411_2012_512
        CheckMac("HMAC_GOSTR3411_2012_512", JCP.GOST_DIGEST_2012_512_NAME);

    }

    /**
     * Функция проверки Mac.
     * Вырабатывается случайная последовательность данных,
     * из нее вырабатывается ключ, высчитывается mac.
     *
     * @param macAlg алгоритм MAC
     * @param hashAlg алгоритм хэширвоания
     *
     * @throws Exception
     */
    public static void CheckMac(String macAlg, String hashAlg) throws Exception {

        byte[] data = DATA.getBytes();

        // вырабатываем случайный blob данных

        final byte[] randomKeyBlob = new byte[64];
        SecureRandom random = SecureRandom.getInstance(JCP.CP_RANDOM, JCSP.PROVIDER_NAME);
        random.nextBytes(randomKeyBlob);

        // хэшируем данные из блоба

        MessageDigest md1 = MessageDigest.getInstance(hashAlg, JCSP.PROVIDER_NAME);
        md1.update(randomKeyBlob);

        byte[] key1 = md1.digest();

        // вырабатываем ключ из хэша

        SecretKeySpec spec1 = new SecretKeySpec(key1, JCSP.GOST_CIPHER_NAME);

        // считаем MAC

        final Mac mac1 = Mac.getInstance(macAlg, JCSP.PROVIDER_NAME);
        mac1.init(spec1);

        mac1.update(data, 0, data.length);
        byte[] result1 = mac1.doFinal();

        // Повторно выполняем все процедуры, имиитируя проверку.
        // хэшируем данные из блоба

        MessageDigest md2 = MessageDigest.getInstance(hashAlg, JCSP.PROVIDER_NAME);
        md2.update(randomKeyBlob);

        byte[] key2 = md2.digest();

        // вырабатываем ключ из хэша

        SecretKeySpec spec2 = new SecretKeySpec(key2, JCSP.GOST_CIPHER_NAME);

        // считаем MAC

        final Mac mac2 = Mac.getInstance(macAlg, JCSP.PROVIDER_NAME);
        mac2.init(spec2);

        mac2.update(data, 0, data.length);
        byte[] result2 = mac2.doFinal();

        // сверяем данные

        if (!Array.compare(result1, result2)) {
            throw new Exception("Mac are not equal!");
        } // if

    }

}
