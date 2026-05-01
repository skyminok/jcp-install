/**
 * $RCSfileGenAndroidKeys.java,v $
 * version $Revision: 36379 $
 * created 22.03.2020 10:57 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * (C) ООО Крипто-Про 2004-2020.
 * <p/>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p/>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package ru.CryptoPro;

import ru.CryptoPro.Crypto.CryptoProvider;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.tools.AlgorithmTools;

import ru.CryptoPro.JCPRequest.GostCertificateRequest;
import userSamples.Certificates;
import userSamples.KeyPairGen;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 * Генерация ключевых контейнеров, например, для:
 *      ACSPClientApp (keys.zip)
 * TLS тестов к
 *      https://testgost2012.cryptopro.ru/
 *      https://testgost2012st.cryptopro.ru/
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class GenKeys {

    /**
     * Генерация контейнера с ключом и сертификатом из УЦ.
     * Предварительно старый контейнер удаляется.
     * Формируется ключ обмена и сертификат с использованием
     * ключа для клиентской аутентификации.
     *
     * @param keyAlgorithm Алгоритм ключа.
     * @param genProvider Провайдер ключа.
     * @param dn DN-имя сертификата.
     * @param keyStoreType Тип контейнера.
     * @param keyStoreProvider Провайдер контейнера.
     * @param alias Алиас ключа.
     * @param password Пароль к ключу.
     * @param caURL Адрес УЦ.
     * @param isServer True, если ключ для сервера.
     * @throws Exception
     */
    private static void genOne(String keyAlgorithm, String genProvider,
        String dn, String keyStoreType, String keyStoreProvider, String
        alias, char[] password, String caURL, boolean isServer) throws
        Exception {

        System.out.println("Generating started. loading key store...");

        KeyStore keyStore = KeyStore.getInstance(
            keyStoreType, keyStoreProvider);

        keyStore.load(null, null);

        try {

            System.out.println("Deleting key " + alias + "...");
            keyStore.deleteEntry(alias);

        } catch (Exception e) {}

        System.out.println("Generating key " + alias + "...");

        KeyPair keyPair = KeyPairGen.genKey(
            keyAlgorithm, genProvider);

        String sigAlgorithm = AlgorithmTools
            .getSignatureAlgorithmByPrivateKey(
                keyPair.getPrivate());

        System.out.println("Signature algorithm - " + sigAlgorithm);
        System.out.println("Creating certificate request...");

        GostCertificateRequest request = new
            GostCertificateRequest(keyStoreProvider);

        int keyUsage;

        if (keyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_DEGREE_NAME) ||
            keyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME) ||
            keyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME)) {
            keyUsage =
                GostCertificateRequest.DIGITAL_SIGNATURE |
                GostCertificateRequest.NON_REPUDIATION;
        } // if
        else {
            keyUsage = GostCertificateRequest.DIGITAL_SIGNATURE |
                GostCertificateRequest.NON_REPUDIATION |
                GostCertificateRequest.KEY_ENCIPHERMENT |
                GostCertificateRequest.KEY_AGREEMENT;
        } // else

        // client auth
        request.addExtKeyUsage(GostCertificateRequest.INTS_PKIX_CLIENT_AUTH);

        if (isServer) {
            // server auth
            request.addExtKeyUsage(GostCertificateRequest.INTS_PKIX_SERVER_AUTH);
        } // if

        request.setKeyUsage(keyUsage);
        request.setPublicKeyInfo(keyPair.getPublic());

        System.out.println("DN in the request is " + dn);

        request.setSubjectInfo(dn);
        request.encodeAndSign(keyPair.getPrivate(), sigAlgorithm);

        System.out.println("Sending certificate request to " + caURL + "...");
        byte[] encoded = request.getEncodedCert(caURL);

        CertificateFactory factory = CertificateFactory.getInstance("X509");
        System.out.println("Creating certificate from response...");

        Certificate certificate = factory.generateCertificate(
            new ByteArrayInputStream(encoded));

        System.out.println("Saving generated key and certificate" +
            " to " + alias + "...");

        keyStore.setKeyEntry(alias, keyPair.getPrivate(),
            password, new Certificate[] {certificate});

        System.out.println("Generating completed.");

    }

    /**
     * Генерация контейнера с ключом и сертификатом из УЦ.
     *
     * @param keyAlgorithm Алгоритм ключа.
     * @param alias Алиас ключа.
     * @param password Пароль к ключу.
     * @param caURL Адрес УЦ.
     * @param isServer True, если ключ для сервера.
     * @throws Exception
     */
    public static void genOne(String keyAlgorithm, String alias,
        char[] password, String caURL, boolean isServer) throws
        Exception {

        genOne(keyAlgorithm, CryptoProvider.PROVIDER_NAME,
            "CN=" + alias, JCP.HD_STORE_NAME, JCP.PROVIDER_NAME,
                alias, password, caURL, isServer);

    }

    /**
     * Запуск генерации контейнера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.out.println("<key_alg> <key_alias> <key_pass> <ca_url> [-server]");

        String keyAlgorithm = args[0];
        String keyAlias     = args[1];
        String keyPassword  = args[2];
        String caAddress    = args[3];

        if (caAddress.isEmpty()) {
            caAddress = Certificates.HTTP_ADDRESS;
        } // if

        boolean isServer = (args.length > 4) && args[4].equalsIgnoreCase("-server");

        System.out.println("*** PARAMETERS:"
            + "\n\talgorithm: " + keyAlgorithm
            + "\n\talias: " + keyAlias
            + "\n\tpassword: " + keyPassword
            + "\n\tCA URL: " + caAddress
            + "\n\tis server: " + isServer
        );

        genOne(keyAlgorithm, keyAlias, keyPassword.toCharArray(),
            caAddress, isServer);

    }

}
