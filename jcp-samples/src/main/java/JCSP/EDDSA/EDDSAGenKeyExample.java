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
import ru.CryptoPro.JCP.KeyStore.ContainerStore;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.AlgIdSpecForeign;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.spec.NameAlgIdSpecForeign;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.JCSPEDDSA;
import ru.CryptoPro.JCSP.params.PasswordParamsSpec;

import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Данный пример демонстрирует генерацию и сохранение ключей Ed25519.
 */
public class EDDSAGenKeyExample {

    //Тип хранилища для записи ключей
    static String STORETYPE = JCSP.HD_STORE_NAME;//"HSMDB";

    /**
     * Генерации ключей Ed25519.
     * В примере осуществляется генерация ключевой пары, генерация
     * самоподписанного сертификата, сохранение ключа и сертификата в контейнер.
     * @param alias Алиас ключа.
     * @param password Пароль.
     * @param dn Субъект сертификата.
     * @param signAlg Алгоритм подписи.
     * @param keyUsage Назначение ключа.
     @throws Exception
     */
    public static void generateKey( String alias, char[] password,
                                              String dn, String signAlg, int keyUsage) throws Exception{

        // Пробуем удалить контейнер на случай, если такой есть.

        try {
            deleteContainer(alias, password);
        } catch (Exception e) {
            // ignore
        }

        // Генерация ключевой пары заданной длины.

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(JCP.EDDSA_NAME, JCSPEDDSA.PROVIDER_NAME);

        // В некоторых случаях необходимо генератару передавать полный путь к контейнеру (например, в случае работы с HSM).
        NameAlgIdSpecForeign spec1 = new NameAlgIdSpecForeign("\\\\.\\" + STORETYPE + "\\" + alias);
        kpg.initialize(spec1);
        PasswordParamsSpec spec2 = new PasswordParamsSpec(password);
        kpg.initialize(spec2);

        KeyPair kp = kpg.generateKeyPair();

        PrivateKey prv_key = kp.getPrivate();
        System.out.println(prv_key);

        PublicKey pub_key = kp.getPublic();
        System.out.println(pub_key);
        System.out.println("createKeyPair() completed.");

        // Создание самоподписанного сертификата.

        X509Certificate selfCert = createSelfSignedCertificate(
                kp, dn, signAlg, keyUsage);

        // Сохранение контейнера.

        saveContainer(alias, password, kp.getPrivate(),
                new X509Certificate[] {selfCert});

    }

    /**
     * Удаление контейнера.
     *
     * @param alias Алиас ключа.
     * @param password Пароль.
     * @throws Exception
     */
    public static void deleteContainer( String alias, char[] password)
            throws Exception {

        System.out.println("deleteContainer() started...");

        KeyStore keyStore = KeyStore.getInstance(
                STORETYPE, JCSPEDDSA.PROVIDER_NAME);

        keyStore.load(null, null);

        if (password != null) {

            alias += ContainerStore.PASSWORD_PREFIX
                    + String.valueOf(password);

        } // if

        keyStore.deleteEntry(alias);
        System.out.println("deleteContainer() completed.");

    }

    /**
     * Создание самоподписанного сертификата.
     *
     * @param kp Ключевая пара.
     * @param dn Subject/Issuer сертификата.
     * @param signAlg Алгоритм подписи.
     * @param keyUsage Назначение ключа.
     * @return сертификат.
     * @throws Exception
     */
    public static X509Certificate createSelfSignedCertificate(
            KeyPair kp, String dn, String signAlg, int keyUsage) throws Exception {

        System.out.println("generateSelfSignedCertificate() started...");

        GostCertificateRequest request = new GostCertificateRequest(JCSPEDDSA.PROVIDER_NAME);
        request.setKeyUsage(keyUsage);

        byte[] enc = request.getEncodedSelfCert(kp, dn, signAlg);

        CertificateFactory cf = CertificateFactory.getInstance("X509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(enc));

        System.out.println("generateSelfSignedCertificate() completed.");
        return cert;

    }

    /**
     * Сохранение ключа и сертификатов в контейнер.
     *
     * @param alias Алиас сохраняемого ключа.
     * @param password Пароль к ключу.
     * @param privateKey Закрытый ключ.
     * @param certs Сертификат(ы).
     * @throws Exception
     */
    public static void saveContainer(String alias,
                                     char[] password, PrivateKey privateKey, X509Certificate[]
                                             certs) throws Exception {

        System.out.println("saveContainer() started...");

        KeyStore keyStore = KeyStore.getInstance(STORETYPE, JCSPEDDSA.PROVIDER_NAME);
        keyStore.load(null, null);

        JCPProtectionParameter parameter = new JCPProtectionParameter(password);
        JCPPrivateKeyEntry entry = new JCPPrivateKeyEntry(privateKey, certs);

        keyStore.setEntry(alias, entry, parameter);
        System.out.println("saveContainer() completed.");

    }


    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Добавление провайдеров.
        JCPInit.initProviders(true);

        //Генерация и сохранение ключа EDDSA
        generateKey("ed25519key",
                "1".toCharArray(),
                "CN=ed25519key",
                JCP.SIGN_EDDSA_NAME,
                GostCertificateRequest.SIGN_DEFAULT);

    }

}
