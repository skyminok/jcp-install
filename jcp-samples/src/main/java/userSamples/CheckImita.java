/**
 * $RCSfile$
 * version $Revision$
 * created 27.09.2005 20:51:25 by elvira
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

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.Certificate;

/**
 * В данном примере осуществляется имитопреобразование в соответствии с
 * алгоритмом ГОСТ Р 28147-89 на ключах согласования сторон.
 */
public class CheckImita {
/**
 * текст
 */
private static final String SAMPLE_TEXT = "Example text";
/**
 * длина вектора
 */
private static final int RND_LENGTH = 8;

/**
 * @param args null
 * @throws Exception
 */
public static void main(String[] args) throws Exception {
    JCPInit.initProviders(false);
    // main_(Constants.EXCH_KEY_PAIR_ALG_2001);
    main_(Constants.EXCH_KEY_PAIR_ALG_2012_256);
    main_(Constants.EXCH_KEY_PAIR_ALG_2012_512);
}

/**
 * Проверка имитовствки.
 *
 * @param keyAlg Алгоритм ключа.
 * @throws Exception
 */
public static void main_(String keyAlg) throws Exception {

    final byte[] data = SAMPLE_TEXT.getBytes();

    /**На каждой стороне должны присутствовать:
     * - свой закрытый ключ
     * - открытый ключ второй стороны (сертификат)
     **/

    /* Генерирование закрытых ключей сторон */
    final KeyPair alisaPair = KeyPairGen.genKey(keyAlg);
    final KeyPair bobPair = KeyPairGen.genKey(keyAlg);

    /* Генерирование самоподписанных сертификатов сторон */
    final Certificate alisaCert = KeyPairGen
            .genSelfCert(alisaPair, "CN=ALISA_CERTIFICATE, O=CryptoPro, C=RU");
    final Certificate bobCert = KeyPairGen
            .genSelfCert(bobPair, "CN=BOB_CERTIFICATE, O=CryptoPro, C=RU");

    /* Генерирование начальной синхропосылки для выработки ключа согласования*/
    final byte[] sv = new byte[RND_LENGTH];
    final SecureRandom random = SecureRandom.getInstance(Constants.RANDOM_ALG);
    random.nextBytes(sv);
    final IvParameterSpec ivspec = new IvParameterSpec(sv);

    /* Выработка ключа согласования алисы c SV*/
    final KeyAgreement alisaKeyAgree =
            KeyAgreement.getInstance(keyAlg);
    alisaKeyAgree.init(alisaPair.getPrivate(), ivspec, null);
    alisaKeyAgree.doPhase(bobCert.getPublicKey(), true);
    final SecretKey alisaAgree =
            alisaKeyAgree.generateSecret(Constants.CHIPHER_ALG);

    /* Подсчет имиты на ключе согласования алисы */
    Mac mac = Mac.getInstance(Constants.CHIPHER_ALG);
    mac.init(alisaAgree);
    mac.update(data);
    final byte[] alisaImita = mac.doFinal();

    /* Выработка ключа согласования боба с тем же SV. */
    final KeyAgreement bobKeyAgree =
            KeyAgreement.getInstance(keyAlg);
    bobKeyAgree.init(bobPair.getPrivate(), ivspec, null);
    bobKeyAgree.doPhase(alisaCert.getPublicKey(), true);
    final SecretKey bobAgree = bobKeyAgree.generateSecret(Constants.CHIPHER_ALG);

    /* Подсчет имиты на ключе согласования боба */
    mac = Mac.getInstance(Constants.CHIPHER_ALG);
    mac.init(bobAgree);
    mac.update(data);
    final byte[] bobImita = mac.doFinal();

    // проверка результатов.
    if (alisaImita.length != bobImita.length)
        throw new Exception("Error in computing imita");

    for (int i = 0; i < alisaImita.length; i++)
        if (alisaImita[i] != bobImita[i])
            throw new Exception("Error in computing imita");

    System.out.println("OK");
}
}
