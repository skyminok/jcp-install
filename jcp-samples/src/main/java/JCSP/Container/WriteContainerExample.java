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

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.AlgIdSpec;

import ru.CryptoPro.JCPRequest.GostCertificateRequest;

import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCP.spec.NameAlgIdSpec;

import userSamples.Certificates;

import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;

/**
 * Пример создания ключевой пары и записи закрытого ключа и
 * сертификата в контейнер с помощью JCSP.
 */
public class WriteContainerExample implements IContainers {

    private enum ProviderType {pt2001, pt2012Short, pt2012Long};

    /**
     * Генерация запроса на сертификат (PKCS10) и получение
     * сертификата.
     *
     * @param name CN сертификата.
     * @param privateKey Закрытый ключ.
     * @param publicKey Открытый ключ.
     * @param keyAlgName Алгоритм ключа.
     * @param signAlgName Алгоритм подписи.
     * @return сформированный сертификат.
     * @throws Exception
     */
    public static X509Certificate generateCertificate(String name,
        PrivateKey privateKey, PublicKey publicKey, String keyAlgName,
        String signAlgName) throws Exception {

        // Создание запроса на сертификат аутентификации сервера.
        GostCertificateRequest request =
            new GostCertificateRequest(JCSP.PROVIDER_NAME);

        request.init(keyAlgName);
        request.setSubjectInfo("CN=" + name + ",C=RU");
        request.setPublicKeyInfo(publicKey);
        request.encodeAndSign(privateKey, signAlgName);

        // Отправка запроса центру сертификации и получение от центра
        // сертификата в DER-кодировке.
        byte[] encodedCertificate =
            request.getEncodedCert(Certificates.HTTP_ADDRESS);

        CertificateFactory cf = CertificateFactory.getInstance("X509");
        return (X509Certificate) cf.generateCertificate(
            new ByteArrayInputStream(encodedCertificate));
    }

    /**
     * Создание контейнера, установка в него сертификата,
     * получаемого в тестовом УЦ.
     *
     * @param name Имя контейнера.
     * @param keyGenAlgName Алгоритм ключей.
     * @param keyGenProvider Имя провйдера.
     * @param signAlgName Алгоритм подписи.
     * @param password Пароль на создаваемый контейнер, если
     * askPinInWindowOnCopy равен false.
     * @param askPinInWindowOnCopy True, если вводить пароль нужно
     * в окне CSP.
     * @throws Exception
     */
    public static void createAndSave(String name, String keyGenAlgName,
        String keyGenProvider, ProviderType pt, String signAlgName,
        char[] password, boolean askPinInWindowOnCopy) throws Exception {

        // Если True, то создаем сразу рабочий контейнер, а
        // не временный.
        boolean avoidTempContainer = true;

        // 1. Создаем пару ключей DH.

        KeyPairGenerator keyGen = KeyPairGenerator
                .getInstance(keyGenAlgName, keyGenProvider);

        if (avoidTempContainer) {

            // Если зададим путь к контейнеру у генератора,
            // то будем работать сразу с рабочим контейнером.

            String container = "\\\\.\\HDIMAGE\\" + name;

            // Если генерируем DH ключ, то, чтобы не испортить
            // параметры, используем параметры по умолчанию.

            AlgIdSpec params = null;

            if (pt == ProviderType.pt2001) {

                if (keyGenAlgName.equalsIgnoreCase(JCP.GOST_EL_DEGREE_NAME)) {
                    params = new NameAlgIdSpec(container);
                } // if
                else if (keyGenAlgName.equalsIgnoreCase(JCP.GOST_EL_DH_NAME)) {
                    params = new NameAlgIdSpec(AlgIdSpec.getDHDefault(), container);
                } // else

            } // if
            else if (pt == ProviderType.pt2012Short) {

                if (keyGenAlgName.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME)) {
                    params = new NameAlgIdSpec(AlgIdSpec.OID_PARAMS_SIG_2012_256, container);
                } // if
                else if (keyGenAlgName.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME)) {
                    params = new NameAlgIdSpec(AlgIdSpec.OID_PARAMS_EXC_2012_256,  container);
                } // else

            } // else
            else if (pt == ProviderType.pt2012Long) {

                if (keyGenAlgName.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME)) {
                    params = new NameAlgIdSpec(AlgIdSpec.OID_PARAMS_SIG_2012_512, container);
                } // if
                else if (keyGenAlgName.equalsIgnoreCase(JCP.GOST_DH_2012_512_NAME)) {
                    params = new NameAlgIdSpec(AlgIdSpec.OID_PARAMS_EXC_2012_512,  container);
                } // else

            } // else

            keyGen.initialize(params);

        } // if

        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        // 2. Получаем сертификат из УЦ.

        X509Certificate certificate =
            generateCertificate(name, privateKey, publicKey,
                keyGenAlgName, signAlgName);

        System.out.println("Private key: " + privateKey);
        System.out.println("Certificate:\n\tSn - " +
                certificate.getSerialNumber().toString(16) +
                "\n\tSubject - " + certificate.getSubjectDN() +
                "\n\tIssuer - " + certificate.getIssuerDN());

        // 3. Помещаем ключ и сертификат в контейнер.

        KeyStore keyStore = KeyStore.getInstance(JCSP.HD_STORE_NAME,
            JCSP.PROVIDER_NAME);
        keyStore.load(null, null);

        if (askPinInWindowOnCopy) {

            // Пароль не важен, он был уже запрошен в окне
            // в случае avoidTempContainer=true либо будет
            // запрошен, если был avoidTempContainer=false.

            keyStore.setKeyEntry(name, privateKey, null,
                new Certificate[] {certificate});

        } // if
        else {

            // Нам известен пароль, можно обойтись без окна и
            // сохранить контейнер с паролем PASSWORD.

            KeyStore.ProtectionParameter protectedParam =
                new KeyStore.PasswordProtection(password);

            KeyStore.Entry entry = new JCPPrivateKeyEntry(privateKey,
                new Certificate[] {certificate});

            keyStore.setEntry(name, entry, protectedParam);

        } // else

    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        JCPInit.initProviders(true);

        // True, если будем вводить пин-код в окне CSP при
        // копировании временного ключа в рабочий контейнер
        // NAME в случае avoidTempContainer=false.
        boolean askPinInWindowOnCopy = false;

        // ГОСТ Р 34.10-2001 DH
        // createAndSave(ALIAS_01, JCP.GOST_EL_DH_NAME, JCSP.PROVIDER_NAME,
        //     ProviderType.pt2001, JCP.GOST_EL_SIGN_NAME, PASSWORD_01,
        //         askPinInWindowOnCopy);

        // ГОСТ Р 34.10-2012 (256) DH
        createAndSave(ALIAS_2012_256, JCP.GOST_DH_2012_256_NAME,
            JCSP.PROVIDER_NAME, ProviderType.pt2012Short,
                JCP.GOST_SIGN_2012_256_NAME, PASSWORD_2012_256,
                    askPinInWindowOnCopy);

        // ГОСТ Р 34.10-2012 (512) DH
        createAndSave(ALIAS_2012_512, JCP.GOST_DH_2012_512_NAME,
            JCSP.PROVIDER_NAME, ProviderType.pt2012Long,
                JCP.GOST_SIGN_2012_512_NAME, PASSWORD_2012_512,
                    askPinInWindowOnCopy);

    }

}
