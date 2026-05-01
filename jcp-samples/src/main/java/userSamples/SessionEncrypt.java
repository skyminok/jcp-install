/**
 * $RCSfile$
 * version $Revision$
 * created 26.09.2005 14:44:17 by elvira
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

import ru.CryptoPro.JCP.params.CryptParamsSpec;
import ru.CryptoPro.JCP.spec.GostCipherSpec;
import ru.CryptoPro.JCP.Util.JCPInit;

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
 * В данном примере производится зашифрование и расшифрование текста на
 * симметричном ключе. Для передачи этого ключа произоводится его зашифрование и
 * расшифрование на ключах согласования сторон.
 */
public class SessionEncrypt {
/**
 * текст
 */
private static final String SAMPLE_TEXT = "Classic encryption/decryption";
/**
 * длина вектора
 */
private static final int RND_LENGTH = 8;
/**
 * Алгоритм шифрования
 */
private static final String CIPHER_ALG = "GOST28147/CFB/NoPadding";

/**
 * Размер блока случайных данных
 */
public static final int randomLength = 8;
/**
 * @param args
 * @throws Exception
 */
public static void main(String[] args) throws Exception {
    JCPInit.initProviders(false);
    // main_(Constants.EXCH_KEY_PAIR_ALG_2001, false);
    main_(Constants.EXCH_KEY_PAIR_ALG_2012_256, true);
    main_(Constants.EXCH_KEY_PAIR_ALG_2012_512, true);
}

/**
 * Шифрование на симметричном ключе.
 *
 * @param keyAlg Алгоритм ключевой пары.
 * @param tc26Z True, если следует использовать инициализацию
 * генератора симметричного ключа параметрами TC26 Z.
 * @throws Exception
 */
public static void main_(String keyAlg, boolean tc26Z) throws Exception {

    final byte[] data = SAMPLE_TEXT.getBytes();

    /**На каждой стороне должны присутствовать:
     * - свой закрытый ключ
     * - открытый ключ второй стороны (сертификат)**/

    /* Генерирование закрытых ключей сторон */
    final KeyPair alisaPair = KeyPairGen.genKeyAllowDh(keyAlg);
    final KeyPair bobPair = KeyPairGen.genKeyAllowDh(keyAlg);

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
    final KeyAgreement alisaKeyAgree = KeyAgreement.getInstance(keyAlg);
    alisaKeyAgree.init(alisaPair.getPrivate(), ivspec, null);
    alisaKeyAgree.doPhase(bobCert.getPublicKey(), true);
    final SecretKey alisaAgree =
            alisaKeyAgree.generateSecret(Constants.CHIPHER_ALG);

    /* Генерирование симметричного ключа алисой с параметрами шифрования из контрольной панели*/
    final KeyGenerator keyGen = KeyGenerator.getInstance(Constants.CHIPHER_ALG);
    CryptParamsSpec sessionKeyParams = null;
    if (tc26Z) {
        sessionKeyParams = CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z);
        keyGen.init(sessionKeyParams);
    }
    final SecretKey simm = keyGen.generateKey();

    /* Зашифрование текста на симметричном ключе алисы*/
    Cipher cipher = Cipher.getInstance(CIPHER_ALG);
    cipher.init(Cipher.ENCRYPT_MODE, simm);
    // передача вектора инициализации бобу
    final byte[] iv = cipher.getIV();
    final byte[] encryptedtext = cipher.doFinal(data, 0, data.length);

    /*Зашифрование симметричного ключа на ключе согласования алисы*/
    cipher.init(Cipher.WRAP_MODE, alisaAgree);
    final byte[] wrappedKey = cipher.wrap(simm);

    /* Выработка ключа согласования боба с тем же SV. */
    final KeyAgreement bobKeyAgree =
            KeyAgreement.getInstance(keyAlg);
    bobKeyAgree.init(bobPair.getPrivate(), ivspec, null);
    bobKeyAgree.doPhase(alisaCert.getPublicKey(), true);
    final SecretKey bobAgree =
            bobKeyAgree.generateSecret(Constants.CHIPHER_ALG);

    /* Расшифрование бобом симметричного ключа.*/
    cipher.init(Cipher.UNWRAP_MODE, bobAgree);
    final SecretKey simmKey = (SecretKey) cipher
            .unwrap(wrappedKey, null, Cipher.SECRET_KEY);

    /* Расшифрование бобом текста на расшифрованном симметричном ключе. IV передан от алисы*/
    cipher = Cipher.getInstance(CIPHER_ALG);

    /*
       Параметры сессионного ключа simmKey при его расшифровании
       (UNWRAP) определяются параметрами ключа согласования.
       Если при зашифровании использовались иные параметры, то
       их можно передать с помощью GostCipherSpec в шифратор,
       расшифровывающий зашифрованные данные.
    */

    AlgorithmParameterSpec parameterSpec;
    if (tc26Z) {
        parameterSpec = new GostCipherSpec(new IvParameterSpec(iv),
            sessionKeyParams);
    }
    else {
        parameterSpec = new IvParameterSpec(iv);
    }

    cipher.init(Cipher.DECRYPT_MODE, simmKey, parameterSpec, null);
    final byte[] decryptedtext = cipher
            .doFinal(encryptedtext, 0, encryptedtext.length);

    // проверка результата.
    if (decryptedtext.length != data.length)
        throw new Exception("Error in crypting");
    for (int i = 0; i < decryptedtext.length; i++)
        if (data[i] != decryptedtext[i])
            throw new Exception("Error in crypting");

    System.out.println("OK");
}
}
