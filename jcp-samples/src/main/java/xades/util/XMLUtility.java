/**
 * $RCSfileXMLUtility.java,v $
 * version $Revision: 36379 $
 * created 31.08.2016 10:39 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * (C) ООО Крипто-Про 2004-2016.
 * <br>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <br>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package xades.util;

import org.w3c.dom.Document;

import ru.xml.tools.DocumentBuilderFactoryHelper;
import ru.xml.tools.XmlFeatureHelper;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.DirList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;

import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;

import java.util.Arrays;
import java.util.Collection;

/**
 * Служебный класс.
 *
 * @author Copyright 2004-2016 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XMLUtility {

    /**
     * Фабрика сертификатов.
     */
    public static final CertificateFactory CERTIFICATE_FACTORY;

    static {

        try {
            CERTIFICATE_FACTORY = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Получение списка промежуточных сертификатов и CRL.
     *
     * @param path Папка с файлами.
     * @param needCrl True, если проверять и грузить CRL.
     * @param storeList Заполняемый список.
     * @throws Exception
     */
    public static void loadIntermediateCertificateAndCrlList(String path, boolean needCrl, Collection storeList) throws Exception {
        final File rootDirectory = new File(path);
        if (rootDirectory.exists()) {
            final File[] files = rootDirectory.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    readObject(file, storeList, needCrl);
                } // for
            } // if
            else {
                readObject(rootDirectory, storeList, needCrl);
            } // else
        } // if
    }

    /**
     * Чтение содержимого сертификата или CRL из файла.
     *
     * @param file Читаемый файл.
     * @param storeList Заполняемый список.
     * @param needCrl True, если проверять и грузить CRL.
     * @throws Exception
     */
    private static void readObject(File file, Collection storeList, boolean needCrl) throws Exception {
        final String fileExt = DirList.getFileExtension(file.getName());
        if (Arrays.asList(".cer", ".crt", ".der").contains(fileExt)) {
            try (FileInputStream is = new FileInputStream(file)) {
                final X509Certificate cert = (X509Certificate) CERTIFICATE_FACTORY.generateCertificate(is);
                storeList.add(cert);
            }
        } // if
        else if (needCrl && fileExt.equalsIgnoreCase(".crl")) {
            try (FileInputStream is = new FileInputStream(file)) {
                final X509CRL crl = (X509CRL) CERTIFICATE_FACTORY.generateCRL(is);
                storeList.add(crl);
            }
        } // else
    }

    /**
     * Чтение хранилища сертификатов.
     *
     * @param certStorePath Путь к хранилищу.
     * @param password Пароль к хранилищу.
     * @return открытое хранилище.
     * @throws Exception
     */
    public static KeyStore loadCertStore(String certStorePath, char[] password) throws Exception {
        final FileInputStream inputStream = new FileInputStream(certStorePath);
        final KeyStore ks = KeyStore.getInstance(JCP.CERT_STORE_NAME);
        ks.load(inputStream, password);
        inputStream.close();
        return ks;
    }

    /**
     * Чтение документа из файла.
     *
     * @param fileName Путь и имя читаемого файла.
     * @return документ.
     * @throws Exception
     */
    public static Document parseFile(String fileName) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactoryHelper.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setNamespaceAware(true);
        try (FileInputStream is = new FileInputStream(fileName)) {
            return dbf.newDocumentBuilder().parse(is);
        }
    }

    /**
     * Преобразование документа из данных.
     *
     * @param fileData Содержимое файла.
     * @return документ.
     * @throws Exception
     */
    public static Document parseFile(byte[] fileData) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactoryHelper.newInstance();
        dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbFactory.setNamespaceAware(true);
        return dbFactory.newDocumentBuilder().parse(new ByteArrayInputStream(fileData));
    }

}
