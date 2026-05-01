/**
 * $RCSfileXAdESV2Verify.java,v $
 * Copyright 2004-2015 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен для целей
 * обучения. Может быть скопирован или модифицирован при условии сохранения
 * абзацев с указанием авторства и прав.
 * <br>
 * Данный код не может быть непосредственно использован для защиты информации.
 * Компания Крипто-Про не несет никакой ответственности за функционирование
 * этого кода.
 */
package xades;

import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.w3c.dom.Document;

import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.XAdES.XAdESType;

import ru.CryptoPro.JCP.Util.JCPInit;
import xades.util.GostXAdESUtility;
import xades.util.XMLUtility;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс для проверки подписи, созданной на языке С (в новом формате).
 * Проверяется в форматах XAdES-BES и XAdES-T. В примере используется класс
 * xades. AnonymousResolver, поскольку у реализации С могут отсуствуют URI в
 * некоторых узлах Reference.
 *
 * @author Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XAdESV2Verify extends GostXAdESUtility {

    /**
     * Документ, который подписывается.
     */
    public static final String XML_DOC =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<Envelope xmlns=\"urn:envelope\">" +
                    "<Data>" +
                    "Hello, World!" +
                    "</Data>" +
                    "<Node xml:id=\"nodeID\">" +
                    "Hello, Node!" +
                    "</Node>" +
                    "</Envelope>";

    public static final String XADES_DOC_PATH = TRUST_DIR + File.separator
        + "xml" + File.separator + "XAdES" + File.separator + "V2" + File.separator;

    protected static final Collection STORE_CERTIFICATE = new ArrayList();
    protected static final Collection STORE_CERTIFICATE_CRL = new ArrayList();


    private static final String CERT_PATH_2001 = TRUST_DIR +
            File.separator + "CERTS" + File.separator + "VERIFY" + File.separator;

    private static final String CRL_PATH_2001 = TRUST_DIR +
            File.separator + "CRLS" + File.separator;


    public static void main(String[] args) throws Exception {

        // Отключаем проверку цепочки службы штампов
        //System.setProperty("ru.CryptoPro.CAdES.validate_tsp", "false");

        XMLUtility.loadIntermediateCertificateAndCrlList(
                CERT_PATH_2001 + "ca0.cer", false, STORE_CERTIFICATE);

        XMLUtility.loadIntermediateCertificateAndCrlList(
                CERT_PATH_2001 + "ca1.cer", false, STORE_CERTIFICATE);

        XMLUtility.loadIntermediateCertificateAndCrlList(
                CERT_PATH_2001 + "ca2.cer", false, STORE_CERTIFICATE);


        XMLUtility.loadIntermediateCertificateAndCrlList(
                CRL_PATH_2001 + "root.crl", true, STORE_CERTIFICATE_CRL);

        Set<X509CRL> cRLs =  new HashSet<X509CRL>(STORE_CERTIFICATE_CRL);

        Set<X509Certificate> certificates = new HashSet<X509Certificate>(STORE_CERTIFICATE);

        // добавление AnonymousResolver для проверки узлов
        // Reference без URI. Передавать для инициализаии
        // InputStream можно, т.к проверка производится в
        // одном потоке.

        ResourceResolver.register(new AnonymousResolver(
            new ByteArrayInputStream(XML_DOC.getBytes())), true);

        byte[] doc = Array.readFile(XADES_DOC_PATH + "xades-v2-verify.xml");
        Document signedDoc = XMLUtility.parseFile(doc);

/*        System.out.println("XAdES BES verification");

        try {

            XAdESSignVerify.verify(signedDoc,
                    new Integer[]{XAdESType.XAdES_BES},
                    null,
                    null,
                    false,
                    0);

        } catch (Exception ex) {

            Exception e = new Exception("XAdES [1] verification fail", ex);
            e.printStackTrace();

        }

        System.out.println("XAdES BES verification full doc");

        try {

            XAdESSignVerify.verify(signedDoc,
                    new Integer[]{XAdESType.XAdES_BES},
                    null,
                    null,
                    true,
                    0);

        } catch (Exception ex) {

            Exception e = new Exception("XAdES [2] verification fail", ex);
            e.printStackTrace();

        }

        System.out.println("XAdES T verification");

        try {

            XAdESSignVerify.verify(signedDoc,
                    new Integer[]{XAdESType.XAdES_T},
                    certificates,
                    cRLs,
                    false,
                    1);

        } catch (Exception ex) {

            Exception e = new Exception("XAdES-T [1] verification fail", ex);
            e.printStackTrace();

        }

        System.out.println("XAdES T verification full");

        try {

            XAdESSignVerify.verify(signedDoc,
                    new Integer[]{XAdESType.XAdES_T},
                    certificates,
                    cRLs,
                    true,
                    1);

        } catch (Exception ex) {

            Exception e = new Exception("XAdES-T [2] verification fail", ex);
            e.printStackTrace();

        }

*/
    System.out.println("XAdES XLT-1 verification full");

    try {
        JCPInit.initProviders(false);
        XAdESSignVerify.verify(signedDoc,
                new Integer[]{XAdESType.XAdES_X_Long_Type_1},
                certificates,
                cRLs,
                true,
                1);

    } catch (Exception ex) {

        Exception e = new Exception("XAdES-XLT-1 verification fail", ex);
        e.printStackTrace();

    }

}

}
