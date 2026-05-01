package xades;

import org.w3c.dom.Document;

import ru.CryptoPro.XAdES.XAdESType;
import ru.CryptoPro.XAdES.transform.EnvelopedTransform;
import ru.CryptoPro.XAdES.transform.ITransform;
import ru.CryptoPro.XAdES.util.XMLUtils;

import xades.config.IXAdESConfig;
import xades.config.XAdESConfig;

import xades.util.GostXAdESUtility;

import xades4j.algorithms.Algorithm;
import xades4j.algorithms.EnvelopedSignatureTransform;

import java.io.File;

/**
 * Класс для создания и взаимной проверки подписи классами
 * XAdESSignVerify и XAdES4JSignVerify.
 * Created by elvira on 20.11.2017.
 */
public class XAdESvsXadES4JExample extends GostXAdESUtility {

    /**
     * Пример документа. Подписываться будет
     * узел с id="acct".
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

    /**
     * Подписываемый узел.
     */
    public static final String XML_DOC_ID = "record";

    /**
     * Запуск примеров.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        IXAdESConfig xAdESConfigTestSignKey = XAdESConfig.Default.CONFIG_2001_S;

        //подпись при помощи xades
        System.out.println("Create XAdES signature");
        Document signedXAdESTDoc = XAdESSignVerify.sign(
                new Integer[]{XAdESType.XAdES_T},
                xAdESConfigTestSignKey,
                XML_DOC.getBytes("UTF-8"),
                WORK_DIR,
                null,
                new ITransform[] {new EnvelopedTransform()},
                null,
                false,
                "http://www.cryptopro.ru:80/tsp/",
                null);

        //подпись при помощи xades4j

        System.out.println("Create XAdES4J signature");
        Document signedXAdES4JTDoc = XAdES4JSignVerify.sign(
                true,
                xAdESConfigTestSignKey,
                XML_DOC.getBytes("UTF-8"),
                WORK_DIR,
                null,
                new Algorithm[]{new EnvelopedSignatureTransform()});


        XMLUtils.writeXML(new File(WORK_DIR, "xades_JCP.xml"), signedXAdESTDoc);
        XMLUtils.writeXML(new File(WORK_DIR, "xades_4J.xml"), signedXAdES4JTDoc);
        //Проверка подписи xades при помощи xades

        System.out.println("XAdES verification of XAdES signature");
        XAdESSignVerify.verify(signedXAdESTDoc, new Integer[]{XAdESType.XAdES_T}, null, null, false, 1);

        //Проверка подписи xades4j при помощи xades

        System.out.println("XAdES verification of XAdES4J signature");
        XAdESSignVerify.verify(signedXAdES4JTDoc, new Integer[]{XAdESType.XAdES_T}, null, null, false, 1);

        //Проверка подписи xades при помощи xades4j

        System.out.println("XAdES4J verification of XAdES signature");
        XAdES4JSignVerify.verify(xAdESConfigTestSignKey, signedXAdESTDoc, false);

        //Проверка подписи xades4j при помощи xades4j

        System.out.println("XAdES4J verification of XAdES4J signature");
        XAdES4JSignVerify.verify(xAdESConfigTestSignKey, signedXAdES4JTDoc, false);

    }

}
