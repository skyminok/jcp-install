/**
 * $RCSfileXAdES4JSignVerify.java,v $
 * version $Revision: 36379 $
 * created 09.09.2015 11:46 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <br>
 * Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package xades;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.XAdES.util.XMLUtils;

import xades.config.IXAdESConfig;
import xades.config.XAdESConfig;
import xades.provider.GostTimeStampTokenProvider;
import xades.provider.GostTimeStampVerificationProvider;
import xades.util.GostXAdESUtility;

import xades.util.XMLUtility;
import xades4j.UnsupportedAlgorithmException;
import xades4j.algorithms.*;
import xades4j.production.*;
import xades4j.properties.DataObjectDesc;
import xades4j.providers.CertificateValidationProvider;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.impl.DefaultAlgorithmsProviderEx;
import xades4j.providers.impl.DefaultMessageDigestProvider;
import xades4j.providers.impl.DirectKeyingDataProvider;
import xades4j.providers.impl.PKIXCertificateValidationProvider;
import xades4j.verification.XadesVerificationProfile;
import xades4j.verification.XadesVerifier;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.XMLSignature;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertStore;
import java.security.cert.X509Certificate;

import java.util.Map;

/**
 * Пример создания и проверки подписи формата XAdES-BES или
 * XAdES-T с помощью google xades4j.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XAdES4JSignVerify extends GostXAdESUtility {

    /**
     * Подписываемый узел.
     */
    public static final String XML_DOC_ID = "acct";



    /**
     * Класс для подписи сообщения и ее проверки в формате XAdES-T.
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Включаем возможность онлайновой проверки.
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        JCPInit.initProviders(false);

        String workDir = System.getProperty("user.dir");
        byte[] xmlData = XAdESExample.XML_DOC.getBytes("UTF-8");

        Algorithm[] algorithms = new Algorithm[]
                {
                        new EnvelopedSignatureTransform(),
                        new CanonicalXMLWithoutComments()
                };


        // Подпись
        Document signedDoc = sign(true,
                XAdESConfig.Default.CONFIG_2001_S,
                xmlData,
                workDir,
                "acct",
                algorithms
        );


        // Проверка подписи.

        verify( XAdESConfig.Default.CONFIG_2001_S,
                signedDoc,
                true);

    }


    /**
     * Создание подписи формата XAdES (BES или T).
     *
     * @param xAdEST True, если следует сформировать XAdES-T.
     * @param xAdESConfig Конфигурация контейнера.
     * @param sourceXmlBin Исходный подписываемый документ.
     * @param signingId Подписываемый узел.
     * @param workDir Рабочая папка.
     * @param transforms Список трансформаций.
     * @return документ с подписью.
     * @throws Exception
     */
    public static Document sign(boolean xAdEST, IXAdESConfig xAdESConfig,
        byte[] sourceXmlBin, String workDir, String signingId, Algorithm[]
        transforms) throws Exception {

        return sign(xAdEST, xAdESConfig, sourceXmlBin, signingId,
            GostXAdESUtility.MAP_DIGEST_OID_2_TSA_URL, workDir,
                transforms);

    }

    /**
     * Создание подписи формата XAdES (BES или T).
     *
     * @param xAdEST True, если следует сформировать XAdES-T.
     * @param xAdESConfig Конфигурация контейнера.
     * @param sourceXmlBin Исходный подписываемый документ.
     * @param signingId Подписываемый узел.
     * @param digest2TsaUrlMap Список пар "oid_алгоритма_хеширования=
     * адрес_tsp_службы". Задает список соответствий между алгоритмом
     * хеширования и адресом TSP службы, чтобы разнообразить и расширить
     * пример.
     * @param workDir Рабочая папка.
     * @param transforms Список трансформаций.
     * @return документ с подписью.
     * @throws Exception
     */
    public static Document sign(boolean xAdEST, IXAdESConfig xAdESConfig,
        byte[] sourceXmlBin, String signingId, Map<String, String> digest2TsaUrlMap,
        String workDir, Algorithm[] transforms) throws Exception {


        // Исходный документ.
        final Document sourceDocument = parseFile(sourceXmlBin);

        return sign(xAdEST, xAdESConfig, sourceDocument, signingId,
            digest2TsaUrlMap, workDir, transforms);
    }

    /**
     * Создание подписи формата XAdES (BES или T).
     *
     * @param xAdEST True, если следует сформировать XAdES-T.
     * @param xAdESConfig Конфигурация контейнера.
     * @param sourceDocument Исходный подписываемый документ.
     * @param signingId Подписываемый узел.
     * @param digest2TsaUrlMap Список пар "oid_алгоритма_хеширования=
     * адрес_tsp_службы". Задает список соответствий между алгоритмом
     * хеширования и адресом TSP службы, чтобы разнообразить и расширить
     * пример.
     * @param workDir Рабочая папка.
     * @param transforms Список трансформаций.
     * @return документ с подписью.
     * @throws Exception
     */
    public static Document sign(boolean xAdEST, IXAdESConfig xAdESConfig,
        Document sourceDocument, String signingId, Map<String, String>
        digest2TsaUrlMap, String workDir, Algorithm[] transforms) throws
        Exception {

        // 1. Документ и узел подписи.

        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();

        Node nodeToSign = null;
        String referenceURI = null;

        if (signingId != null) {

            // Подписываемый узел (предположительно, FinalPayment с неким Id).
            final XPathExpression expr = xpath.compile(String.format("//*[@Id='%s']", signingId));
            final NodeList nodes = (NodeList) expr.evaluate(sourceDocument, XPathConstants.NODESET);

            if (nodes.getLength() == 0) {
                throw new Exception("Can't find node with id: " + signingId);
            } // if

            nodeToSign = nodes.item(0);
            referenceURI = "#" + signingId;

        } // if
        else {
            nodeToSign = sourceDocument.getDocumentElement();
            referenceURI = "";
        } // else

        boolean envelopedFound = false;

        // Если в списке есть Enveloped, то берем родителя узла.
        if (transforms != null) {

            for (Algorithm transform : transforms) {
                if (transform.getUri().equals(CanonicalizationMethod.ENVELOPED)) {
                    envelopedFound = true;
                    break;
                }
            }

        }

        if (!envelopedFound && signingId != null) {
            nodeToSign = nodeToSign.getParentNode();
        }

        // 2. Ключ подписи и сертификат.

        final KeyStore keyStore = KeyStore.getInstance(
            xAdESConfig.getKeyStoreType(),
            xAdESConfig.getDefaultProvider()
        );

        keyStore.load(null, null);

        // Ключ подписи.
        final PrivateKey privateKey = (PrivateKey) keyStore.getKey(
            xAdESConfig.getSignatureContainer().getAlias(),
            xAdESConfig.getSignatureContainer().getPassword());

        // Сертификат для проверки.
        final X509Certificate cert = (X509Certificate) keyStore.getCertificate(
            xAdESConfig.getSignatureContainer().getAlias());

        // 3. Алгоритмы.

        final KeyingDataProvider keyingProvider = new DirectKeyingDataProvider(cert, privateKey);

        final XadesSigningProfile sigProf = xAdEST
            ? new XadesTSigningProfile(keyingProvider)
            : new XadesBesSigningProfile(keyingProvider);

        sigProf
                // time-stamp provider. Дополнительно задается список соответствий между
                // алгоритмом хеширования и адресом TSP службы, чтобы разнообразить и
                // расширить пример.
                .withTimeStampTokenProvider(new GostTimeStampTokenProvider(
                        digest2TsaUrlMap, xAdESConfig.getDefaultProvider()))

                        // digest provider
                .withDigestEngineProvider(new DefaultMessageDigestProvider() { // digest

                    @Override
                    public MessageDigest getEngine(String digestAlgorithmURI) throws UnsupportedAlgorithmException {

                        final String digestAlgOid = GostXAdESUtility.digestUri2Digest(digestAlgorithmURI);

                        try {
                            return MessageDigest.getInstance(digestAlgOid);
                        } catch (NoSuchAlgorithmException e) {
                            throw new UnsupportedAlgorithmException(e.getMessage(), digestAlgorithmURI, e);
                        }
                    }

                })

                .withAlgorithmsProviderEx(new DefaultAlgorithmsProviderEx() { // algorithms

                    private String digestUrn = null;

                    @Override
                    public Algorithm getSignatureAlgorithm(String keyAlgorithmName)
                            throws UnsupportedAlgorithmException {

                        digestUrn = GostXAdESUtility.key2DigestUrn(keyAlgorithmName);
                        final String signatureUrn = GostXAdESUtility.key2SignatureUrn(keyAlgorithmName);

                        return new GenericAlgorithm(signatureUrn);
                    }

                    @Override
                    public String getDigestAlgorithmForReferenceProperties() {
                        return digestUrn;
                    }

                    public String getDigestAlgorithmForDataObjsReferences() {
                        return digestUrn;
                    }

                    public String getDigestAlgorithmForTimeStampProperties() {
                        return digestUrn;
                    }

                    @Override
                    public Algorithm getCanonicalizationAlgorithmForSignature() {
                        return new ExclusiveCanonicalXMLWithoutComments();
                    }

                    @Override
                    public Algorithm getCanonicalizationAlgorithmForTimeStampProperties() {
                        return new ExclusiveCanonicalXMLWithoutComments();
                    }

                });

        // 4. Подпись.

        final XadesSigner signer = sigProf.newSigner();
        final DataObjectDesc dataObj = new DataObjectReference(referenceURI);

        if (transforms != null) {
            for (Algorithm transform : transforms) {
                dataObj.withTransform(transform);
            }
        }

        final SignedDataObjects dataObjects = new SignedDataObjects(dataObj);

        signer.sign(dataObjects, nodeToSign);
        System.out.println("XAdES4J signature completed.");
        // Лог.
        if (workDir != null) {
            XMLUtils.saveXml2File(sourceDocument,
                    workDir + "/xades4j.xml", false);
        }

        return sourceDocument;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Проверка подписи формата XAdES со штампом времени.
     *
     * @param xAdESConfig Конфигурация контейнера.
     * @param document Исходный подписываемый документ.
     * @param enableOnlineRevocationCheck True, если следует выполнить
     * online-проверку цепочки сертификатов.
     * @throws Exception
     */
    public static void verify(IXAdESConfig xAdESConfig, Document document,
        boolean enableOnlineRevocationCheck) throws Exception {

        verify(xAdESConfig, document, TRUST_STORE, TRUST_PASSWORD,
            null, enableOnlineRevocationCheck);

    }

    /**
     * Проверка подписи формата XAdES со штампом времени.
     *
     * @param xAdESConfig Конфигурация контейнера.
     * @param sourceXmlBin Исходный подписываемый документ.
     * @param enableOnlineRevocationCheck True, если следует выполнить
     * online-проверку цепочки сертификатов.
     * @throws Exception
     */
    public static void verify(IXAdESConfig xAdESConfig, byte[] sourceXmlBin,
        boolean enableOnlineRevocationCheck) throws Exception {

        verify(xAdESConfig, sourceXmlBin, TRUST_STORE, TRUST_PASSWORD,
            null, enableOnlineRevocationCheck);

    }

    /**
     * Проверка подписи формата XAdES со штампом времени.
     *
     * @param xAdESConfig Конфигурация контейнера.
     * @param sourceXmlBin Исходный подписываемый документ.
     * @param trustStorePath Путь к файлу хранилища корневых сертификатов.
     * @param trustStorePassword Пароль к хранилищу корневых сертификатов.
     * @param intermediateCertsAndCRLs Список дополнительных сертификатов
     * и CRL.
     * @param enableOnlineRevocationCheck True, если следует выполнить
     * online-проверку цепочки сертификатов.
     * @throws Exception
     */
    public static void verify(IXAdESConfig xAdESConfig, byte[] sourceXmlBin,
        String trustStorePath, char[] trustStorePassword, CertStore
        intermediateCertsAndCRLs, boolean enableOnlineRevocationCheck)
        throws Exception {

        // Исходный документ.
        final Document document = parseFile(sourceXmlBin);

        verify(xAdESConfig, document, trustStorePath, trustStorePassword,
            intermediateCertsAndCRLs, enableOnlineRevocationCheck);

    }

    /**
     * Проверка подписи формата XAdES со штампом времени.
     *
     * @param xAdESConfig Конфигурация контейнера.
     * @param document Исходный подписываемый документ.
     * @param trustStorePath Путь к файлу хранилища корневых сертификатов.
     * @param trustStorePassword Пароль к хранилищу корневых сертификатов.
     * @param intermediateCertsAndCRLs Список дополнительных сертификатов
     * и CRL.
     * @param enableOnlineRevocationCheck True, если следует выполнить
     * online-проверку цепочки сертификатов.
     * @throws Exception
     */
    public static void verify(IXAdESConfig xAdESConfig, Document document,
        String trustStorePath, char[] trustStorePassword, CertStore
        intermediateCertsAndCRLs, boolean enableOnlineRevocationCheck)
            throws Exception {

        final NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");

        if (nl.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        } // if

        // 2. Сертификаты.

        // Хранилище корневых сертификатов.
        final KeyStore trustStore = GostXAdESUtility.loadCertStore(
                trustStorePath, trustStorePassword);

        // Построение и проверка цепочки. Проверка всегда включена,
        // используем либо online проверку, либо файлы CRL.
        final CertificateValidationProvider validationProvider =
            // для отладки
            //(intermediateCertsAndCRLs != null)
            //    ? new GostPKIXCertificateValidationProvider(trustStore, enableOnlineRevocationCheck, "RevCheck", xAdESConfig.getDefaultProvider(), intermediateCertsAndCRLs)
            //    : new GostPKIXCertificateValidationProvider(trustStore, enableOnlineRevocationCheck, "RevCheck", xAdESConfig.getDefaultProvider());
            (intermediateCertsAndCRLs != null)
            ?  new PKIXCertificateValidationProvider(trustStore, enableOnlineRevocationCheck, intermediateCertsAndCRLs)
            :  new PKIXCertificateValidationProvider(trustStore, enableOnlineRevocationCheck);

        final XadesVerificationProfile verProf = new XadesVerificationProfile(validationProvider)

                // time-stamp validation
                .withTimeStampTokenVerifier(new GostTimeStampVerificationProvider(
                        validationProvider, xAdESConfig.getDefaultProvider()))

                        // digest
                .withDigestEngineProvider(new DefaultMessageDigestProvider() {

                    @Override
                    public MessageDigest getEngine(String digestAlgorithmURI) throws UnsupportedAlgorithmException {

                        final String digestAlgOid = GostXAdESUtility.digestUri2Digest(digestAlgorithmURI);

                        try {
                            return MessageDigest.getInstance(digestAlgOid);
                        } catch (NoSuchAlgorithmException e) {
                            throw new UnsupportedAlgorithmException(e.getMessage(), digestAlgorithmURI, e);
                        }

                    }

                });

        // 3. Проверка подписи.

        for (int i = 0; i < nl.getLength(); i++) {

            final XadesVerifier verifier = verProf.newVerifier();
            final Element signatureElement = (Element) nl.item(i);

            verifier.verify(signatureElement, null);
            System.out.println("XAdES4J verification completed.");

        }

    }

}
