/**
 * $RCSfile$
 * version $Revision$
 * created 30.12.2004 15:28:45 by avsh
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2005.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import org.w3c.dom.Document;
import ru.xml.tools.DocumentBuilderFactoryHelper;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;
import ru.CryptoPro.JCPxml.Consts;
import ru.xml.tools.TransformerFactoryHelper;
import ru.xml.tools.XmlFeatureHelper;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * В данном примере осуществляется создание подписанного XML-документа в
 * соответствии с алгоритмом ГОСТ Р 34.10-2001/2012.
 */
public class XmlDSigTest {

// Папка для файлов.
private static final String PATH = System.getProperty("user.dir") +
    File.separator + "temp" + File.separator;

/**
 * @param args
 * @throws Exception
 */
public static void main(String[] args) throws Exception {
    JCPInit.initProviders(false);
    // main_(JCP.GOST_EL_DEGREE_NAME, "newCert", JCP.GOST_EL_SIGN_NAME,
    //    Consts.URI_GOST_SIGN, Consts.URI_GOST_DIGEST, "XmlDSigObject.xml");
    main_(JCP.GOST_EL_2012_256_NAME, "newCert_2012_256", JCP.GOST_SIGN_2012_256_NAME,
        Consts.URN_GOST_SIGN_2012_256, Consts.URN_GOST_DIGEST_2012_256, "XmlDSigObject_2012_256.xml");
    main_(JCP.GOST_EL_2012_512_NAME, "newCert_2012_256", JCP.GOST_SIGN_2012_512_NAME,
        Consts.URN_GOST_SIGN_2012_512, Consts.URN_GOST_DIGEST_2012_512, "XmlDSigObject_2012_512.xml");
}

/**
 * Подпись XML-документа.
 *
 * @param keyAlg Алгоритм ключа.
 * @param dnName Имя сертификата.
 * @param signAlg Алгоритм подписи.
 * @param xmlSigAlg Алгоритм подписи для включения в документ.
 * @param xmlDigestAlg Алгоритм включения для включения в документ.
 * @param outFileName Имя файла для сохранения подписанного документа.
 * @throws Exception
 */
public static void main_(String keyAlg, String dnName,
    String signAlg, String xmlSigAlg, String xmlDigestAlg, String outFileName)
    throws Exception {

    System.out.println("Example of signing XML document:");
    String KeyPairAlgorithm = keyAlg;
    // генерирование ключевой пары
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KeyPairAlgorithm);
    java.security.KeyPair pair = keyGen.generateKeyPair();
    PrivateKey privKey = pair.getPrivate();
    System.out.println("Key pair is generated (" + keyAlg + ")");
    // генерирование сертификата
    GostCertificateRequest gr = new GostCertificateRequest();
    byte[] enc = gr.getEncodedSelfCert(pair, "CN=" + dnName +
        ", O=CryptoPro, C=RU", signAlg);
    CertificateFactory cf = CertificateFactory.getInstance("X509");
    X509Certificate cert = (X509Certificate) cf
        .generateCertificate(new ByteArrayInputStream(enc));

    System.out.println("Certificate is generated");
    //создание пустого XML документа
    DocumentBuilderFactory dbFactory = DocumentBuilderFactoryHelper.newInstance();
    dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    dbFactory.setNamespaceAware(true);

    //создаем объект подписывающий XML документы
    XWSXmlDSig xmldsig = new XWSXmlDSig();
    //передаем закрытый ключ созданному объекту
    xmldsig.setPrivateKey(privKey);
    //передаем созданному объеку извлеченный сертификат
    xmldsig.SetCertificate(cert);
    xmldsig.setSignMethod(xmlSigAlg);
    xmldsig.setdigestMethod(xmlDigestAlg);

    //подписываем созданный документ
    Document doc = xmldsig.SignObject();
    //печатаем подписанный документ в файл
    OutputStream os;
    os = new FileOutputStream(PATH + outFileName);

    TransformerFactory tf = TransformerFactoryHelper.newInstance();
    Transformer trans = tf.newTransformer();
    trans.transform(new DOMSource(doc), new StreamResult(os));
    os.close();

    //загружаем подписанный документ из файла и проверяем его подпись
    doc = dbFactory.newDocumentBuilder().parse(PATH + outFileName);
    boolean res = XWSXmlDSig.Validate(doc);
    if (res) {
        System.out.println("Test for sign and verify object of XML document is passed");
        System.out.println();
    }
}
}



