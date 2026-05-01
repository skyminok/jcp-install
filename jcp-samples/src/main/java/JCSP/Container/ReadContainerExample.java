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
package JCSP.Container;

import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Пример чтения закрытого ключа и сертификата из контейнера с
 * помощью JCSP.
 */
public class ReadContainerExample implements IContainers {

    /**
     * Вывод информации о сертификате и закрытом ключе.
     *
     * @param privateKey Закрытый ключ.
     * @param certificate Сертификат.
     */
    public static void printInfo(PrivateKey privateKey,
        X509Certificate certificate) {

        System.out.println("Private key: " + privateKey);
        System.out.println("Certificate:\n\tSn - " +
            certificate.getSerialNumber().toString(16) +
            "\n\tSubject - " + certificate.getSubjectDN() +
            "\n\tIssuer - " + certificate.getIssuerDN());

    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        JCPInit.initProviders(true);

        KeyStore keyStore = KeyStore.getInstance(JCSP.HD_STORE_NAME, JCSP.PROVIDER_NAME);
        keyStore.load(null, null);

        //
        // printInfo((PrivateKey)keyStore.getKey(ALIAS_01, PASSWORD_01),
        //     (X509Certificate)keyStore.getCertificate(ALIAS_01));
        //

        printInfo((PrivateKey)keyStore.getKey(ALIAS_2012_256, PASSWORD_2012_256),
            (X509Certificate)keyStore.getCertificate(ALIAS_2012_256));

        printInfo((PrivateKey)keyStore.getKey(ALIAS_2012_512, PASSWORD_2012_512),
            (X509Certificate)keyStore.getCertificate(ALIAS_2012_512));


    }

}
