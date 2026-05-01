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
package JCSP.Signature;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Encoder;
import ru.CryptoPro.JCSP.JCSP;

import java.security.*;

/**
 * Пример подписью и проверки подписи, полученной с помощью
 * сгенерированных ключей и криптопровайдера JCSP.
 */
public class SignByGenKeyExample {

    /**
     * Подпись и проверка подписи.
     *
     * @param keyGenAlgName Алгоритм ключей.
     * @param keyGenProvider Имя провйдера.
     * @param signAlgName Алгоритм подписи.
     * @throws Exception
     */
    public static void signAndVerify(String keyGenAlgName,
        String keyGenProvider, String signAlgName) throws Exception {

        final String message = "Message for signature";
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(
            keyGenAlgName, keyGenProvider);

        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        Signature signer = Signature.getInstance(signAlgName,
            JCSP.PROVIDER_NAME);

        signer.initSign(privateKey);
        signer.update(message.getBytes());

        byte[] signature = signer.sign();

        Encoder encoder = new Encoder();
        System.out.println("Signature: " + encoder.encode(signature) +
            "\nfor Message: " + message);

        Signature validator = Signature.getInstance(signAlgName,
            JCSP.PROVIDER_NAME);

        validator.initVerify(publicKey);
        validator.update(message.getBytes());

        System.out.println("Signature verified: " +
            validator.verify(signature));

    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        JCPInit.initProviders(true);

        // ГОСТ Р 34.10-2001 DH
        // signAndVerify(JCP.GOST_EL_DEGREE_NAME, JCSP.PROVIDER_NAME,
        //     JCP.CRYPTOPRO_SIGN_NAME);

        // ГОСТ Р 34.10-2012 (256) DH
        signAndVerify(JCP.GOST_EL_2012_256_NAME, JCSP.PROVIDER_NAME,
             JCP.CRYPTOPRO_SIGN_2012_256_NAME);

        // ГОСТ Р 34.10-2012 (512) DH
        signAndVerify(JCP.GOST_EL_2012_512_NAME, JCSP.PROVIDER_NAME,
             JCP.CRYPTOPRO_SIGN_2012_512_NAME);

    }

}
