/**
 * $RCSfile$
 * version $Revision$
 * created 14.04.2005 18:01:52 by elvira
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

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.params.DigestParamsSpec;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.params.ParamsInterface;
import ru.CryptoPro.JCP.tools.Platform;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * В данном примере осуществляется создание и проверка ЭЦП в соответствии с
 * алгоритмом ГОСТ Р 34.10-2001/2012.
 */
public class SignAndVerify {
/**
 * текст
 */
private static final String SAMPLE_TEXT = "generating and verifing signature";

/**
 * @param args null
 * @throws Exception
 */
public static void main(String[] args) throws Exception {
    JCPInit.initProviders(false);
    // main_(JCP.GOST_EL_EPH_DEGREE_NAME, Constants.SIGN_EL_ALG_2001, Constants.SIGN_CP_ALG_2001);
    main_(JCP.GOST_EPH_2012_256_NAME, Constants.SIGN_EL_ALG_2012_256, Constants.SIGN_CP_ALG_2012_256);
    main_(JCP.GOST_EPH_2012_512_NAME, Constants.SIGN_EL_ALG_2012_512, Constants.SIGN_CP_ALG_2012_512);
}

    /**
     * Формирование подписи и проверка.
     *
     * @param keyAlg Алгоритм ключа.
     * @param signAlgDirect Алгоритм подписи JCP.
     * @param signAlgBack Алгоритм подписи для совместимости с КриптоПро CSP.
     * @throws Exception
     */
public static void main_(String keyAlg, String signAlgDirect,
    String signAlgBack) throws Exception {

    //* Генерирование ключевой пары
    final KeyPair keyPair = KeyPairGen.genKey(keyAlg);

    //* Создание подписи типа "GOST3411withGOST3410EL"
    final byte[] signEL = sign(signAlgDirect, keyPair.getPrivate(),
            SAMPLE_TEXT.getBytes());
    System.out.println("Value of signature (signEL) is:");
    System.out.println(Constants.toHexString(signEL));
    // Проверка подписи
    final boolean signELver = verify(signAlgDirect, keyPair.getPublic(),
            SAMPLE_TEXT.getBytes(), signEL);
    System.out.println("Signature verifies (signEL) is: " + signELver);

    //* Создание подписи типа "CryptoProSignature" (совместимо с КриптоПро CSP)
    final byte[] signCP = sign(signAlgBack, keyPair.getPrivate(),
            SAMPLE_TEXT.getBytes());
    System.out.println("Value of signature (signCP) is:");
    System.out.println(Constants.toHexString(signCP));
    // Проверка подписи
    final boolean signCPver = verify(signAlgBack, keyPair.getPublic(),
            SAMPLE_TEXT.getBytes(), signCP);
    System.out.println("Signature verifies (signCP) is: " + signCPver);

    //* Создание подписи с изменением параметров хеширования

    // ВНИМАНИЕ! для совместимости с другими продуктами КриптоПро
    // допустимо использовать только параметры по умолчанию:
    // DigestParamsSpec.OID_HashVerbaO ("1.2.643.2.2.30.1")

    ParamsInterface digestParams = null;
    if (signAlgDirect.equals(JCP.GOST_EL_SIGN_NAME) || signAlgDirect.equals(JCP.CRYPTOPRO_SIGN_NAME)) {
        final OID digestOid = DigestParamsSpec.OID_HashVar_1; //"1.2.643.2.2.30.2";
        digestParams = DigestParamsSpec.getInstance(digestOid);
    } // if

    //подпись
    final byte[] sign = sign(signAlgDirect, keyPair.getPrivate(),
            SAMPLE_TEXT.getBytes(), digestParams);
    System.out.println("Value of signature (sign) is:");
    System.out.println(Constants.toHexString(sign));
    // Проверка подписи с изменением параметров хеширования
    final boolean ver = verify(signAlgDirect, keyPair.getPublic(),
            SAMPLE_TEXT.getBytes(), sign, digestParams);
    System.out.println("Signature verifies (sign) is: " + ver);

    System.out.println("OK");
}

/**
 * Создание подписи
 *
 * @param alghorithmName алгоритм подписи
 * @param privateKey закрытый ключ
 * @param data подписываемые данные
 * @return подпись
 * @throws Exception /
 */
public static byte[] sign(String alghorithmName, PrivateKey privateKey,
                          byte[] data) throws Exception {
    final Signature sig = Signature.getInstance(alghorithmName);
    sig.initSign(privateKey);
    sig.update(data);
    return sig.sign();
}

/**
 * Создание подписи с изменением параметров хеширования
 *
 * @param alghorithmName алгоритм подписи
 * @param privateKey закрытый ключ
 * @param data подписываемые данные
 * @param digestParams параметры хеширования
 * @return подпись
 * @throws Exception /
 */
public static byte[] sign(String alghorithmName, PrivateKey privateKey,
                          byte[] data, ParamsInterface digestParams)
        throws Exception {
    final Signature sig = Signature.getInstance(alghorithmName);
    sig.initSign(privateKey);
    /**
     * Java производства IBM не поддерживает установку параметров.
     */
    if (!Platform.isIbm && digestParams != null) {
        sig.setParameter(digestParams);
    }
    sig.update(data);
    return sig.sign();
}

/**
 * Проверка подписи на открытом ключе
 *
 * @param alghorithmName алгоритм подписи
 * @param publicKey открытый ключ
 * @param data подписываемые данные
 * @param signature подпись
 * @return true - верна, false - не верна
 * @throws Exception /
 */
public static boolean verify(String alghorithmName, PublicKey publicKey,
                             byte[] data, byte[] signature) throws Exception {
    final Signature sig = Signature.getInstance(alghorithmName);
    sig.initVerify(publicKey);
    sig.update(data);
    return sig.verify(signature);
}

/**
 * Проверка подписи на открытом ключе с изменением параметров хеширования
 *
 * @param alghorithmName алгоритм подписи
 * @param publicKey открытый ключ
 * @param data подписываемые данные
 * @param signature подпись
 * @param digestParams параметры хеширования
 * @return true - верна, false - не верна
 * @throws Exception /
 */
public static boolean verify(String alghorithmName, PublicKey publicKey,
                             byte[] data, byte[] signature,
                             ParamsInterface digestParams) throws Exception {
    final Signature sig = Signature.getInstance(alghorithmName);
    sig.initVerify(publicKey);
    /**
     * Java производства IBM не поддерживает установку параметров.
     */
    if (!Platform.isIbm && digestParams != null) {
        sig.setParameter(digestParams);
    }

    sig.update(data);
    return sig.verify(signature);
}
}
