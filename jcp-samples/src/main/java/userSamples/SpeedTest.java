/**
 * $RCSfile$
 * version $Revision$
 * created 13.10.2008 14:47:53 by Iva
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2008.
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

import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.CryptParamsSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.Arrays;

/**
 * Определение скоростей JCP.
 *
 * @author Copyright 2004-2008 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class SpeedTest {
/**
 * size of byte array.
 */
public static final int BLOCK_SIZE = 16384;
/**
 * repeate amount.
 */
public static final int LOOPS_AMOUNT = 250;
/**
 * Static class, ctor forbidden.
 */
private SpeedTest() {
}

/**
 * @param args
 * @throws Exception
 */
public static void main(String[] args) throws Exception {
    JCPInit.initProviders(false);
    // main_(JCP.GOST_DIGEST_NAME, false, JCP.GOST_EL_DH_EPH_NAME, JCP.GOST_EL_SIGN_NAME);
    main_(JCP.GOST_DIGEST_2012_256_NAME, true, JCP.GOST_EPH_DH_2012_256_NAME, JCP.GOST_SIGN_2012_256_NAME);
    main_(JCP.GOST_DIGEST_2012_512_NAME, true, JCP.GOST_EPH_DH_2012_512_NAME, JCP.GOST_SIGN_2012_512_NAME);
}

/**
 * Замер скорости.
 *
 * @param digestAlg Алгоритм хеширования.
 * @param tc26Z True, если следует использовать инициализацию
 * генератора симметричного ключа параметрами TC26 Z.
 * @param keyAlg Алгоритм ключевой пары.
 * @param signAlg Алгоритм подписи.
 * @throws Exception any error
 */
public static void main_(String digestAlg, boolean tc26Z,
    String keyAlg, String signAlg) throws Exception {

    byte[] testText = new byte[8];
    byte[] longText = new byte[BLOCK_SIZE];
    long startTime;
    long endTime;
    // create objects
    MessageDigest digest = MessageDigest.getInstance(digestAlg);
    SecureRandom random = SecureRandom.getInstance(JCP.CP_RANDOM);
    IvParameterSpec params = new IvParameterSpec(testText);
    Cipher cipher = Cipher.getInstance("GOST28147/CFB/NoPadding");
    // generate keys
    KeyGenerator kg = KeyGenerator.getInstance(CryptoProvider.GOST_CIPHER_NAME);
    if (tc26Z) {
        kg.init(CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z));
    }
    SecretKey key = kg.generateKey();
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyAlg);
    java.security.KeyPair pair = keyGen.generateKeyPair();
    PrivateKey privateKey = pair.getPrivate();
    PublicKey publicKey = pair.getPublic();
    // test create sign
    Signature signature = Signature.getInstance(signAlg);
    signature.initSign(privateKey);
    signature.update(testText);
    byte[] signBytes = signature.sign();
    // test verify sign
    signature.initVerify(publicKey);
    signature.update(testText);
    if (!signature.verify(signBytes))
        throw new ProviderException();
    // test encrypt
    cipher.init(Cipher.ENCRYPT_MODE, key, params);
    byte[] encryptResult = cipher.doFinal(testText);
    cipher.init(Cipher.DECRYPT_MODE, key, params);
    byte[] decryptResult = cipher.doFinal(encryptResult);
    if (!Arrays.equals(decryptResult, testText))
        throw new ProviderException();

    // test random speed
    random.nextBytes(longText);
    startTime = System.currentTimeMillis();
    for (int i = 0; i < LOOPS_AMOUNT; i++) {
        random.nextBytes(longText);
    }
    endTime = System.currentTimeMillis();
    long randomSpeed =
            ((long) BLOCK_SIZE * LOOPS_AMOUNT) / (endTime - startTime);

    // test digest speed.
    digest.update(longText);
    startTime = System.currentTimeMillis();
    for (int i = 0; i < LOOPS_AMOUNT; i++) {
        digest.update(longText);
    }
    endTime = System.currentTimeMillis();
    long digestSpeed =
            ((long) BLOCK_SIZE * LOOPS_AMOUNT) / (endTime - startTime);

    // test encrypt speed
    cipher.init(Cipher.ENCRYPT_MODE, key, params);
    cipher.update(longText);
    startTime = System.currentTimeMillis();
    for (int i = 0; i < LOOPS_AMOUNT; i++) {
        cipher.update(longText);
    }
    endTime = System.currentTimeMillis();
    long cryptSpeed =
            ((long) BLOCK_SIZE * LOOPS_AMOUNT) / (endTime - startTime);

    // test sign speed
    signature.initSign(privateKey);
    signature.update(testText);
    signature.sign();
    startTime = System.currentTimeMillis();
    for (int i = 0; i < LOOPS_AMOUNT; i++) {
        signature.initSign(privateKey);
        signature.update(testText);
        signature.sign();
    }
    endTime = System.currentTimeMillis();
    long signTime = endTime - startTime;

    // test verify speed
    signature.initVerify(publicKey);
    signature.update(testText);
    signature.verify(signBytes);
    startTime = System.currentTimeMillis();
    for (int i = 0; i < LOOPS_AMOUNT; i++) {
        signature.initVerify(publicKey);
        signature.update(testText);
        signature.verify(signBytes);
    }
    endTime = System.currentTimeMillis();
    long verifyTime = endTime - startTime;

    System.out.println("Random Speed:" + randomSpeed +
            " KB per sec.");
    System.out.println("Digest [" + digestAlg + "] Speed:" +
            digestSpeed + " KB per sec.");
    System.out.println("Crypt [use tc26-Z: " + tc26Z + "] speed:" +
            cryptSpeed + " KB per sec.");
    System.out.println("Signature [" + signAlg + "] generation time: " +
            signTime / LOOPS_AMOUNT + "." + signTime % LOOPS_AMOUNT);
    System.out.println("Signature [" + signAlg + "] verify time: " +
            verifyTime / LOOPS_AMOUNT + "." + verifyTime % LOOPS_AMOUNT);
}
}
