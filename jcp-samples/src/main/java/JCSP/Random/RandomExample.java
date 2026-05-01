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
package JCSP.Random;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Encoder;
import ru.CryptoPro.JCSP.JCSP;

import java.security.SecureRandom;

/**
 * Пример получения произвольной последовательности байтов с помощью
 * провайдера JCSP.
 */
public class RandomExample {

    private static final int BLOCK_SIZE = 1024;

    public static void main(String[] args) throws Exception {

        JCPInit.initProviders(true);
        final byte[] array = new byte[BLOCK_SIZE];
        final SecureRandom rnd = SecureRandom.getInstance(
            JCP.CP_RANDOM, JCSP.PROVIDER_NAME);

        rnd.nextBytes(array);

        Encoder encoder = new Encoder();
        System.out.println("Random bytes: " +
            encoder.encode(array));
    }

}
