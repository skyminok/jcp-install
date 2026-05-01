/**
 * $RCSfileXAdESSignVerify.java,v $
 * version $Revision: 36379 $
 * created 09.09.2015 9:34 by afevma
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

import org.bouncycastle.tsp.TimeStampToken;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;

import ru.CryptoPro.XAdES.*;
import ru.CryptoPro.XAdES.DataObjects;
import ru.CryptoPro.XAdES.transform.EnvelopedTransform;
import ru.CryptoPro.XAdES.transform.ITransform;
import util.ResolveProvider;
import ru.CryptoPro.XAdES.util.*;
import ru.CryptoPro.JCP.Util.JCPInit;
import xades.config.IXAdESConfig;
import xades.config.XAdESConfig;
import xades.util.GostXAdESUtility;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;

import java.util.*;

/**
 * Класс для подписи сообщения и ее проверки в формате XAdES-T.
 *
 * @author Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XAdESSignVerify extends GostXAdESUtility {

    /**
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Включаем возможность онлайновой проверки.
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        JCPInit.initProviders(false);

        byte[] xmlData = XAdESExample.XML_DOC.getBytes("UTF-8");
        String tsaUrl  = "http://cryptopro.ru:80/tsp/";

        // Сначала XAdES-BES.

        Document signedDoc = sign(
            new Integer[] {XAdESType.XAdES_BES},
            XAdESConfig.Default.CONFIG_2001_S,
            xmlData,
            WORK_DIR,
            "acct",
            new ITransform[] {new EnvelopedTransform()},
            null,
            false,
            null,
            null
        );

        // Далее усовершенствуем единственного подписанта
        // до XAdES-T. Документ также изменится.

        enhance(
            signedDoc,
            XAdESConfig.Default.CONFIG_2001_S,
            WORK_DIR,
            XAdESType.XAdES_BES,
            XAdESType.XAdES_T,
            // список пуст, т.к. цепочки короткие, а сертификат
            // подписи уже включен в документ
            null,
            tsaUrl
        );

        // Проверка подписи.

        verify(
            signedDoc,
            new Integer[] {XAdESType.XAdES_T},
            null,
            null,
            true,
            0
        );

    }

    /**
     * Создание и проверка подписи формата XAdES
     * со штампом времени.
     *
     * @param xAdESType типы создаваемых подписей.
     * @param xAdESConfig Конфигурация контейнера.
     * @param sourceXmlBin Исходный подписываемый документ.
     * @param workDir Папка для сохранения подписанного документа.
     * @param signingId Подписываемый узел.
     * @param transforms Список трансформаций.
     * @param certificates Список сертификатов.
     * @param addCertificateChain True, если нужно добавить всю цепочку.
     * @param tsaUrl Адрес TSP службы для создания XAdES-T.
     * @param cRLs Список CRL для проверки цепочки сертификатов
     * подписанта.
     * @return подписанный документ.
     * @throws Exception
     */
    public static Document sign(Integer[] xAdESType, IXAdESConfig xAdESConfig,
        byte[] sourceXmlBin, String workDir, String signingId, ITransform[]
        transforms, Set<X509Certificate> certificates, boolean addCertificateChain,
        String tsaUrl, Set<X509CRL> cRLs) throws Exception {

        final Document document = parseFile(sourceXmlBin);

        return sign(xAdESType, xAdESConfig, document, workDir, signingId,
            transforms, certificates, addCertificateChain, tsaUrl, cRLs);

    }

    /**
     * Создание и проверка подписи формата XAdES
     * со штампом времени.
     *
     * @param xAdESType типы создаваемых подписей.
     * @param xAdESConfig Конфигурация контейнера.
     * @param document Исходный подписываемый документ.
     * @param workDir Папка для сохранения подписанного документа.
     * @param signingId Подписываемый узел.
     * @param transforms Список трансформаций.
     * @param certificates Список сертификатов.
     * @param addCertificateChain True, если нужно добавить всю цепочку.
     * @param tsaUrl Адрес TSP службы для создания XAdES-T.
     * @param cRLs Список CRL для проверки цепочки сертификатов
     * подписанта.
     * @return подписанный документ.
     * @throws Exception
     */
    public static Document sign(Integer[] xAdESType, IXAdESConfig xAdESConfig,
        Document document, String workDir, String signingId, ITransform[]
        transforms, Set<X509Certificate> certificates, boolean addCertificateChain,
        String tsaUrl, Set<X509CRL> cRLs) throws Exception {

        Node nodeToSign;
        String referenceURI;

        if (signingId != null) {

            final XPathFactory factory = XPathFactory.newInstance();
            final XPath xpath = factory.newXPath();

            final XPathExpression expr = xpath.compile(String.format("//*[@Id='%s']", signingId));
            final NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

            if (nodes.getLength() == 0) {
                throw new Exception("Can't find node with id: " + signingId);
            } // if

            nodeToSign = nodes.item(0);
            referenceURI = "#" + signingId;

        } // if
        else {
            nodeToSign = document.getDocumentElement();
            referenceURI = "";
        } // else

        return sign(xAdESType, xAdESConfig, workDir, nodeToSign, referenceURI,
            transforms, certificates, addCertificateChain, tsaUrl, cRLs);

    }

    /**
     * Создание и проверка подписи формата XAdES
     * со штампом времени.
     *
     * @param xAdESType типы создаваемых подписей.
     * @param xAdESConfig Конфигурация контейнера.
     * @param workDir Папка для сохранения подписанного документа.
     * @param nodeToSign Подписываемый узел.
     * @param referenceUri Идентификатор подписываемого узела.
     * @param transforms Список трансформаций.
     * @param certificates Список сертификатов.
     * @param addCertificateChain True, если нужно добавить всю цепочку.
     * @param tsaUrl Адрес TSP службы для создания XAdES-T или XAdES-X
     * Long Type 1.
     * @param cRLs Список CRL для проверки цепочки сертификатов
     * подписанта.
     * @return подписанный документ.
     * @throws Exception
     */
    public static Document sign(Integer[] xAdESType, IXAdESConfig xAdESConfig,
        String workDir, Node nodeToSign, String referenceUri, ITransform[]
        transforms, Set<X509Certificate> certificates, boolean addCertificateChain,
        String tsaUrl, Set<X509CRL> cRLs) throws Exception {

        // Загрузка контейнера.

        KeyStore keyStore = KeyStore.getInstance(
            xAdESConfig.getKeyStoreType());

        keyStore.load(null, null);

        Certificate[] chain = keyStore.getCertificateChain(
            xAdESConfig.getSignatureContainer().getAlias());

        if (certificates == null) {
            certificates = new HashSet<X509Certificate>();

            for (Certificate cert : chain) {
                certificates.add((X509Certificate) cert);
            } // for

        } // if

        PrivateKey privateKey;
        if (xAdESConfig.getDefaultProvider().equalsIgnoreCase(ResolveProvider.ALTERNATIVE_PROVIDER)) {

            JCPProtectionParameter parameter = new JCPProtectionParameter(
                xAdESConfig.getSignatureContainer().getPassword());

            JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) keyStore.getEntry(
                xAdESConfig.getSignatureContainer().getAlias(), parameter);

            privateKey = entry.getPrivateKey();

        } // if
        else {
            privateKey = (PrivateKey) keyStore.getKey(
                xAdESConfig.getSignatureContainer().getAlias(),
                xAdESConfig.getSignatureContainer().getPassword());
        }

        // Подпись.

        final ITransform envelopedTransform = new EnvelopedTransform();
        boolean envelopedFound = false;

        // Если в списке есть Enveloped, то берем родителя узла.
        if (transforms != null) {

            for (ITransform transform : transforms) {
                if (transform.getAlgorithm().equals(envelopedTransform.getAlgorithm())) {
                    envelopedFound = true;
                    break;
                }
            }

        }

        if (!envelopedFound && referenceUri != null && !referenceUri.equals("")) {
            nodeToSign = nodeToSign.getParentNode();
        }

        final DataObjects dataObjects = new DataObjects(Collections.singletonList(referenceUri));

        if (transforms != null) {
            for (ITransform transform : transforms) {
                dataObjects.addTransform(transform);
            }
        }

        X509Certificate[] x509Certificates = new X509Certificate[chain.length + certificates.size()];
        System.arraycopy(chain, 0, x509Certificates, 0, chain.length);

        if (!certificates.isEmpty()) {

            System.arraycopy(certificates.toArray(new X509Certificate[certificates.size()]),
                0, x509Certificates, chain.length, certificates.size());

        } // if

        // Создание подписи.
        final XAdESSignature xAdESSignature = new XAdESSignature();

        // Добавление подписей.
        for (Integer type : xAdESType) {

            xAdESSignature.addSigner(xAdESConfig.getDefaultProvider(),
                xAdESConfig.getDigestMethod(), xAdESConfig.getSignatureMethod(),
                    null, privateKey, Arrays.asList(x509Certificates), addCertificateChain,
                        type, tsaUrl, cRLs);

        } // for

        final OutputStream outputStream = (workDir != null)
            ? new FileOutputStream(workDir + "/xades.xml")
            : new ByteArrayOutputStream();

        xAdESSignature.open(outputStream);
        xAdESSignature.update((Element) nodeToSign, dataObjects);

        xAdESSignature.close();
        outputStream.close();
        System.out.println("XAdES signature completed.");

        return (workDir != null)
            ? parseFile(workDir + "/xades.xml")
            : parseFile(((ByteArrayOutputStream) outputStream).toByteArray());

    }

    /**
     * Усовершенствование подписанта.
     *
     * @param document Документ с подписями.
     * @param xAdESConfig Конфигурация контейнера.
     * @param workDir Папка для сохранения подписанного документа.
     * @param decodeType Исходный тип подписи.
     * @param newType Новый тип подписанта.
     * @param certificates Список сертификатов.
     * @param tsaUrl Адрес TSP службы для создания XAdES-T или
     * XAdES-X Long Type 1.
     * @throws Exception
     */
    private static void enhance(Document document, IXAdESConfig xAdESConfig,
        String workDir, Integer decodeType, Integer newType, List<X509Certificate>
        certificates, String tsaUrl) throws Exception {

        // Декодирование.

        XAdESSignature xAdESSignature = new XAdESSignature(
            document.getDocumentElement(), decodeType);

        // Подписант XAdES-BES.
        XAdESSigner srcXAdESSigner = xAdESSignature.getXAdESSignerInfo(0);

        // Усовершенствование decodeType -> newType.

        XAdESSigner dstXAdESSigner = srcXAdESSigner.enhance(
            xAdESConfig.getDefaultProvider(),
            xAdESConfig.getDigestMethod(),
            certificates,
            tsaUrl,
            newType
        );

        if (workDir != null) {
            XMLUtils.writeXML(new File(workDir, "xades_enh.xml"), document);
            XMLUtils.writeXML(new File(workDir, "xades_enh_signer.xml"), dstXAdESSigner.getSignerInfo());
        } // if

        System.out.println("XAdES enhance completed.");
    }

    /**
     * Проверка подписей документа целиком, с самостоятельным извлеченимем
     * подписей, но с одним заданным типом, или с заданием типов всех подписей
     * и последовательным их извлечением.
     *
     * @param sourceXmlBin Проверяемый документ.
     * @param xAdESTypes Список типов. Если fullVerifyWithFirstType==true,
     * то из списка берется только первый тип.
     * @param certificates Дополнительные сертификаты, которые могут
     * быть указаны при проверке подписей для построения цепочки
     * сертификатов.
     * @param cRLs Списки CRL, которые могут использоваться для
     * проверки подписи XAdES-BES или XAdES-T. Может быть null.
     * @param fullVerifyWithFirstType True, если следует передать на проверку
     * весь документ целиком. Если true, то используется только првый тип в
     * списке xAdESTypes.
     * @param expectedTimestampCount Количество штампов в подписи.
     * @return количество найденнных подписей.
     * @throws Exception
     */
    public static int verify(byte[] sourceXmlBin, Integer[] xAdESTypes,
        Set<X509Certificate> certificates, Set<X509CRL> cRLs, boolean
        fullVerifyWithFirstType, int expectedTimestampCount) throws
        Exception {

        final Document document = parseFile(sourceXmlBin);
        return verify(document, xAdESTypes, certificates, cRLs,
            fullVerifyWithFirstType, expectedTimestampCount);

    }

    /**
     * Проверка подписей документа целиком, с самостоятельным извлеченимем
     * подписей, но с одним заданным типом, или с заданием типов всех подписей
     * и последовательным их извлечением.
     *
     * @param document Проверяемый документ.
     * @param xAdESTypes Список типов. Если fullVerifyWithFirstType==true,
     * то из списка берется только первый тип.
     * @param certificates Дополнительные сертификаты, которые могут
     * быть указаны при проверке подписей для построения цепочки
     * сертификатов.
     * @param cRLs Списки CRL, которые могут использоваться для
     * проверки подписи XAdES-BES или XAdES-T. Может быть null.
     * @param fullVerifyWithFirstType True, если следует передать на проверку
     * весь документ целиком. Если true, то используется только првый тип в
     * списке xAdESTypes.
     * @param expectedTimestampCount Количество штампов в подписи.
     * @return количество найденнных подписей.
     * @throws Exception
     */
    public static int verify(Document document, Integer[] xAdESTypes,
        Set<X509Certificate> certificates, Set<X509CRL> cRLs, boolean
        fullVerifyWithFirstType, int expectedTimestampCount) throws
        Exception {

        if (!fullVerifyWithFirstType) {

            // Проверка раздельная, каждой подписи.

            int actualTimestampCount = 0;
            final NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");

            if (nl.getLength() == 0) {
                throw new Exception("Cannot find Signature element");
            } // if

            if (xAdESTypes.length != nl.getLength()) {
                throw new Exception("Count of XAdES types not equal signature count");
            }

            for (int i = 0; i < nl.getLength(); i++) {

                // Декодирование конкретной подписи.
                final XAdESSignature xAdESSignature = new XAdESSignature((Element) nl.item(i), xAdESTypes[i]);
                final XAdESSigner xAdESSigner = xAdESSignature.getXAdESSignerInfo(0);

                // Проверка.

                if (!xAdESSigner.getSignatureType().equals(XAdESType.XAdES_X_Long_Type_1)) {
                    xAdESSigner.verify(certificates, cRLs);
                } // if
                else {
                    xAdESSigner.verify(null, null);
                } // else

                actualTimestampCount += check(xAdESTypes[i], xAdESSigner, i, true);

            } // for

            if (actualTimestampCount != expectedTimestampCount) {
                throw new Exception("Invalid timestamp count, expected: " +
                    expectedTimestampCount + " but actual: " + actualTimestampCount);
            } // if

            System.out.println("XAdES verification completed.");
            return nl.getLength();

        } // if
        else {

            // Проверка всего документа с типов в первом элементе.

            final XAdESSignature xmlAdvancedSignature = new XAdESSignature(
                document.getDocumentElement(), xAdESTypes[0]);

            if (!xAdESTypes[0].equals(XAdESType.XAdES_X_Long_Type_1)) {
                xmlAdvancedSignature.verify(certificates, cRLs);
            } // if
            else {
                xmlAdvancedSignature.verify(null);
            } // else

            System.out.println("XAdES verification completed.");
            return xmlAdvancedSignature.getXAdESSignerInfos().length;

        } // else

    }

    /**
     * Проверка дополнительных полей в зависимости от типа
     * подписи.
     *
     * @param xAdESType Предполагаемый тип подписи.
     * @param xAdESSigner Подписант.
     * @param i Номер подписанта.
     * @return количество штампов времени в подписанте.
     * @throws Exception
     */
    public static int check(Integer xAdESType, XAdESSigner xAdESSigner,
        int i, boolean fullDecoded) throws Exception {

        int timeStampCount = 0;

        if (xAdESSigner.getSignerInfo() == null) {
            throw new Exception("SignerInfo is null");
        } // if

        if (xAdESSigner.getSignatureValue() == null) {
            throw new Exception("SignatureValue is null");
        } // if

        if (xAdESSigner.getSignerCertificate() == null) {
            throw new Exception("Signer certificate is null");
        } // if

        // Отдельно для XAdES-T и XAdES-X Long Type 1.
        if (xAdESType.equals(XAdESType.XAdES_T) ||
            xAdESType.equals(XAdESType.XAdES_X_Long_Type_1)) {

            if (fullDecoded) {

                final TimeStampToken signatureTimestamp = ((XAdESSignerT) xAdESSigner)
                    .getEarliestValidSignatureTimeStampToken();

                if (signatureTimestamp == null) {
                    throw new Exception("Invalid earliest signature timestamp" +
                            " in [" + i + "] signature");
                } // if

            } // if

            final List<TimeStampToken> signatureTimeStamps =
                ((XAdESSignerT)xAdESSigner).getSignatureTimestampTokens();

            if (signatureTimeStamps == null || signatureTimeStamps.isEmpty()) {
                throw new Exception("Invalid signature timestamp count, empty " +
                    "list in [" + i + "] signature");
            } // if

            timeStampCount += signatureTimeStamps.size();

        } // if

        // Отдельно для XAdES-X Long Type 1.
        if (xAdESType.equals(XAdESType.XAdES_X_Long_Type_1)) {

            if (fullDecoded) {

                final TimeStampToken sigAndRefsTimestamp = ((XAdESSignerXLT1) xAdESSigner)
                    .getEarliestValidSigAndRefsTimeStampToken();

                if (sigAndRefsTimestamp == null) {
                    throw new Exception("Invalid earliest sig-and-refs timestamp" +
                            "in [" + i + "] signature");
                } // if

            } // if

            final List<TimeStampToken> signAndRefsTimeStamps =
                ((XAdESSignerXLT1)xAdESSigner).getSigAndRefsTimestampTokens();

            if (signAndRefsTimeStamps == null || signAndRefsTimeStamps.isEmpty()) {
                throw new Exception("Invalid sig-and-refs timestamp count, empty " +
                    "list in [" + i + "] signature");
            } // if

            timeStampCount += signAndRefsTimeStamps.size();

        } // if

        return timeStampCount;

    }

}
