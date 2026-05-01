/**
 * Copyright 2004-2013 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.Encryption;

import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.CryptParamsInterface;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCP.params.CryptDhAllowedSpec;
import util.ResolveProvider;

import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 * Класс с общими для всех примеров сущностями и определениями.
 *
 * @author 25/11/2013
 *
 */
public abstract class Common {

    protected static final String text = "Example of computing imita by two users.";
    protected static final int randomLength = 8;

    protected static final String defaultProvider = JCSP.PROVIDER_NAME;
    protected static final String MacAlgorithm = JCSP.GOST_CIPHER_NAME;
    protected static final String randomAlgorithmName = "CPRandom";

    // TODO: добавить проверку if windows then registry else hdimage.
    protected static final String KeyStoreName = ResolveProvider.ALTERNATIVE_HD_IMAGE;

    protected static final String CipherAlgorithm = JCSP.GOST_CIPHER_NAME + "/CFB/NoPadding";

    protected PrivateKey alicaPrivate = null;
    protected PrivateKey bobPrivate = null;
    protected PublicKey alicaPublic = null;
    protected PublicKey bobPublic = null;
    protected Certificate alicaCert;
    protected Certificate bobCert;

    protected abstract String getAlicaAlias();
    protected abstract char[] getAlicaPassword();

    protected abstract String getBobAlias();
    protected abstract char[] getBobPassword();

    /**
     * Создание необходимых ключей и контейнеров.
     *
     * @throws Exception
     */
    protected void prepare(String pairProvider, String exchKeyAlgorithm,
        String signAlgorithm, boolean create, boolean store) throws Exception {

        // чтение закрытого ключа алисы
        KeyStore hdImageStore = KeyStore.getInstance(KeyStoreName, defaultProvider);
        hdImageStore.load(null, null);

        if (!create && hdImageStore.isKeyEntry(getAlicaAlias())) {

            JCPProtectionParameter parameter = new JCPProtectionParameter(getAlicaPassword());
            JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) hdImageStore.getEntry(getAlicaAlias(), parameter);

            alicaPrivate = entry.getPrivateKey();
            System.out.println("Alica's private key was read from container");

            // чтение соответствующего сертификата открытого ключа алисы
            alicaCert = entry.getCertificate();
            System.out.println("Alica's certificate key was read from container");

            alicaPublic = alicaCert.getPublicKey();

        }
        // если ключа нет - генерация
        else {

            KeyPairGenerator alicaKeyGen = KeyPairGenerator
                .getInstance(exchKeyAlgorithm, pairProvider);

            // Разрешаем согласовать на ключе подписи.
            alicaKeyGen.initialize(new CryptDhAllowedSpec());

            KeyPair alicaPair = alicaKeyGen.generateKeyPair();
            System.out.println("Alica's key pair was generated");

            alicaPublic = alicaPair.getPublic();
            alicaPrivate = alicaPair.getPrivate();

            System.out.println("Alica's private key was read from container");

            if (store) {

                // Тут происходит передача имени провайдера, без него может быть
                // выбран алгоритм подписи у JCP, а не JCSP, и произойдет ошибка.
                GostCertificateRequest gr = new GostCertificateRequest(defaultProvider);
                gr.init(alicaPrivate.getAlgorithm());

                byte[] enc = gr.getEncodedSelfCert(alicaPair,
                    "CN=" + getAlicaAlias() + ", O=CryptoPro, C=RU",
                    signAlgorithm);

                CertificateFactory cf = CertificateFactory.getInstance("X509");
                alicaCert = cf.generateCertificate(new ByteArrayInputStream(enc));
                System.out.println("Alica's certificate was generated");

                JCPPrivateKeyEntry entry = new JCPPrivateKeyEntry(alicaPrivate,
                    new Certificate[] {alicaCert});

                KeyStore.PasswordProtection pp =
                    new KeyStore.PasswordProtection(getAlicaPassword());

                hdImageStore.setEntry(getAlicaAlias(), entry, pp);

                entry = (JCPPrivateKeyEntry) hdImageStore.getEntry(getAlicaAlias(), pp);
                alicaPrivate = entry.getPrivateKey();

            } // if

        } // else

        if (!create && hdImageStore.isKeyEntry(getBobAlias())) {

            JCPProtectionParameter parameter = new JCPProtectionParameter(getBobPassword());
            JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) hdImageStore.getEntry(getBobAlias(), parameter);

            bobPrivate = entry.getPrivateKey();
            System.out.println("Bob's private key was read from container");

            // чтение соответствующего сертификата открытого ключа боба
            bobCert = entry.getCertificate();
            System.out.println("Bob's certificate was read from container");

            bobPublic = bobCert.getPublicKey();

        }
        // если ключа нет - генерация
        else {

            KeyPairGenerator bobKeyGen = KeyPairGenerator
                .getInstance(exchKeyAlgorithm, pairProvider);

            // Разрешаем согласовать на ключе подписи.
            bobKeyGen.initialize(new CryptDhAllowedSpec());

            KeyPair bobPair = bobKeyGen.generateKeyPair();
            System.out.println("Bob's key pair was generated");

            bobPublic = bobPair.getPublic();
            bobPrivate = bobPair.getPrivate();

            System.out.println("Bob's private key was read from container");

            if (store) {

                // Тут происходит передача имени провайдера, без него может быть
                // выбран алгоритм подписи у JCP, а не JCSP, и произойдет ошибка.
                GostCertificateRequest gr = new GostCertificateRequest(defaultProvider);
                gr.init(bobPrivate.getAlgorithm());

                byte[] enc = gr.getEncodedSelfCert(bobPair,
                    "CN=" + getBobAlias() + ", O=CryptoPro, C=RU",
                    signAlgorithm);

                CertificateFactory cf = CertificateFactory.getInstance("X509");
                bobCert = cf.generateCertificate(new ByteArrayInputStream(enc));
                System.out.println("Bob's certificate was generated");

                JCPPrivateKeyEntry entry = new JCPPrivateKeyEntry(bobPrivate,
                    new Certificate[] {bobCert});
                KeyStore.PasswordProtection pp =
                    new KeyStore.PasswordProtection(getBobPassword());

                hdImageStore.setEntry(getBobAlias(), entry, pp);

                entry = (JCPPrivateKeyEntry) hdImageStore.getEntry(getBobAlias(), pp);
                bobPrivate = entry.getPrivateKey();

            } // if

        } // else

    }

    /**
     * Выполнение расчетов.
     *
     * @param pairProvider Имя провайдера для генерации ключевых пар.
     * @param agreeProvider Провайдер для формирования ключей согласования.
     * @param simProvider Провайдер для генерации симметричного ключа.
     * @param simParams Параметры для сессионного ключа.
     * @param exchKeyAlgorithm Алгоритм шифрования или ключа обмена.
     * @param wrapAlgorithm Алгоритм зашифрования симметричного ключа.
     * @param signAlgorithm Алгоритм подписи запроса.
     * @param create true, если следует создать контейнеры и сохранить их;
     * иначе предполагается, что они существуют и их нужно загрузить.
     * @param delete true, если по завершении следует удалить контейнер.
     * @throws Exception
     */
    public abstract void execute(String pairProvider, String agreeProvider,
        String simProvider, CryptParamsInterface simParams, String exchKeyAlgorithm,
        String wrapAlgorithm, String signAlgorithm, boolean create,
        boolean delete) throws Exception;

    /**
     * Удаление созданных контейнеров.
     *
     * @throws Exception
     */
    protected void clear(boolean delete) throws Exception {

        if (delete) {

            KeyStore keyStore = KeyStore.getInstance(
                KeyStoreName, defaultProvider);

            keyStore.load(null, null);

            try {
                keyStore.deleteEntry(getAlicaAlias());
            } catch (Exception e) {
                // ignore
            }

            try {
                keyStore.deleteEntry(getBobAlias());
            } catch (Exception e) {
                // ignore
            }

        } // if

    }

}
