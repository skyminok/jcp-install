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
package JCSP.RSA;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Encoder;
import ru.CryptoPro.JCSP.JCSPRSA;

import java.security.*;

/**
 * Примеры создания и проверки подписи RSA.
 */
public class RSASignVerifyExample {

    final static String message = "Message for signature";

    /**
     * В данном примере осуществляется генерация ключа JCSPRSA заданной длины,
     * создание и проверка подписи.
     *
     * @param keyLen Длина ключа JCSPRSA.
     * @param signAlgName Алгоритм подписи.
     * @throws Exception
     */
    public static void signAndVerify(int keyLen, String signAlgName) throws Exception {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(
                JCP.RSA_NAME, JCSPRSA.PROVIDER_NAME);
        keyGen.initialize(keyLen);

        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        Signature signer = Signature.getInstance(signAlgName,
                JCSPRSA.PROVIDER_NAME);

        signer.initSign(privateKey);
        signer.update(message.getBytes());

        byte[] signature = signer.sign();

        Encoder encoder = new Encoder();
        System.out.println("Signature: " + encoder.encode(signature) +
                "\nfor Message: " + message);

        Signature validator = Signature.getInstance(signAlgName,
                JCSPRSA.PROVIDER_NAME);

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

        //Создание и проверка подписи SHA1withRSA для ключа RSA длиной 1024
        signAndVerify(1024, JCP.SIGN_SHA1_RSA_NAME);

        //Создание и проверка подписи SHA256withRSA для ключа RSA длиной 2048
        signAndVerify(2048, JCP.SIGN_SHA256_RSA_NAME);

        //Создание и проверка подписи SHA384withRSA для ключа RSA длиной 1024
        signAndVerify(1024, JCP.SIGN_SHA384_RSA_NAME);

        //Создание и проверка подписи SHA512withRSA для ключа RSA длиной 2048
        signAndVerify(2048, JCP.SIGN_SHA512_RSA_NAME);

    }

}
