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
package JCSP.ECDSA;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.ContainerStore;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.ECDSAParamsSpec;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.spec.NameAlgIdSpecForeign;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.JCSPECDSA;
import ru.CryptoPro.JCSP.params.PasswordParamsSpec;

import java.io.ByteArrayInputStream;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;

/**
 * В данном примере приводятся различные способы
 * генерации ключевой пары ECDSA/ECDH.
 */
public class ECDSAGenKeyExample {

    //Тип хранилища для записи ключей
    static String STORETYPE = JCSP.HD_STORE_NAME;//"HSMDB";

    /**
     * Первый способ генерации ключей ECDSA/ECDH заданной длины.
     * Длина ключа задается числом.
     * В примере осуществляется генерация ключевой пары, генерация
     * самоподписанного сертификата, сохранение ключа и сертификата в контейнер.
     * @param keyLen длина ключа
     * @param alias Алиас ключа.
     * @param password Пароль.
     * @param exchange True, если ключ обмена.
     * @param dn Субъект сертификата.
     * @param signAlg Алгоритм подписи.
     * @param keyUsage Назначение ключа.
     @throws Exception
     */
    public static void generateWithKeyLen( String alias, char[] password, boolean exchange,
                                           int keyLen,  String dn, String signAlg, int keyUsage) throws Exception{

        // Пробуем удалить контейнер на случай, если такой есть.

        try {
            deleteContainer(alias, password);
        } catch (Exception e) {
            // ignore
        }

        // Генерация ключевой пары заданной длины.

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(exchange ? JCP.ECDH_NAME : JCP.ECDSA_NAME, JCSPECDSA.PROVIDER_NAME);
        kpg.initialize(keyLen);

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
     * Первый способ генерации ключей ECDSA/ECDH заданной длины.
     * Длина ключа определяется по имени кривой.
     * В примере осуществляется генерация ключевой пары, генерация
     * самоподписанного сертификата, сохранение ключа и сертификата в контейнер.
     * @param paramName имя кривой
     * @param alias Алиас ключа.
     * @param password Пароль.
     * @param exchange True, если ключ обмена.
     * @param dn Субъект сертификата.
     * @param signAlg Алгоритм подписи.
     * @param keyUsage Назначение ключа.
     @throws Exception
     */
    public static void generateWithParamName( String alias, char[] password, boolean exchange,
                                           String paramName,  String dn, String signAlg, int keyUsage) throws Exception{

        // Пробуем удалить контейнер на случай, если такой есть.

        try {
            deleteContainer(alias, password);
        } catch (Exception e) {
            // ignore
        }

        // Генерация ключевой пары заданной длины.

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(exchange ? JCP.ECDH_NAME : JCP.ECDSA_NAME, JCSPECDSA.PROVIDER_NAME);
        ECGenParameterSpec spec = new ECGenParameterSpec(paramName);
        kpg.initialize(spec);

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
     * Третий способ генерации ключей ECDSA/ECDH заданной длины.
     * Длина ключа задается числом.
     * В примере осуществляется генерация ключевой пары, генерация
     * самоподписанного сертификата, сохранение ключа и сертификата в контейнер.
     * @param keyLen длина ключа
     * @param alias Алиас ключа.
     * @param password Пароль.
     * @param exchange True, если ключ обмена.
     * @param dn Субъект сертификата.
     * @param signAlg Алгоритм подписи.
     * @param keyUsage Назначение ключа.
     @throws Exception
     */
    public static void generateWithECDSAParamsSpec( String alias, char[] password, boolean exchange,
                                              int keyLen,  String dn, String signAlg, int keyUsage) throws Exception{

        // Пробуем удалить контейнер на случай, если такой есть.

        try {
            deleteContainer(alias, password);
        } catch (Exception e) {
            // ignore
        }

        // Генерация ключевой пары заданной длины.

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(exchange ? JCP.ECDH_NAME : JCP.ECDSA_NAME, JCSPECDSA.PROVIDER_NAME);
        OID ecdsaOID = null;
        switch (keyLen){
            case 192:
                ecdsaOID = ECDSAParamsSpec.OID_ECDSA_P192;
                break;
            case 224:
                ecdsaOID = ECDSAParamsSpec.OID_ECDSA_P224;
                break;
            case 256:
                ecdsaOID = ECDSAParamsSpec.OID_ECDSA_P256;
                break;
            case 384:
                ecdsaOID = ECDSAParamsSpec.OID_ECDSA_P384;
                break;
            case 521:
                ecdsaOID = ECDSAParamsSpec.OID_ECDSA_P521;
                break;
            default:
                throw new Exception("Invalid key length");
        }
        kpg.initialize(ECDSAParamsSpec.getInstance(ecdsaOID));

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
                STORETYPE, JCSPECDSA.PROVIDER_NAME);

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

        GostCertificateRequest request = new GostCertificateRequest(JCSPECDSA.PROVIDER_NAME);
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

        KeyStore keyStore = KeyStore.getInstance(STORETYPE, JCSPECDSA.PROVIDER_NAME);
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

        //Генерация и сохранение ключа ECDSA с параметрами кривой secp192r1
        generateWithKeyLen("secp192r1key",
                "1".toCharArray(),
                false,
                192,
                "CN=secp192r1key",
                JCP.SIGN_SHA256_ECDSA_NAME,
                GostCertificateRequest.SIGN_DEFAULT);

        //Генерация и сохранение ключа ECDH с параметрами кривой secp256r1
        generateWithKeyLen("secp256r1key",
                "1".toCharArray(),
                true,
                256,
                "CN=secp256r1key",
                JCP.SIGN_SHA256_ECDSA_NAME,
                GostCertificateRequest.CRYPT_DEFAULT);

        //Генерация и сохранение ключа ECDSA с параметрами кривой secp224r1
        generateWithParamName("secp224r1key",
                "1".toCharArray(),
                false,
                ECDSAParamsSpec.ECDSA_P224_NAME,
                "CN=secp224r1key",
                JCP.SIGN_SHA256_ECDSA_NAME,
                GostCertificateRequest.SIGN_DEFAULT);

        //Генерация и сохранение ключа ECDH с параметрами кривой secp384r1
        generateWithParamName("secp384r1key",
                "1".toCharArray(),
                true,
                ECDSAParamsSpec.ECDSA_P384_NAME,
                "CN=secp384r1key",
                JCP.SIGN_SHA256_ECDSA_NAME,
                GostCertificateRequest.CRYPT_DEFAULT);

        //Генерация и сохранение ключа ECDSA с параметрами кривой secp521r1
        generateWithECDSAParamsSpec("secp521r1key",
                "1".toCharArray(),
                false,
                521,
                "CN=secp521r1key",
                JCP.SIGN_SHA256_ECDSA_NAME,
                GostCertificateRequest.SIGN_DEFAULT);

    }

}
