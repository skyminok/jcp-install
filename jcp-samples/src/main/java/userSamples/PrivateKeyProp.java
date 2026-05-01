/**
 * PrivateKeyProp.java,v $
 * version $
 * created 14.11.2020 11:38 by afevma
 * last modified $ by $
 * (C) ООО Крипто-Про 2004-2020.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import com.objsys.asn1j.runtime.Asn1ObjectIdentifier;

import ru.CryptoPro.JCP.ASN.CertificateExtensions.PrivateKeyUsagePeriod;
import ru.CryptoPro.JCP.ASN.Gost_CryptoPro_PrivateKey._Gost_CryptoPro_PrivateKeyValues;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Extension;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Key.PrivateKeyInterface;
import ru.CryptoPro.JCP.Key.SpecKey;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.tools.PKUPDecoder;

import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;

/**
 * Абстрактный класс, позволяющий получить
 * некоторые свойства закрытого ключа.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public abstract class PrivateKeyProp {

    /**
     * Тип ключа подписи.
     */
    public static final int SPEC_SIGNATURE = 2;

    /**
     * Тип ключа подписи и обмена.
     */
    public static final int SPEC_KEY_EXCHANGE = 1;

    /**
     * Класс свойств ключа.
     */
    public static class KeyInfo {

        public int keyLength; // длина ключа в битах
        public boolean isExportable; // экспортируемость ключа
        public boolean isRSA; // true, если RSA

        public Calendar notBeforeFromKey; // срок начала действия (из контейнера)
        public Calendar notAfterFromKey; // срок окончания действия (из контейнера)
        public boolean match; // true, если закрытый и открытый ключ соответствует друг другу

        public Calendar notBeforeFromCert; // срок начала действия (из сертификата)
        public Calendar notAfterFromCert; // срок окончания действия (из сертификата)

    }

    /**
     * Хранилище ключей.
     */
    private KeyStore keyStore;

    /**
     * Загрузка хранилища ключей.
     *
     * @throws Exception
     */
    public void load() throws Exception {

        keyStore = KeyStore.getInstance(
            getKeyStoreType(),
            getProviderName()
        );

        keyStore.load(null, null);
    }

    /**
     * Получение имени хранилища.
     *
     * @return имя хранилища.
     */
    public abstract String getKeyStoreType();

    /**
     * Получение имени ГОСТ провайдера.
     *
     * @return имя провайдера.
     */
    public abstract String getProviderName();

    /**
     * Получение некоторых свойств закрытого ключа.
     *
     * @param privateKey закрытый ключ.
     * @param isExchange true, если ключ обмена.
     * @param cert сертификат ключа.
     * @return свойства ключа.
     * @throws Exception
     */
    private KeyInfo getKeyInfo(PrivateKey privateKey,
        boolean isExchange, X509Certificate cert)
        throws Exception {

        KeyInfo keyInfo = new KeyInfo();
        String keyAlgorithm = privateKey.getAlgorithm();

        // Длина ключа.

        if (privateKey instanceof SpecKey) {

            SpecKey specKey   = (SpecKey) privateKey;
            keyInfo.keyLength = specKey.getKeySize();

        } // if

        keyInfo.isRSA =
            keyAlgorithm.equalsIgnoreCase(JCP.RSA_NAME) ||
            keyAlgorithm.equalsIgnoreCase(JCP.CP_RSA_NAME);

        // Разные свойства ключа.
        //
        // В случае ключа RSA надо явно передать наше имя алгоритма
        // CP_RSA, а не RSA, т.к. RSA алгоритмы в провайдере Java CSP
        // RSA, а он специально не регистрирует KeyFactory для RSA,
        // чтобы не мешать дефолтным KeyFactory типа Sun.

        KeyFactory kf = KeyFactory.getInstance(
            keyInfo.isRSA ? JCP.CP_RSA_NAME : keyAlgorithm,
            getProviderName()
        );

        PrivateKeyInterface pki = kf.getKeySpec(privateKey,
            PrivateKeyInterface.class);

        if (pki != null) {

            // Экспортируемость.

            keyInfo.isExportable = pki.isExportable();

            // Срок действия.

            final Extension keyUsagePeriodExtension = isExchange
                ? pki.getExtension(new Asn1ObjectIdentifier(_Gost_CryptoPro_PrivateKeyValues.id_CryptoPro_private_keys_extension_exchange_key_usage_period))
                : pki.getExtension(new Asn1ObjectIdentifier(_Gost_CryptoPro_PrivateKeyValues.id_CryptoPro_private_keys_extension_signature_key_usage_period));

            if (keyUsagePeriodExtension != null) {

                PrivateKeyUsagePeriod keyUsagePeriod = new PrivateKeyUsagePeriod();
                if (PKUPDecoder.decodeExtension(keyUsagePeriodExtension, keyUsagePeriod)) {

                    try {

                        keyInfo.notBeforeFromKey = keyUsagePeriod.notBefore != null
                            ? keyUsagePeriod.notBefore.getTime()
                            : null;

                    } catch (Exception e) {}

                    try {

                        keyInfo.notAfterFromKey = keyUsagePeriod.notAfter != null
                            ? keyUsagePeriod.notAfter.getTime()
                            : null;

                    } catch (Exception e) {}

                } // if

            } // if

            if (cert != null) {

                // Проверка на соответствие закрытого ключа
                // и открытого ключа.

                keyInfo.match = pki.match(cert.getPublicKey(), getProviderName());

                // Получаем срок действия ключа из сертификата.

                PrivateKeyUsagePeriod certificatePrivateKeyUsagePeriod
                    = new PrivateKeyUsagePeriod();

                boolean certificatePrivateKeyUsagePeriodDecoded =
                    PKUPDecoder.decodeExtension(cert,
                        certificatePrivateKeyUsagePeriod);

                if (certificatePrivateKeyUsagePeriodDecoded) {

                    try {

                        keyInfo.notBeforeFromCert = certificatePrivateKeyUsagePeriod.notBefore != null
                            ? certificatePrivateKeyUsagePeriod.notBefore.getTime()
                            : null;

                    } catch (Exception e) {}

                    try {

                        keyInfo.notAfterFromCert = certificatePrivateKeyUsagePeriod.notAfter != null
                            ? certificatePrivateKeyUsagePeriod.notAfter.getTime()
                            : null;

                    } catch (Exception e) {}

                } // if

            } // if

        } // if

        return keyInfo;

    }

    /**
     * Получение некоторых свойств закрытого ключа
     * их хранилища.
     *
     * @param alias Алиас ключа.
     * @param password Пароль к ключу.
     * @param keySpec Тип ключа.
     * @return свойства ключа.
     */
    public KeyInfo getKeyInfo(String alias, char[] password, int keySpec) {

        System.out.println("%%% ALIAS: " + alias + " %%%");

        JCPProtectionParameter parameter = new JCPProtectionParameter(
            password, true, true, keySpec);

        try {

            JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry)
                keyStore.getEntry(alias, parameter);

            PrivateKey privateKey = entry.getPrivateKey();
            X509Certificate cert  = (X509Certificate) entry.getCertificate();

            switch (keySpec) {
                case SPEC_SIGNATURE:    System.out.println("** Signature key exists."); break;
                case SPEC_KEY_EXCHANGE: System.out.println("** Exchange key exists.");  break;
            } // switch

            KeyInfo keyInfo = getKeyInfo(privateKey, keySpec == SPEC_KEY_EXCHANGE, cert);

            System.out.println("\tKey algorithm: " +     privateKey.getAlgorithm());
            System.out.println("\tKey length: " +        keyInfo.keyLength);
            System.out.println("\tKey is exportable: " + keyInfo.isExportable);
            System.out.println("\tKey is RSA: " +        keyInfo.isRSA);

            if (keyInfo.notBeforeFromKey != null) {
                System.out.println("\tUse key not before: " +
                    keyInfo.notBeforeFromKey.getTime());
            } // if

            if (keyInfo.notAfterFromKey != null) {
                System.out.println("\tUse key not after: " +
                    keyInfo.notAfterFromKey.getTime());
            } // if

            if (cert != null) {

                System.out.println("\tCertificate exists. Subject: "
                    + cert.getSubjectDN() + ". It matches the key: "
                        + keyInfo.match);

                if (keyInfo.notBeforeFromCert != null) {
                    System.out.println("\tUse key not before (from certificate): " +
                        keyInfo.notBeforeFromCert.getTime());
                } // if

                if (keyInfo.notAfterFromCert != null) {
                    System.out.println("\tUse key not after (from certificate): " +
                        keyInfo.notAfterFromCert.getTime());
                } // if

            } // if

            return keyInfo;

        } catch (Exception e) {

            switch (keySpec) {
                case SPEC_SIGNATURE:    System.out.println("** Signature key does not exist."); break;
                case SPEC_KEY_EXCHANGE: System.out.println("** Exchange key does not exist.");  break;
            } // switch

        }

        return null;

    }

}

