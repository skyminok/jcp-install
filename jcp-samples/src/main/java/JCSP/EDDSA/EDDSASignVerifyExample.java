/**
 * Copyright 2004-2024 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.EDDSA;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.AlgIdSpecForeign;
import ru.CryptoPro.JCP.tools.Encoder;
import ru.CryptoPro.JCSP.JCSPEDDSA;

import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Примеры создания и проверки подписи EDDSA.
 */
public class EDDSASignVerifyExample {

    final static String message = "Message for signature";

    /**
     * В данном примере осуществляется генерация ключа EDDSA,
     * создание и проверка подписи.
     *
     * @throws Exception
     */
    public static void signAndVerify() throws Exception {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(
                JCP.EDDSA_NAME, JCSPEDDSA.PROVIDER_NAME);

        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        Signature signer = Signature.getInstance( JCP.SIGN_EDDSA_NAME,
                JCSPEDDSA.PROVIDER_NAME);

        signer.initSign(privateKey);
        signer.update(message.getBytes());

        byte[] signature = signer.sign();

        Encoder encoder = new Encoder();
        System.out.println("Signature: " + encoder.encode(signature) +
                "\nfor Message: " + message);

        Signature validator = Signature.getInstance( JCP.SIGN_EDDSA_NAME,
                JCSPEDDSA.PROVIDER_NAME);

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
        // Добавление провайдеров.
        JCPInit.initProviders(true);

        signAndVerify();
    }

}
