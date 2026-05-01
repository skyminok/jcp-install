/**
 * $RCSfile$
 * version $Revision$
 * created 13.08.2007 14:09:47 by kunina
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2007.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован 
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package xmlSign;

import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.xml.tools.DocumentBuilderFactoryHelper;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.tools.Platform;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;
import ru.CryptoPro.JCPxml.XmlInit;
import ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.xml.tools.TransformerFactoryHelper;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

/**
 * Формирование и проверка подписи всего XML-документа для алгоритма ГОСТ Р
 * 34.10-2001.
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XMLSignDoc {

static {
    if (!XmlInit.isInitialized()) {
        XmlInit.init();
    }
}

 /**/
private XMLSignDoc() {
}

public static void main(String[] args) {

    JCPInit.initProviders(false);

    try {
        //подписываемый xml документ
        final String testDoc = "tests.xml";

        //подписанный xml документ
        final String signDoc = "XmlDSigDocument.xml";

        // алгоритм ГОСТ Р 34.10-2001 (для генерирования ключевой пары)
        final String KeyPairAlgorithm = JCP.GOST_EL_DEGREE_NAME;

        // имя субъекта (оно же издателя) для генерирования самоподписанного сертификата
        final String certName = "CN=newCert, O=CryptoPro, C=RU";

        // алгоритм подписи (ГОСТ Р 34.10-2001)
        final String signMethod =
                "http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411";

        // алгоритм хеширования, используемый при подписи (ГОСТ Р 34.11-94)
        final String digestMethod =
                "http://www.w3.org/2001/04/xmldsig-more#gostr3411";

        /* В первую очередь осуществляет регистрация алгоритма подписи ГОСТ Р 34.10-2001*/
        JCPXMLDSigInit.init();

        Logger.getLogger("LOG").info("sign doc begin");
        signDoc(KeyPairAlgorithm, JCP.PROVIDER_NAME, JCP.GOST_EL_SIGN_NAME,
            JCP.PROVIDER_NAME, certName, signMethod, digestMethod, testDoc, signDoc);
        Logger.getLogger("LOG").info("sign doc end\nsign doc verify");
        signDocVer(signDoc);

    } catch (Exception e) {
        e.printStackTrace();
    }
}

/**
 * Формирование подписи всего XML-документа для алгоритма ГОСТ Р 34.10-2001.
 *
 * @param keyPairAlgorithm алгоритм ГОСТ Р 34.10-2001 (для генерирования ключевой
 * пары)
 * @param certName имя субъекта (оно же издателя) для генерирования самоподписанного
 * сертификата
 * @param signMethod алгоритм подписи (ГОСТ Р 34.10-2001)
 * @param digestMethod алгоритм хеширования, используемый при подписи (ГОСТ Р
 * 34.11-94)
 * @param testDoc подписываемый документ
 * @param signDoc подписанный документ
 * @throws Exception e
 */
public static void signDoc(String keyPairAlgorithm, String keyProvider,
    String signAlgorithm, String signProvider, String certName,
    String signMethod, String digestMethod,String testDoc, String signDoc)
    throws Exception {

    /* Генерирование ключевой пары в соответствии с которой будет осуществлять подпись XML-документа*/

    // создание генератора ключевой пары ЭЦП
    final KeyPairGenerator keyGen =
            KeyPairGenerator.getInstance(keyPairAlgorithm, keyProvider);

    // генерирование ключевой пары
    final KeyPair keypair = keyGen.generateKeyPair();

    // получение открытого ключа
    final PublicKey publicKey = keypair.getPublic();

    // получение закрытого ключа
    final PrivateKey privateKey = keypair.getPrivate();

    /* Генерирование самоподписанного сертфиката в соответствии с ключевой парой*/

    // создание генератора самоподписанного сертификата
    final GostCertificateRequest request = new GostCertificateRequest(signProvider);

    // генерирование самоподписанного сертификата, возвращаемого в DER-кодировке
    final byte[] encodedCert = request.getEncodedSelfCert(keypair, certName, signAlgorithm);

    // инициализация генератора X509-сертификатов
    final CertificateFactory cf = CertificateFactory.getInstance("X509");

    // генерирование X509-сертификата из закодированного представления сертификата
    final X509Certificate cert = (X509Certificate) cf
            .generateCertificate(new ByteArrayInputStream(encodedCert));

    /* Загружаем подписываемый XML-документ из файла */

    // инициализация объекта чтения XML-документа
    final DocumentBuilderFactory dbf = DocumentBuilderFactoryHelper.newInstance();

    // установка запрета на external entities
    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

    // установка флага, определяющего игнорирование пробелов в содержимом элементов при обработке XML-документа
    dbf.setIgnoringElementContentWhitespace(true);

    // установка флага, определяющего преобразование узлов CDATA в текстовые узлы при обработке XML-документа
    dbf.setCoalescing(true);

    // установка флага, определяющего поддержку пространств имен при обработке XML-документа
    dbf.setNamespaceAware(true);

    // загрузка содержимого подписываемого документа на основе установленных флагами правил
    final DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    final Document doc;

    if (!Platform.isAndroid) {
        doc = documentBuilder.parse(testDoc);
    }
    else {
        try (FileInputStream is = new FileInputStream(testDoc)) {
            doc = documentBuilder.parse(is);
        }
    }

    /* Добавление узла подписи <ds:Signature> в загруженный XML-документ */

    // инициализация объекта формирования ЭЦП в соответствии с алгоритмом ГОСТ Р 34.10-2001
    final XMLSignature sig = new XMLSignature(doc, "", signMethod);

    // получение корневого узла XML-документа
    final Element anElement = doc.getDocumentElement();

    // добавление в корневой узел XML-документа узла подписи
    anElement.appendChild(sig.getElement());

    /* Определение правил работы с XML-документом и добавление в узел подписи этих правил */

    // создание узла преобразований <ds:Transforms> обрабатываемого XML-документа
    final Transforms transforms = new Transforms(doc);

    // добавление в узел преобразований правил работы с документом
    transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
    transforms.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS);

    // добавление в узел подписи ссылок (узла <ds:Reference>), определяющих правила работы с
    // XML-документом (обрабатывается текущий документ с заданными в узле <ds:Transforms> правилами
    // и заданным алгоритмом хеширования)
    sig.addDocument("", transforms, digestMethod);

    /* Создание подписи всего содержимого XML-документа на основе закрытого ключа, заданных правил и алгоритмов */

    // создание внутри узла подписи узла <ds:KeyInfo> информации об открытом ключе на основе
    // сертификата
    sig.addKeyInfo(cert);

    // создание подписи XML-документа
    sig.sign(privateKey);

    /* Сохранение подписанного XML-документа в файл */

    // определение потока, в который осуществляется запись подписанного XML-документа
    final FileOutputStream os = new FileOutputStream(signDoc);

    // инициализация объекта копирования содержимого XML-документа в поток
    final TransformerFactory tf = TransformerFactoryHelper.newInstance();

    // создание объекта копирования содержимого XML-документа в поток
    final Transformer trans = tf.newTransformer();

    // копирование содержимого XML-документа в поток
    trans.transform(new DOMSource(doc), new StreamResult(os));
    os.close();
}

/**
 * Проверка подписи всего XML-документа для алгоритма ГОСТ Р 34.10-2001.
 *
 * @param signDoc подписанный документ
 * @throws Exception /
 */
public static void signDocVer(String signDoc) throws Exception {

    // инициализация объекта чтения XML-документа
    final DocumentBuilderFactory dbf = DocumentBuilderFactoryHelper.newInstance();

    // установка запрета на external entities
    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

    // установка флага, определяющего игнорирование пробелов в содержимом элементов при обработке XML-документа
    dbf.setIgnoringElementContentWhitespace(true);

    // установка флага, определяющего преобразование узлов CDATA в текстовые узлы при обработке XML-документа
    dbf.setCoalescing(true);

    // установка флага, определяющего поддержку пространств имен при обработке XML-документа
    dbf.setNamespaceAware(true);

    // загрузка содержимого подписываемого документа на основе установленных флагами правил
    final DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

    /* Загружаем подписанный XML-документ из файла */

    final Document doc;

    if (!Platform.isAndroid) {
        doc = documentBuilder.parse(signDoc);
    }
    else {
        try (FileInputStream is = new FileInputStream(signDoc)) {
            doc = documentBuilder.parse(is);
        }
    }

    /* Чтение узла подписи <ds:Signature> из XML-документа */

    // чтение из загруженного документа содержимого пространства имени Signature
    final Element nscontext = doc.createElementNS(null, "namespaceContext");
    nscontext.setAttributeNS("http://www.w3.org/2000/xmlns/",
            "xmlns:" + "ds".trim(), Constants.SignatureSpecNS);

    // выбор из прочитанного содержимого пространства имени узла подписи <ds:Signature>
    final Element sigElement = (Element) XPathAPI
            .selectSingleNode(doc, "//ds:Signature[1]", nscontext);

    /* Проверка подписи XML-документа на основе информации об открытом ключе, хранящейся в
    XML-документе */

    // инициализация объекта проверки подписи
    final XMLSignature signature = new XMLSignature(sigElement, "");

    // чтение узла <ds:KeyInfo> информации об открытом ключе
    final KeyInfo ki = signature.getKeyInfo();

    // чтение сертификата их узла информации об открытом ключе
    final X509Certificate certKey = ki.getX509Certificate();

    // если сертификат найден, то осуществляется проверка
    // подписи на основе сертфиката
    if (certKey != null) {
        Logger.getLogger("LOG").info("The XML signature  is " +
                (signature.checkSignatureValue(certKey)
                        ? "valid (good)" : "invalid (bad)"));

    }
    // в противном случае осуществляется проверка на открытом ключе
    else {
        // чтение открытого ключа из узла информации об открытом ключе
        final PublicKey pk = ki.getPublicKey();

        // если открытый ключ найден, то на нем осуществляется проверка подписи
        if (pk != null) {
            Logger.getLogger("LOG").info(
                    "The XML signature is " + (signature.checkSignatureValue(pk)
                            ? "valid (good)" : "invalid (bad)"));
        }
        // в противном случае проверка не может быть выполнена
        else
            throw new Exception(
                    "There are no information about public key. Verification couldn't be implemented");

    }
}
}
