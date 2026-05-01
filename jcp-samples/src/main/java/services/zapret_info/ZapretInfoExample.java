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
package services.zapret_info;

import CMS_samples.CMStools;
import com.objsys.asn1j.runtime.*;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.*;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.CertificateSerialNumber;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Name;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Пример формирования запроса для выгрузки из реестра
 * http://zapret-info.gov.ru.
 * Инструкция находится по адресу http://zapret-info.gov.ru/docs/
 * description_for_operators_2012-11-09v1.4.pdf
 *
 * Класс ZapretInfoExample подписывает запрос {@link #REQUEST}
 * электронной цифровой подписью формата PKCS7.
 */
public class ZapretInfoExample {

    /**
     * Папка для сохранения файлов.
     */
    private static final String PATH_TO_SAVE = "C:\\";
    /**
     * Алиас ключа в ключевом контейнере.
     */
    private static final String ALIAS = "gost_exch";
    /**
     * Пароль для доступа к ключу в ключевом контейнере.
     */
    private static final char[] PASSWORD = "Pass1234".toCharArray();
    /**
     * Простейший запрос на получение выгрузки, взятый из инструкции.
     */
    private static final String REQUEST =
            "<?xml version=\"1.0\" encoding=\"windows-1251\"?>\n" +
            "<request>\n" +
            "<requestTime>2012-01-01T01:01:01.000+04:00</requestTime>\n" +
            "<operatorName>Наименование оператора</operatorName>\n" +
            "<inn>1234567890</inn>\n" +
            "<ogrn>1234567890123</ogrn>\n" +
            "<email>email@email.ru</email>\n" +
            "</request>";

    /**
     * Функция формирования простой отсоединенной подписи формата PKCS#7
     * по хешу сообщения.
     * Пример подписи взят из {@link CMS_samples.CMS#CMSSign(byte[],
     * PrivateKey, Certificate, boolean)}.
     *
     * @param data Данные для подписи.
     * @param privateKey Закрытый ключ для создания ЭЦП.
     * @param certificate Сертификат подписи.
     * @return ЭЦП.
     * @throws Exception
     */
    public static byte[] createPKCS7(byte[] data, PrivateKey privateKey,
        X509Certificate certificate) throws Exception {

        // Получаем бинарную подпись длиной 64 байта.

        final Signature signature = Signature.getInstance(JCP.GOST_DHEL_SIGN_NAME);
        signature.initSign(privateKey);
        signature.update(data);

        final byte[] sign = signature.sign();

        // Формируем контекст подписи формата PKCS7.

        final ContentInfo all = new ContentInfo();
        all.contentType = new Asn1ObjectIdentifier(
            new OID(CMStools.STR_CMS_OID_SIGNED).value);

        final SignedData cms = new SignedData();
        all.content = cms;
        cms.version = new CMSVersion(1);

        // Идентификатор алгоритма хеширования.

        cms.digestAlgorithms = new DigestAlgorithmIdentifiers(1);
        final DigestAlgorithmIdentifier a = new DigestAlgorithmIdentifier(
            new OID(CMStools.DIGEST_OID).value);
        a.parameters = new Asn1Null();
        cms.digestAlgorithms.elements[0] = a;

        // Т.к. подпись отсоединенная, то содержимое отсутствует.

        cms.encapContentInfo = new EncapsulatedContentInfo(
            new Asn1ObjectIdentifier(new OID(CMStools.STR_CMS_OID_DATA).value), null);

        // Добавляем сертификат подписи.

        cms.certificates = new CertificateSet(1);
        final ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate asnCertificate =
            new ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate();

        final Asn1BerDecodeBuffer decodeBuffer =
            new Asn1BerDecodeBuffer(certificate.getEncoded());
        asnCertificate.decode(decodeBuffer);

        cms.certificates.elements = new CertificateChoices[1];
        cms.certificates.elements[0] = new CertificateChoices();
        cms.certificates.elements[0].set_certificate(asnCertificate);

        // Добавялем информацию о подписанте.

        cms.signerInfos = new SignerInfos(1);
        cms.signerInfos.elements[0] = new SignerInfo();
        cms.signerInfos.elements[0].version = new CMSVersion(1);
        cms.signerInfos.elements[0].sid = new SignerIdentifier();

        final byte[] encodedName = certificate.getIssuerX500Principal().getEncoded();
        final Asn1BerDecodeBuffer nameBuf = new Asn1BerDecodeBuffer(encodedName);
        final Name name = new Name();
        name.decode(nameBuf);

        final CertificateSerialNumber num = new CertificateSerialNumber(
            certificate.getSerialNumber());

        cms.signerInfos.elements[0].sid.set_issuerAndSerialNumber(
            new IssuerAndSerialNumber(name, num));
        cms.signerInfos.elements[0].digestAlgorithm =
            new DigestAlgorithmIdentifier(new OID(CMStools.DIGEST_OID).value);
        cms.signerInfos.elements[0].digestAlgorithm.parameters = new Asn1Null();
        cms.signerInfos.elements[0].signatureAlgorithm =
            new SignatureAlgorithmIdentifier(new OID(CMStools.SIGN_OID).value);
        cms.signerInfos.elements[0].signatureAlgorithm.parameters = new Asn1Null();
        cms.signerInfos.elements[0].signature = new SignatureValue(sign);

        // Получаем закодированную подпись.

        final Asn1BerEncodeBuffer asnBuf = new Asn1BerEncodeBuffer();
        all.encode(asnBuf, true);

        return asnBuf.getMsgCopy();
    }

    /**
     * Функция декодирования подписи формата PKCS7.
     * Пример подписи взят из {@link CMS_samples.CMS#CMSVerify(byte[],
     * Certificate, byte[])}.
     *
     * @param pkcs7Signature ЭЦП формата PKCS7.
     * @param data Подписанные данные.
     * @param certificate Сертификат для проверки подписи.
     * @return True, если подпись корректна.
     * @throws Exception
     */
    public static boolean verifyPKCS7(byte[] pkcs7Signature, byte[] data,
        X509Certificate certificate) throws Exception {

        // Декодирование подписи формата PKCS7.

        int i = -1;
        final Asn1BerDecodeBuffer asnBuf = new Asn1BerDecodeBuffer(pkcs7Signature);
        final ContentInfo all = new ContentInfo();
        all.decode(asnBuf);

        // Проверка формата подписи.

        boolean supportedType =
            new OID(CMStools.STR_CMS_OID_SIGNED).eq(all.contentType.value);
        if (!supportedType) {
            throw new Exception("Not supported");
        }

        final SignedData cms = (SignedData) all.content;
        if (cms.version.value != 1) {
            throw new Exception("Incorrect version");
        }

        boolean supportedData = new OID(CMStools.STR_CMS_OID_DATA).eq(
            cms.encapContentInfo.eContentType.value);
        if (!supportedData) {
            throw new Exception("Nested not supported");
        }

        byte[] text = null;
        if (data != null) {
            text = data;
        } else if (cms.encapContentInfo.eContent != null) {
            text = cms.encapContentInfo.eContent.value;
        }

        if (text == null) {
            throw new Exception("No content");
        }

        // Получение идентификатора алгоритма хеширования.

        OID digestOid = null;
        DigestAlgorithmIdentifier a = new DigestAlgorithmIdentifier(
            new OID(CMStools.DIGEST_OID).value);

        for (i = 0; i < cms.digestAlgorithms.elements.length; i++) {

            if (cms.digestAlgorithms.elements[i].algorithm.equals(a.algorithm)) {
                digestOid = new OID(cms.digestAlgorithms.elements[i].algorithm.value);
                break;
            } // if

        } // for

        if (digestOid == null) {
            throw new Exception("Unknown digest");
        }

        // Поиск сертификат подписи.

        int pos = -1;
        for (i = 0; i < cms.certificates.elements.length; i++) {

            final Asn1BerEncodeBuffer encBuf = new Asn1BerEncodeBuffer();
            cms.certificates.elements[i].encode(encBuf);

            final byte[] in = encBuf.getMsgCopy();
            if (Arrays.equals(in, certificate.getEncoded())) {
                System.out.println("Selected certificate: " + certificate.getSubjectDN());
                pos = i;
                break;
            } // if

        } // for

        if (pos == -1) {
            throw new Exception("Not signed on certificate");
        }

        // Декодирование подписанта.

        final SignerInfo info = cms.signerInfos.elements[pos];
        if (info.version.value != 1) {
            throw new Exception("Incorrect version");
        }

        if (!digestOid.equals(new OID(info.digestAlgorithm.algorithm.value))) {
            throw new Exception("Not signed on certificate");
        }

        final byte[] sign = info.signature.value;

        // Проверка подписи.

        final Signature signature = Signature.getInstance(JCP.GOST_EL_SIGN_NAME);
        signature.initVerify(certificate);
        signature.update(text);

        return signature.verify(sign);
    }

    public static void main(String[] args) throws Exception {

        JCPInit.initProviders(false);

        // Инициализация ключевого контейнера, получение закрытого
        // ключа и сертификата.

        KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
        keyStore.load(null, null);

        PrivateKey privateKey = (PrivateKey)keyStore.getKey(ALIAS, PASSWORD);
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(ALIAS);

        // Формирование подписи PKCS7.

        byte[] pkcs7Sign = createPKCS7(REQUEST.getBytes(), privateKey, certificate);

        // Локальная проверка подписи PKCS7.

        boolean checkResult = verifyPKCS7(pkcs7Sign, REQUEST.getBytes(), certificate);

        if (checkResult) {
            System.out.println("Valid signature");
        } else {
            System.err.println("Invalid signature");
            return;
        }

        // Сохранение данных и подписи в файлы.

        Array.writeFile(PATH_TO_SAVE + "request.xml", REQUEST.getBytes());
        Array.writeFile(PATH_TO_SAVE + "pkcs7.p7s", pkcs7Sign);


    }
}
