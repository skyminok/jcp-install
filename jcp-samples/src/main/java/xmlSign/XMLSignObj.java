/**
 * $RCSfile$
 * version $Revision$
 * created 13.08.2007 14:07:04 by kunina
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
import org.apache.xml.security.signature.ObjectContainer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.xml.tools.DocumentBuilderFactoryHelper;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Platform;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;
import ru.CryptoPro.JCPxml.XmlInit;
import ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit;
import ru.xml.tools.TransformerFactoryHelper;
import ru.xml.tools.XmlFeatureHelper;

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
 * Формирование и проверка подписи объекта XML-документа для алгоритма ГОСТ Р
 * 34.10-2001.
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XMLSignObj {

static {
    if (!XmlInit.isInitialized()) {
        XmlInit.init();
    }
}

 /**/
private XMLSignObj() {
}

public static void main(String[] args) {

    JCPInit.initProviders(false);

    try {
        //подписанный xml документ
        final String signObj = "XmlDSigObject.xml";

        // алгоритм ГОСТ Р 34.10-2001 (для генерирования ключевой пары)
        final String KeyPairAlgorithm = JCP.GOST_EL_DEGREE_NAME;

        // имя субъекта (оно же издателя) для генерирования самоподписанного сертификата
        final String certName = "CN=newCert, O=CryptoPro, C=RU";

        // идентификатор подписываемого объекта
        final String Id = "TheFirstObject";

        // алгоритм подписи (ГОСТ Р 34.10-2001)
        final String signMethod =
                "http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411";

        // алгоритм хеширования, используемый при подписи (ГОСТ Р 34.11-94)
        final String digestMethod =
                "http://www.w3.org/2001/04/xmldsig-more#gostr3411";

        /* В первую очередь осуществляет регистрация алгоритма подписи ГОСТ Р 34.10-2001*/
        JCPXMLDSigInit.init();

        Logger.getLogger("LOG").info("sign obj begin");
        signObj(KeyPairAlgorithm, JCP.PROVIDER_NAME, JCP.GOST_EL_SIGN_NAME,
            JCP.PROVIDER_NAME, certName, Id, signMethod, digestMethod, signObj);
        Logger.getLogger("LOG").info("sign obj end\nsign obj verify");
        signObjVer(signObj);

    } catch (Exception e) {
        e.printStackTrace();
    }


}

/**
 * Формирование подписи объекта XML-документа для алгоритма ГОСТ Р 34.10-2001.
 *
 * @param keyPairAlgorithm алгоритм ГОСТ Р 34.10-2001 (для генерирования ключевой
 * пары)
 * @param certName имя субъекта (оно же издателя) для генерирования самоподписанного
 * сертификата
 * @param id идентификатор подписываемого объекта
 * @param signMethod алгоритм подписи (ГОСТ Р 34.10-2001)
 * @param digestMethod алгоритм хеширования, используемый при подписи (ГОСТ Р
 * 34.11-94)
 * @param signObj подписанный документ
 * @throws Exception e
 */
public static void signObj(String keyPairAlgorithm, String keyProvider, String signAlgorithm,
     String signProvider, String certName, String id, String signMethod, String digestMethod,
     String signObj) throws Exception {

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

/* Создание нового (пустого) XML-документа */

    // инициализация объекта создания XML-документа
    final DocumentBuilderFactory dbf = DocumentBuilderFactoryHelper.newInstance();

    // установка запрета на external entities
    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

    // установка флага, определяющего поддержку пространств имен при обработке XML-документа
    dbf.setNamespaceAware(true);

    // создание нового (пустого) XML-документа
    final DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    final Document doc = documentBuilder.newDocument();

/* Добавление узла подписи <ds:Signature> в созданный XML-документ */

    // инициализация объекта формирования ЭЦП в соответствии с алгоритмом ГОСТ Р 34.10-2001
    final XMLSignature sig = new XMLSignature(doc, "", signMethod);

    // добавление узла подписи в пустой XML-документ
    doc.appendChild(sig.getElement());

/* Создание внутри узла подписи подписываемого объекта */

    // создание узла объекта <ds:Object> в созданном XML-документе (в узле подписи, поскольку документ
    // состоит только из этого узла)
    final ObjectContainer obj = new ObjectContainer(doc);

    // создание подписываемого объекта с идентификатором "InsideObject"
    final Element anElement = doc.createElement("InsideObject");

    // создание текста для объекта с идентификатором "InsideObject"
    anElement.appendChild(doc.createTextNode("A text in a box"));

    // добавление объекта в узел объекта
    obj.appendChild(anElement);

    // определение идентификатора узла объекта
    obj.setId(id);

    // добавление в узел подписи созданного узла объекта
    sig.appendObject(obj);

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

/* Создание подписи объекта XML-документа на основе закрытого ключа, заданных правил и алгоритмов */

    // создание внутри узла подписи узла <ds:KeyInfo> информации об открытом ключе на основе
    // сертификата
    sig.addKeyInfo(cert);

    // создание подписи объекта XML-документа
    sig.sign(privateKey);

/* Сохранение подписанного XML-документа (а точнее объекта XML-документа) в файл */

    // определение потока, в который осуществляется запись подписанного XML-документа
    final FileOutputStream os = new FileOutputStream(signObj);

    // инициализация объекта копирования содержимого XML-документа в поток
    final TransformerFactory tf = TransformerFactoryHelper.newInstance();

    // создание объекта копирования содержимого XML-документа в поток
    final Transformer trans = tf.newTransformer();

    // копирование содержимого XML-документа в поток
    trans.transform(new DOMSource(doc), new StreamResult(os));
    os.close();
}

/**
 * проверка подписи объекта XML-документа для алгоритма ГОСТ Р 34.10-2001.
 *
 * @param signObj подписанный документ
 * @throws Exception /
 */
public static void signObjVer(String signObj) throws Exception {
    // инициализация объекта создания XML-документа
    final DocumentBuilderFactory dbf = DocumentBuilderFactoryHelper.newInstance();

    // установка запрета на external entities
    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

    // установка флага, определяющего поддержку пространств имен при обработке XML-документа
    dbf.setNamespaceAware(true);

/* Загружаем подписанный XML-документ (а точнее подписанный объект XML-документа) из файла */

    final DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

    final Document doc;

    if (!Platform.isAndroid) {
        doc = documentBuilder.parse(signObj);
    }
    else {
        try (FileInputStream is = new FileInputStream(signObj)) {
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

/* Проверка подписи объекта XML-документа на основе информации об открытом ключе, хранящейся в
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
        else throw new Exception(
                "There are no information about public key. Verification couldn't be implemented");
    }
}
}
