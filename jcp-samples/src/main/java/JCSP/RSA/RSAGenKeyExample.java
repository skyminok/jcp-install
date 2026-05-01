package JCSP.RSA;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.ContainerStore;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.spec.NameAlgIdSpecForeign;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.JCSPRSA;
import ru.CryptoPro.JCSP.params.PasswordParamsSpec;
import ru.CryptoPro.JCSP.params.RSAExchangeKeySpec;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Данный пример демонстрирует генерацию и сохранение ключей RSA.
 */
public class RSAGenKeyExample {

    //Тип хранилища для записи ключей
    static String STORETYPE = JCSP.HD_STORE_NAME;//"HSMDB";

    /**
     * Генерация ключей RSA.
     * В примере осуществляется генерация ключевой пары, генерация
     * самоподписанного сертификата, сохранение ключа и сертификата в контейнер.
     * @param alias Алиас ключа.
     * @param password Пароль.
     * @param keyLen Длина ключа
     * @param isKeyExch Необходимо сгенерить ключ обмена
     * @param dn Субъект сертификата.
     * @param signAlg Алгоритм подписи.
     * @param keyUsage Назначение ключа.
     @throws Exception
     */
    public static void generateKey( String alias, char[] password, int keyLen, boolean isKeyExch,
                                    String dn, String signAlg, int keyUsage) throws Exception{

        // Пробуем удалить контейнер на случай, если такой есть.

        try {
            deleteContainer(alias, password);
        } catch (Exception e) {
            // ignore
        }

        // Генерация ключевой пары заданной длины.

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(JCP.RSA_NAME, JCSPRSA.PROVIDER_NAME);
        kpg.initialize(keyLen);

        // В некоторых случаях необходимо генератару передавать полный путь к контейнеру (например, в случае работы с HSM).
        NameAlgIdSpecForeign spec1 = new NameAlgIdSpecForeign("\\\\.\\" + STORETYPE + "\\" + alias);
        kpg.initialize(spec1);
        PasswordParamsSpec spec2 = new PasswordParamsSpec(password);
        kpg.initialize(spec2);

        // Если необходимо сгенерить ключ обмена.
        if (isKeyExch) {
            RSAExchangeKeySpec spec3 = new RSAExchangeKeySpec();
            kpg.initialize(spec3);
        } // if

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
                STORETYPE, JCSPRSA.PROVIDER_NAME);

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

        GostCertificateRequest request = new GostCertificateRequest(JCSPRSA.PROVIDER_NAME);
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

        KeyStore keyStore = KeyStore.getInstance(STORETYPE, JCSPRSA.PROVIDER_NAME);
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

        //Генерация и сохранение ключа подписи RSA длиной 4096
        generateKey("rsasignkey",
                "1".toCharArray(),
                4096,
                false,
                "CN=rsasignkey",
                JCP.SIGN_SHA1_RSA_NAME,
                GostCertificateRequest.SIGN_DEFAULT);

        //Генерация и сохранение ключа обмена RSA длиной 2048
        generateKey("rsaexchkey",
                "1".toCharArray(),
                2048,
                true,
                "CN=rsaexchkey",
                JCP.SIGN_SHA1_RSA_NAME,
                GostCertificateRequest.SIGN_DEFAULT);

    }


}

