/**
 * $RCSfileXAdESExample.java,v $
 * version $Revision: 36379 $
 * created 03.06.2015 15:17 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 *
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

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;

import ru.CryptoPro.XAdES.util.XMLUtils;

import xades.config.IXAdESConfig;
import xades.config.XAdESConfig;

import xades.provider.GostTimeStampTokenProvider;
import xades.provider.GostTimeStampVerificationProvider;
import xades.util.*;

import xades4j.UnsupportedAlgorithmException;
import xades4j.algorithms.*;
import xades4j.production.*;
import xades4j.properties.DataObjectDesc;

import xades4j.providers.*;
import xades4j.providers.impl.*;

import xades4j.verification.XadesVerificationProfile;
import xades4j.verification.XadesVerifier;

import javax.xml.crypto.dsig.XMLSignature;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.security.*;
import java.security.cert.*;

import java.util.*;

/**
 * Пример создания подписи XAdES-T (со штампом времени)
 * и ее проверки.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XAdESExample extends GostXAdESUtility {

    /**
     * Пример документа. Подписываться будет
     * узел с id="acct".
     */
    public static final String XML_DOC =
            "<?xml version=\"1.0\"?>\n" +
                    "<PatientRecord>    \n" +
                    "    <Name>John Doe</Name>    \n" +
                    "    <Account Id=\"acct\">123456</Account>    \n" +
                    "    <BankInfo Id=\"bank\">ХомБанк</BankInfo>    \n" +
                    "    <Visit date=\"10pm March 10, 2002\">    \n" +
                    "        <Diagnosis>Сообщение</Diagnosis>    \n" +
                    "    </Visit>\n" +
                    "</PatientRecord>";

    /**
     * Подписываемый узел.
     */
    public static final String XML_DOC_ID = "acct";

    /**
     * Запуск примеров.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        JCPInit.initProviders(false);

        // ГОСТ Р 34.10-2001.
        // signAndVerifyExample(XAdESConfig.CONFIG_2001_S);

        // ГОСТ Р 34.10-2012 (256).
        signAndVerifyExample(XAdESConfig.Default.CONFIG_2012_S);

        // ГОСТ Р 34.10-2012 (512).
        signAndVerifyExample(XAdESConfig.Default.CONFIG_2012_L);

    }

    /**
     * Создание и проверка подписи XAdES. Цепочка сертификатов
     * проверяется online.
     *
     * @param xAdESConfig Конфигурация контейнера.
     * @throws Exception
     */
    private static void signAndVerifyExample(IXAdESConfig xAdESConfig)
        throws Exception {

        signAndVerify(xAdESConfig, XML_DOC.getBytes("UTF-8"), XML_DOC_ID,
            GostXAdESUtility.MAP_DIGEST_OID_2_TSA_URL, WORK_DIR,
                TRUST_STORE, TRUST_PASSWORD, Collections.emptyList(), true);

    }

    /**
     * Создание и проверка подписи формата XAdES
     * со штампом времени.
     *
     * @param xAdESConfig Конфигурация контейнера.
     * @param workDir Рабочая папка.
     * @param sourceXmlBin Исходный подписываемый документ.
     * @param signingId Подписываемый узел.
     * @return документ с подписью.
     * @throws Exception
     */
    public static Document signXAdES_T(IXAdESConfig xAdESConfig,
        String workDir, byte[] sourceXmlBin, String signingId) throws Exception {

        return signAndVerify(xAdESConfig, sourceXmlBin, signingId,
            GostXAdESUtility.MAP_DIGEST_OID_2_TSA_URL, workDir, TRUST_STORE,
                TRUST_PASSWORD, Collections.emptyList(), true);

    }

    /**
     * Создание и проверка подписи формата XAdES
     * со штампом времени.
     *
     * @param xAdESConfig Конфигурация контейнера.
     * @param sourceXmlBin Исходный подписываемый документ.
     * @param signingId Подписываемый узел.
     * @param digest2TsaUrlMap Список пар "oid_алгоритма_хеширования=
     * адрес_tsp_службы". Задает список соответствий между алгоритмом
     * хеширования и адресом TSP службы, чтобы разнообразить и расширить
     * пример.
     * @param workDir Рабочая папка.
     * @param trustStorePath Путь к файлу хранилища корневых сертификатов.
     * @param trustStorePassword Пароль к хранилищу корневых сертификатов.
     * @param intermediateCertsAndCRLs Список дополнительных сертификатов
     * и CRL.
     * @param enableOnlineRevocationCheck True, если следует выполнить
     * online-проверку цепочки сертификатов.
     * @return документ с подписью.
     * @throws Exception
     */
    public static Document signAndVerify(IXAdESConfig xAdESConfig,
        byte[] sourceXmlBin, String signingId, Map<String, String>
        digest2TsaUrlMap, String workDir, String trustStorePath, char[]
        trustStorePassword, Collection intermediateCertsAndCRLs, boolean
        enableOnlineRevocationCheck) throws Exception {

        // Включаем возможность онлайновой проверки.
        System.setProperty("com.sun.security.enableCRLDP", Boolean.toString(enableOnlineRevocationCheck));
        System.setProperty("com.ibm.security.enableCRLDP", Boolean.toString(enableOnlineRevocationCheck));

        //************************************ Подпись ************************************

        // 1. Документ и узел подписи.

        // Исходный документ.
        final Document sourceDocument = parseFile(sourceXmlBin);

        final XPathFactory factory = XPathFactory.newInstance();
        final XPath xpath = factory.newXPath();

        // Подписываемый узел (предположительно, FinalPayment с неким Id).
        final XPathExpression expr = xpath.compile(String.format("//*[@Id='%s']", signingId));
        final NodeList nodes = (NodeList) expr.evaluate(sourceDocument, XPathConstants.NODESET);

        if (nodes.getLength() == 0) {
            throw new Exception("Can't find node with id: " + signingId);
        } // if

        final Node nodeToSign = nodes.item(0);
        // final Node sigParent = nodeToSign.getParentNode();
        final String referenceURI = "#" + signingId;

        // 2. Ключ подписи и сертификат.

        final KeyStore keyStore = KeyStore.getInstance(
            xAdESConfig.getKeyStoreType(), xAdESConfig.getDefaultProvider());

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
        final XadesSigningProfile sigProf = new XadesTSigningProfile(keyingProvider)

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
        dataObj.withTransform(new EnvelopedSignatureTransform());
        // dataObj.withTransform(new ExclusiveCanonicalXMLWithoutComments());

        final SignedDataObjects dataObjects = new SignedDataObjects(dataObj);

        signer.sign(dataObjects, nodeToSign);
        System.out.println("XAdES-T signature completed.");

        XMLUtils.writeXML(System.out, sourceDocument);

        //************************************ Проверка ************************************

        // 1. Подписанный документ.

        final Document verifyDocument = sourceDocument;

        // Узел с подписью (предположительно, один).
        final NodeList nl =
            verifyDocument.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");

        if (nl.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        } // if

        // 2. Сертификаты.

        // Хранилище корневых сертификатов.
        final KeyStore trustStore = GostXAdESUtility.
            loadCertStore(trustStorePath, trustStorePassword);

        final CertStore intermediateCertsAndCRLStore = CertStore.getInstance("Collection",
            new CollectionCertStoreParameters(intermediateCertsAndCRLs));

        // Построение и проверка цепочки. Проверка всегда включена,
        // используем либо online проверку, либо файлы CRL.
        final CertificateValidationProvider validationProvider =
             // для отладки
             // new GostPKIXCertificateValidationProvider(trustStore, true, "RevCheck",
             //   xAdESConfig.getDefaultProvider(), intermediateCertsAndCRLStore);
             new PKIXCertificateValidationProvider(trustStore, true, intermediateCertsAndCRLStore);

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

        final XadesVerifier verifier = verProf.newVerifier();
        final Element signatureElement = (Element) nl.item(0); // предположительно, один узел с подписью

        verifier.verify(signatureElement, null);
        System.out.println("Validation of XAdES-T completed.");

        return verifyDocument;

    }

    /**
     * Проверка подписи.
     *
     * @param document Проверяемый документ.
     * @throws Exception
     */
    public static void verify(Document document) throws Exception {

        // Узел с подписью (предположительно, один).
        final NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");

        if (nl.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        } // if

        // 2. Сертификаты.

        // Хранилище корневых сертификатов.
        final KeyStore trustStore = GostXAdESUtility.
            loadCertStore(TRUST_STORE, TRUST_PASSWORD);

        final CertStore intermediateCertsAndCRLStore = CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(Collections.emptyList()));

        // Построение и проверка цепочки. Проверка всегда включена,
        // используем либо online проверку, либо файлы CRL.
        final CertificateValidationProvider validationProvider =
                // для отладки
                // new GostPKIXCertificateValidationProvider(trustStore, true, "RevCheck",
                //   xAdESConfig.getDefaultProvider(), intermediateCertsAndCRLStore);
                new PKIXCertificateValidationProvider(trustStore, false, intermediateCertsAndCRLStore);

        final XadesVerificationProfile verProf = new XadesVerificationProfile(validationProvider)

                // time-stamp validation
                .withTimeStampTokenVerifier(new GostTimeStampVerificationProvider(
                        validationProvider, JCP.PROVIDER_NAME))

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

        final XadesVerifier verifier = verProf.newVerifier();
        final Element signatureElement = (Element) nl.item(0); // предположительно, один узел с подписью

        verifier.verify(signatureElement, null);
        System.out.println("Validation of XAdES-T completed.");

    }

}

