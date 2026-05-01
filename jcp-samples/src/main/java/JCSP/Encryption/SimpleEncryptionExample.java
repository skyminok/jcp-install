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
package JCSP.Encryption;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.params.CryptParamsSpec;

import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCP.Util.JCPInit;

/**
 * Пример шифрования на симметричном ключе на алгоритме 28147
 * с помощью Java CSP.
 *
 * В примере используются разные режимы шифрования.
 */
public class SimpleEncryptionExample {

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(true);
        String cipherAlg = JCP.GOST_CIPHER_NAME;
        String defaultProvider = JCSP.PROVIDER_NAME;
        String defaultCipherProvider = JCSP.PROVIDER_NAME;
        userSamples.SimpleEncryptionExample.execute(cipherAlg, cipherAlg, defaultProvider, defaultCipherProvider, CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z));
        userSamples.SimpleEncryptionExample.execute(cipherAlg, "GOST28147/ECB/PKCS5_PADDING", defaultProvider, defaultCipherProvider, null);
        userSamples.SimpleEncryptionExample.execute(cipherAlg, "GOST28147/CBC/PKCS5_PADDING", defaultProvider, defaultCipherProvider, CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z));
        userSamples.SimpleEncryptionExample.execute(cipherAlg, "GOST28147/CFB/PKCS5_PADDING", defaultProvider, defaultCipherProvider, null);
        userSamples.SimpleEncryptionExample.execute(cipherAlg, "GOST28147/OFB/PKCS5_PADDING", defaultProvider, defaultCipherProvider, CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z));
    }

}
