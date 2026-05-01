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
package userSamples;

import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;

/**
 * Пример шифрования на симметричном ключе на алгоритме
 * ГОСТ Р 34.12-2015 Магма с помощью JCP.
 *
 * В примере используются разные режимы шифрования.
 * Для шифрования большого объема данных следуюет пользоваться режимом CTR_ACPKM.
 * Для остальных режимов (ECB, CBC, CFB, OFB) объем шифруемых данных ограничен значением 4 мб.
 */
public class SimpleEncryptionMagmaExample {

    public static final String message = "Message for encryption and decryption"; // данные для зашифрования
    public static final String cipherAlg = JCP.GOST_M_CIPHER_NAME; // алгоритм секретного ключа

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(false);
        String cipherAlg = JCP.GOST_M_CIPHER_NAME;
        String defaultProvider = JCP.PROVIDER_NAME;
        String defaultCipherProvider = CryptoProvider.PROVIDER_NAME;
        SimpleEncryptionExample.execute(cipherAlg, cipherAlg, defaultProvider, defaultCipherProvider, null);
        SimpleEncryptionExample.execute(cipherAlg, "GOST3412_2015_M/CTR_ACPKM/PKCS5_PADDING", defaultProvider, defaultCipherProvider, null);
        SimpleEncryptionExample.execute(cipherAlg, "GOST3412_2015_M/ECB/PKCS5_PADDING", defaultProvider, defaultCipherProvider, null);
        SimpleEncryptionExample.execute(cipherAlg, "GOST3412_2015_M/CBC/PKCS5_PADDING", defaultProvider, defaultCipherProvider, null);
        SimpleEncryptionExample.execute(cipherAlg, "GOST3412_2015_M/CFB/PKCS5_PADDING", defaultProvider, defaultCipherProvider, null);
        SimpleEncryptionExample.execute(cipherAlg, "GOST3412_2015_M/OFB/PKCS5_PADDING", defaultProvider, defaultCipherProvider, null);
    }

}
