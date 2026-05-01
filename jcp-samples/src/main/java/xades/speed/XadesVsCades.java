/**
 * $RCSfileXadesVsCades.java,v $
 * version $Revision$ created 13.02.2018 0:12 by elvira
 * last modified $Date$ by $Author$
 * <br>
 * Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package xades.speed;

import CAdES.configuration.IConfiguration;
import CAdES.configuration.SimpleConfiguration;
import CAdES.configuration.container.Container2001;
import org.bouncycastle.tsp.TimeStampToken;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESType;

import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.tools.Array;

import ru.CryptoPro.XAdES.DataObjects;
import ru.CryptoPro.XAdES.XAdESSignature;
import ru.CryptoPro.XAdES.XAdESSigner;
import ru.CryptoPro.XAdES.XAdESSignerT;
import ru.CryptoPro.XAdES.XAdESSignerXLT1;
import ru.CryptoPro.XAdES.XAdESType;
import ru.CryptoPro.XAdES.transform.EnvelopedTransform;
import ru.CryptoPro.XAdES.transform.ITransform;
import util.ResolveProvider;
import ru.CryptoPro.JCP.Util.JCPInit;
import xades.config.IXAdESConfig;
import xades.config.XAdESConfig;
import xades.util.XMLUtility;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Пример для сравнения скорости работы функций подписи
 * и проверки подписи для Xades и Cades (в одном потоке
 * и в многопоточном режиме).
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XadesVsCades {

    /**
     * Документ для подписи.
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

    public static final String WORK_DIR = System.getProperty("user.dir") +
            File.separator + "temp" + File.separator;

    public static final String TRUST_DIR = System.getProperty("user.dir") +
            File.separator + "data" + File.separator;

    /**
     * Количество итераций в цикле
     */
    public final static int iCount = 1000;

    /**
     * Количество потоков
     */
    public final static int threadCount = 10;

    static {
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");
    }

    /**
     * Различные варианты подписи и проверки подписи для
     * Cades и Xades. Выбор функции через командную строку.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(false);
        int n = args.length > 0 ? Integer.parseInt(args[0]) : 0;
        switch (n) {
            case 1:
                signXadesStream();
                break;
            case 2:
                signXadesStreamThread();
                break;
            case 3:
                signXadesFile();
                break;
            case 4:
                signXadesFileThread();
                break;
            case 5:
                signCadesStream();
                break;
            case 6:
                signCadesStreamThread();
                break;
            case 7:
                signCadesFile();
                break;
            case 8:
                signCadesFileThread();
                break;
            case 9:
                verifyXadesBes();
                break;
            case 10:
                verifyXadesBesThread();
                break;
            case 11:
                verifyXadesT();
                break;
            case 12:
                verifyXadesTThread();
                break;
            case 13:
                verifyCadesBes();
                break;
            case 14:
                verifyCadesBesThread();
                break;
            case 15:
                verifyCadesT();
                break;
            case 16:
                verifyCadesTThread();
                break;
            default:
                break;

        }

    }

    /**
     * Подпись Xades с записью в байтовый поток (однопоточный тест)
     *
     * @throws Exception
     */
    public static void signXadesStream() throws Exception {
        System.out.println("Create xades BES to stream (single)");
        IXAdESConfig xAdESConfigTestSignKey = XAdESConfig.Default.CONFIG_2001_S;
        byte[] xmlData = XML_DOC.getBytes("UTF-8");
        sign(
                new Integer[]{XAdESType.XAdES_BES},
                xAdESConfigTestSignKey,
                xmlData,
                null,
                null,
                new ITransform[]{new EnvelopedTransform()},
                null,
                false,
                null,
                null
        );
        System.out.println("**************");

    }

    /**
     * Подпись Xades с записью в байтовый поток (многопоточный тест)
     *
     * @throws Exception
     */
    public static void signXadesStreamThread() throws Exception {
        System.out.println("Create xades BES to stream (multi)");
        Thread[] array = new Thread[threadCount];
        for (int j = 0; j < threadCount; j++)
            array[j] = new XadesVsCadesThread(j, false, true,
                XAdESType.XAdES_BES, null, null, null, 0, null);
        for (int j = 0; j < threadCount; j++) {
            array[j].start();
        }
        for (int j = 0; j < threadCount; j++) {
            array[j].join();
        }
        System.out.println("**************");

    }

    /**
     * Подпись Xades с записью в файл (однопоточный тест)
     *
     * @throws Exception
     */
    public static void signXadesFile() throws Exception {
        System.out.println("Create xades BES to file (single)");
        IXAdESConfig xAdESConfigTestSignKey = XAdESConfig.Default.CONFIG_2001_S;
        byte[] xmlData = XML_DOC.getBytes("UTF-8");
        sign(
                new Integer[]{XAdESType.XAdES_BES},
                xAdESConfigTestSignKey,
                xmlData,
                WORK_DIR + "testXades.xml",
                null,
                new ITransform[]{new EnvelopedTransform()},
                null,
                false,
                null,
                null
        );
        System.out.println("**************");

    }

    /**
     * Подпись Xades с записью в файл (многопоточный тест)
     *
     * @throws Exception
     */
    public static void signXadesFileThread() throws Exception {
        System.out.println("Create xades BES to file (multi)");
        Thread[] array = new Thread[threadCount];
        for (int j = 0; j < threadCount; j++)
            array[j] = new XadesVsCadesThread(j, false, true,
                XAdESType.XAdES_BES, null, null, null, 0,
                    WORK_DIR + "testXades.xml");
        for (int j = 0; j < threadCount; j++) {
            array[j].start();
        }
        for (int j = 0; j < threadCount; j++) {
            array[j].join();
        }
        System.out.println("**************");

    }

    /**
     * Подпись Cades с записью в байтовый поток (однопоточный тест)
     *
     * @throws Exception
     */
    public static void signCadesStream() throws Exception {
        System.out.println("Create cades BES to stream (single)");
        IConfiguration config = new SimpleConfiguration(new Container2001(), false);
        byte[] xmlData = XML_DOC.getBytes("UTF-8");
        signCADES(config, CAdESType.CAdES_BES, null, xmlData, null);
        System.out.println("**************");

    }

    /**
     * Подпись Cades с записью в байтовый поток (многопоточный тест)
     *
     * @throws Exception
     */
    public static void signCadesStreamThread() throws Exception {
        System.out.println("Create cades BES to stream (multi)");
        Thread[] array = new Thread[threadCount];
        for (int j = 0; j < threadCount; j++)
            array[j] = new XadesVsCadesThread(j, true, true,
                CAdESType.CAdES_BES, null, null, null, 0, null);
        for (int j = 0; j < threadCount; j++) {
            array[j].start();
        }
        for (int j = 0; j < threadCount; j++) {
            array[j].join();
        }
        System.out.println("**************");

    }

    /**
     * Подпись Cades с записью в файл (однопоточный тест)
     *
     * @throws Exception
     */
    public static void signCadesFile() throws Exception {
        System.out.println("Create cades BES to file (single)");
        IConfiguration config = new SimpleConfiguration(new Container2001(), false);
        byte[] xmlData = XML_DOC.getBytes("UTF-8");
        signCADES(config, CAdESType.CAdES_BES, null, xmlData,
            WORK_DIR + "testCades.bin");
        System.out.println("**************");

    }

    /**
     * Подпись Cades с записью в файл (многопоточный тест)
     *
     * @throws Exception
     */
    public static void signCadesFileThread() throws Exception {
        System.out.println("Create cades BES to file (multi)");
        Thread[] array = new Thread[threadCount];
        for (int j = 0; j < threadCount; j++)
            array[j] = new XadesVsCadesThread(j, true, true,
                CAdESType.CAdES_BES, null, null, null, 0,
                    WORK_DIR + "testCades.bin");
        for (int j = 0; j < threadCount; j++) {
            array[j].start();
        }
        for (int j = 0; j < threadCount; j++) {
            array[j].join();
        }
        System.out.println("**************");

    }

    /**
     * Проверка Xades BES (однопоточно)
     *
     * @throws Exception
     */
    public static void verifyXadesBes() throws Exception {
        System.out.println("Verify xades BES (single)");
        byte[] doc = Array.readFile(TRUST_DIR + "xadesTestBes.xml");
        Document signedDoc = XMLUtility.parseFile(doc);
        XadesVsCades.verify(signedDoc, new Integer[]{XAdESType.XAdES_BES},
            null, null, false, 0);
        System.out.println("*************");
    }

    /**
     * Проверка Xades BES (много поточно)
     *
     * @throws Exception
     */
    public static void verifyXadesBesThread() throws Exception {
        System.out.println("Verify xades BES (multi)");
        Thread[] array = new Thread[XadesVsCades.threadCount];
        for (int j = 0; j < XadesVsCades.threadCount; j++)
            array[j] = new XadesVsCadesThread(j, false, false,
                XAdESType.XAdES_BES, null, null, null, 0,
                    TRUST_DIR + "xadesTestBes.xml");
        for (int j = 0; j < XadesVsCades.threadCount; j++) {
            array[j].start();
        }
        for (int j = 0; j < XadesVsCades.threadCount; j++) {
            array[j].join();
        }
        System.out.println("*************");
    }

    /**
     * Проверка Xades T (однопоточно)
     *
     * @throws Exception
     */
    public static void verifyXadesT() throws Exception {
        System.out.println("Verify xades T (single)");
        byte[] doc = Array.readFile(TRUST_DIR + "xadesTestT.xml");
        Document signedDoc = XMLUtility.parseFile(doc);
        XadesVsCades.verify(signedDoc, new Integer[]{XAdESType.XAdES_T},
            null, null, false, 1);
        System.out.println("*************");
    }

    /**
     * Проверка Xades T (много поточно)
     *
     * @throws Exception
     */
    public static void verifyXadesTThread() throws Exception {
        System.out.println("Verify xades T (multi)");
        Thread[] array = new Thread[XadesVsCades.threadCount];
        for (int j = 0; j < XadesVsCades.threadCount; j++)
            array[j] = new XadesVsCadesThread(j, false, false,
                XAdESType.XAdES_T, null, null, null, 1,
                    TRUST_DIR + "xadesTestT.xml");
        for (int j = 0; j < XadesVsCades.threadCount; j++) {
            array[j].start();
        }
        for (int j = 0; j < XadesVsCades.threadCount; j++) {
            array[j].join();
        }
        System.out.println("*************");
    }

    /**
     * Проверка Cades BES (однопоточно)
     *
     * @throws Exception
     */
    public static void verifyCadesBes() throws Exception {
        System.out.println("Verify Cades BES (single)");
        IConfiguration config = new SimpleConfiguration(new Container2001(), false);
        XadesVsCades.verifyCAdES(config, TRUST_DIR + "cadesTestBes.bin");
        System.out.println("*************");
    }

    /**
     * Проверка Cades BES (много поточно)
     *
     * @throws Exception
     */
    public static void verifyCadesBesThread() throws Exception {
        System.out.println("Verify Cades BES (multi)");
        Thread[] array = new Thread[XadesVsCades.threadCount];
        for (int j = 0; j < XadesVsCades.threadCount; j++)
            array[j] = new XadesVsCadesThread(j, true, false,
                CAdESType.CAdES_BES, null, null, null, 0,
                    TRUST_DIR + "cadesTestBes.bin");
        for (int j = 0; j < XadesVsCades.threadCount; j++) {
            array[j].start();
        }
        for (int j = 0; j < XadesVsCades.threadCount; j++) {
            array[j].join();
        }
        System.out.println("*************");
    }

    /**
     * Проверка Cades T (однопоточно)
     *
     * @throws Exception
     */
    public static void verifyCadesT() throws Exception {
        System.out.println("Verify Cades T (single)");
        IConfiguration config = new SimpleConfiguration(new Container2001(), false);
        XadesVsCades.verifyCAdES(config, TRUST_DIR + "cadesTestT.bin");
        System.out.println("*************");
    }

    /**
     * Проверка Cades T (много поточно)
     *
     * @throws Exception
     */
    public static void verifyCadesTThread() throws Exception {
        System.out.println("Verify Cades T (multi)");
        Thread[] array = new Thread[XadesVsCades.threadCount];
        for (int j = 0; j < XadesVsCades.threadCount; j++)
            array[j] = new XadesVsCadesThread(j, true, false,
                CAdESType.CAdES_BES, null, null, null, 0,
                    TRUST_DIR + "cadesTestT.bin");
        for (int j = 0; j < XadesVsCades.threadCount; j++) {
            array[j].start();
        }
        for (int j = 0; j < XadesVsCades.threadCount; j++) {
            array[j].join();
        }
        System.out.println("*************");
    }

    /**
     * Создание CAdES-подписи
     *
     * @throws Exception
     */
    public static void signCADES(IConfiguration config, Integer cadesType,
        String tsaUrl, byte[] data, String workDir) throws Exception {

        CAdESSignature cadesSignature = new CAdESSignature();

        cadesSignature.addSigner(config.getProviderName(),
                config.getDigestOid(),
                config.getPublicKeyOid(),
                config.getPrivateKey(),
                config.getChain(),
                cadesType,
                tsaUrl, false,
                config.getSignedAttributes(),
                config.getUnsignedAttributes());

        for (int i = 0; i < 20; i++) {
            final OutputStream outputStream = (workDir != null)
                    ? new FileOutputStream(workDir)
                    : new ByteArrayOutputStream();
            cadesSignature.open(outputStream);
            cadesSignature.update(data);
            cadesSignature.close();
            outputStream.close();
        }

        long base = System.currentTimeMillis();
        for (int i = 0; i < iCount; i++) {
            final OutputStream outputStream = (workDir != null)
                    ? new FileOutputStream(workDir)
                    : new ByteArrayOutputStream();
            cadesSignature.open(outputStream);
            cadesSignature.update(data);
            cadesSignature.close();
            outputStream.close();
        }

        long result = System.currentTimeMillis();
        System.out.println("Sign result: " + (double) (iCount * 1000) / (result - base));

    }

    /**
     * Проверка CAdES-подписи.
     *
     * @throws Exception
     */
    public static void verifyCAdES(IConfiguration config, String
        signPath) throws Exception {

        byte[] data = Array.readFile(signPath);
        ByteArrayInputStream cadesCmsStream = new ByteArrayInputStream(data);
        CAdESSignature cadesSignature = new CAdESSignature(cadesCmsStream, null, null);

        for (int j = 0; j < 20; j++)
            cadesSignature.verify(config.getCertificateStore() != null ? null : new HashSet<X509Certificate>(config.getChain()),
                    (config.getCRLStore() != null ? null : config.getCRLs()));

        long base = System.currentTimeMillis();
        for (int j = 0; j < iCount; j++)
            cadesSignature.verify(config.getCertificateStore() != null ? null : new HashSet<X509Certificate>(config.getChain()),
                    (config.getCRLStore() != null ? null : config.getCRLs()));
        long result = System.currentTimeMillis();
        System.out.println("Verify result: " + (double) (iCount * 1000) / (result - base));

    }

    /**
     * Создание и проверка подписи формата XAdES со штампом времени.
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
     * @param cRLs Список CRL для проверки цепочки сертификатов подписанта.
     * @return подписанный документ.
     * @throws Exception
     */
    public static void sign(Integer[] xAdESType, IXAdESConfig xAdESConfig,
        byte[] sourceXmlBin, String workDir, String signingId, ITransform[]
        transforms, Set<X509Certificate> certificates, boolean addCertificateChain,
        String tsaUrl, Set<X509CRL> cRLs) throws Exception {

        final Document document = XMLUtility.parseFile(sourceXmlBin);

        sign(xAdESType, xAdESConfig, document, workDir, signingId,
            transforms, certificates, addCertificateChain, tsaUrl, cRLs);

    }

    /**
     * Создание и проверка подписи формата XAdES со штампом времени.
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
     * @param cRLs Список CRL для проверки цепочки сертификатов подписанта.
     * @return подписанный документ.
     * @throws Exception
     */
    public static void sign(Integer[] xAdESType, IXAdESConfig xAdESConfig,
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

        sign(xAdESType, xAdESConfig, workDir, nodeToSign, referenceURI,
            transforms, certificates, addCertificateChain, tsaUrl, cRLs);

    }

    /**
     * Создание и проверка подписи формата XAdES со штампом времени.
     *
     * @param xAdESType типы создаваемых подписей.
     * @param xAdESConfig Конфигурация контейнера.
     * @param workDir Папка для сохранения подписанного документа.
     * @param nodeToSign Подписываемый узел.
     * @param referenceUri Идентификатор подписываемого узела.
     * @param transforms Список трансформаций.
     * @param certificates Список сертификатов.
     * @param addCertificateChain True, если нужно добавить всю цепочку.
     * @param tsaUrl Адрес TSP службы для создания XAdES-T или XAdES-X Long Type
     * 1.
     * @param cRLs Список CRL для проверки цепочки сертификатов подписанта.
     * @return подписанный документ.
     * @throws Exception
     */
    public static void sign(Integer[] xAdESType, IXAdESConfig xAdESConfig,
        String workDir, Node nodeToSign, String referenceUri, ITransform[]
        transforms, Set<X509Certificate> certificates, boolean addCertificateChain,
        String tsaUrl, Set<X509CRL> cRLs) throws Exception {

        // Загрузка контейнера.

        KeyStore keyStore = KeyStore.getInstance(xAdESConfig.getKeyStoreType());
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

        for (int i = 0; i < 20; i++) {
            final OutputStream outputStream = (workDir != null)
                ? new FileOutputStream(workDir)
                : new ByteArrayOutputStream();
            xAdESSignature.open(outputStream);
            xAdESSignature.update((Element) nodeToSign, dataObjects);
            xAdESSignature.close();
            outputStream.close();
        }

        long base = System.currentTimeMillis();
        for (int i = 0; i < iCount; i++) {
            final OutputStream outputStream = (workDir != null)
                ? new FileOutputStream(workDir)
                : new ByteArrayOutputStream();
            xAdESSignature.open(outputStream);
            xAdESSignature.update((Element) nodeToSign, dataObjects);
            xAdESSignature.close();
            outputStream.close();
        }

        long result = System.currentTimeMillis();
        System.out.println("Sign result: " + (double) (iCount * 1000) / (result - base));

    }

    /**
     * Проверка подписей документа целиком, с самостоятельным извлеченимем
     * подписей, но с одним заданным типом, или с заданием типов всех подписей и
     * последовательным их извлечением.
     *
     * @param document Проверяемый документ.
     * @param xAdESTypes Список типов. Если fullVerifyWithFirstType==true, то из
     * списка берется только первый тип.
     * @param certificates Дополнительные сертификаты, которые могут быть
     * указаны при проверке подписей для построения цепочки сертификатов.
     * @param cRLs Списки CRL, которые могут использоваться для проверки подписи
     * XAdES-BES или XAdES-T. Может быть null.
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
                    for (int j = 0; j < 20; j++)
                        xAdESSigner.verify(certificates, cRLs);

                    long base = System.currentTimeMillis();
                    for (int j = 0; j < iCount; j++)
                        xAdESSigner.verify(certificates, cRLs);
                    long result = System.currentTimeMillis();
                    System.out.println("Verify result: " + (double) (iCount * 1000) / (result - base));

                } // if
                else {
                    for (int j = 0; j < 20; j++)
                        xAdESSigner.verify(null, null);

                    long base = System.currentTimeMillis();
                    for (int j = 0; j < iCount; j++)
                        xAdESSigner.verify(null, null);
                    long result = System.currentTimeMillis();
                    System.out.println("Verify result: " + (double) (iCount * 1000) / (result - base));
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
                for (int j = 0; j < 20; j++)
                    xmlAdvancedSignature.verify(certificates, cRLs);

                long base = System.currentTimeMillis();
                for (int j = 0; j < iCount; j++)
                    xmlAdvancedSignature.verify(certificates, cRLs);
                long result = System.currentTimeMillis();
                System.out.println("Verify result: " + (double) (iCount * 1000) / (result - base));
            } // if
            else {
                for (int j = 0; j < 20; j++)
                    xmlAdvancedSignature.verify(null);

                long base = System.currentTimeMillis();
                for (int j = 0; j < iCount; j++)
                    xmlAdvancedSignature.verify(null);
                long result = System.currentTimeMillis();
                System.out.println("Verify result: " + (double) (iCount * 1000) / (result - base));
            } // else

            System.out.println("XAdES verification completed.");
            return xmlAdvancedSignature.getXAdESSignerInfos().length;

        } // else

    }

    /**
     * Проверка дополнительных полей в зависимости от типа подписи.
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
                ((XAdESSignerT) xAdESSigner).getSignatureTimestampTokens();

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
                ((XAdESSignerXLT1) xAdESSigner).getSigAndRefsTimestampTokens();

            if (signAndRefsTimeStamps == null || signAndRefsTimeStamps.isEmpty()) {
                throw new Exception("Invalid sig-and-refs timestamp count, empty " +
                    "list in [" + i + "] signature");
            } // if

            timeStampCount += signAndRefsTimeStamps.size();

        } // if
        return timeStampCount;
    }

}
