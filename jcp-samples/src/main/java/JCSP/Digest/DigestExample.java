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
package JCSP.Digest;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.tools.Encoder;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.security.MessageDigest;

/**
 * Пример получения хеша сообщения с помощью провайдера JCSP.
 */
public class DigestExample {

    /**
     * Хеширование сообщения.
     *
     * @param algName Алгоритм хеширования.
     * @throws Exception
     */
    public static void digest(String algName) throws Exception {

        final String message = "Message for digest";
        final MessageDigest messageDigest =
            MessageDigest.getInstance(algName, JCSP.PROVIDER_NAME);

        messageDigest.update(message.getBytes());
        final byte[] digest = messageDigest.digest();

        Encoder encoder = new Encoder();
        System.out.println("Digest: " + encoder.encode(digest) +
            "\nfor Message: " + message);

    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        JCPInit.initProviders(true);

        // ГОСТ Р 34.11-94
        // digest(JCP.GOST_DIGEST_NAME);

        // ГОСТ Р 34.11-2012 (256)
        digest(JCP.GOST_DIGEST_2012_256_NAME);

        // ГОСТ Р 34.11-2012 (512)
        digest(JCP.GOST_DIGEST_2012_512_NAME);
    }

}
