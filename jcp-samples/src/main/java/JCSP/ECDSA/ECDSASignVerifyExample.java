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
package JCSP.ECDSA;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Encoder;
import ru.CryptoPro.JCSP.JCSPECDSA;

import java.security.*;

/**
 * Примеры создания и проверки подписи ECDSA.
 */
public class ECDSASignVerifyExample {

    final static String message = "Message for signature";

    /**
     * В данном примере осуществляется генерация ключа ECDSA заданной длины,
     * создание и проверка подписи.
     *
     * @param keyLen Длина ключа ECDSA.
     * @param signAlgName Алгоритм подписи.
     * @throws Exception
     */
    public static void signAndVerify(int keyLen, String signAlgName) throws Exception {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(
                JCP.ECDSA_NAME, JCSPECDSA.PROVIDER_NAME);
        keyGen.initialize(keyLen);

        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        Signature signer = Signature.getInstance(signAlgName,
                JCSPECDSA.PROVIDER_NAME);

        signer.initSign(privateKey);
        signer.update(message.getBytes());

        byte[] signature = signer.sign();

        Encoder encoder = new Encoder();
        System.out.println("Signature: " + encoder.encode(signature) +
                "\nfor Message: " + message);

        Signature validator = Signature.getInstance(signAlgName,
                JCSPECDSA.PROVIDER_NAME);

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

        //Создание и проверка подписи SHA1withECDSA для ключа ECDSA с параметрами кривой secp192r1
        signAndVerify(192, JCP.SIGN_SHA1_ECDSA_NAME);

        //Создание и проверка подписи SHA256withECDSA для ключа ECDSA с параметрами кривой secp256r1
        signAndVerify(256, JCP.SIGN_SHA256_ECDSA_NAME);

        //Создание и проверка подписи SHA384withECDSA для ключа ECDSA с параметрами кривой secp224r1
        signAndVerify(224, JCP.SIGN_SHA384_ECDSA_NAME);

        //Создание и проверка подписи SHA512withECDSA для ключа ECDSA с параметрами кривой secp384r1
        signAndVerify(384, JCP.SIGN_SHA512_ECDSA_NAME);

        //Создание и проверка подписи SHA1withECDSA для ключа ECDSA с параметрами кривой secp521r1
        signAndVerify(521, JCP.SIGN_SHA1_ECDSA_NAME);

    }

}
