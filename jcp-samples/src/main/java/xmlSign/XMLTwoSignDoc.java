/**
 * $RCSfile$
 * version $Revision$
 * created 13.08.2007 14:27:59 by kunina
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
import org.apache.xml.security.transforms.params.XPath2FilterContainer;
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
 * Формирование и проверка двух независимых подписей всего XML-документа для
 * алгоритма ГОСТ Р 34.10-2001.
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class XMLTwoSignDoc {

static {
    if (!XmlInit.isInitialized()) {
        XmlInit.init();
    }
}

 /**/
private XMLTwoSignDoc() {
}

public static void main(String[] args) {

    JCPInit.initProviders(false);

    try {
        //подписываемый xml документ
        String testDoc = "tests.xml";

        //подписанный xml документ
        String signTwo = "XmlTwoDSig.xml";

        // алгоритм ГОСТ Р 34.10-2001 (для генерирования ключевой пары)
        String KeyPairAlgorithm = JCP.GOST_EL_DEGREE_NAME;

        // имя субъекта (оно же издателя) для генерирования первого самоподписанного сертификата
        String certName1 = "CN=newCert1, O=CryptoPro, C=RU";

        // имя субъекта (оно же издателя) для генерирования второго самоподписанного сертификата
        String certName2 = "CN=newCert2, O=CryptoPro, C=RU";

        // идентификатор первого узла подписи
        String Id1 = "FirstSignature";

        // идентификатор второго узла подписи
        String Id2 = "SecondSignature";

        // алгоритм подписи (ГОСТ Р 34.10-2001)
        String signMethod =
                "http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411";

        // алгоритм хеширования, используемый при подписи (ГОСТ Р 34.11-94)
        String digestMethod =
                "http://www.w3.org/2001/04/xmldsig-more#gostr3411";

        /* В первую очередь осуществляет регистрация алгоритма подписи ГОСТ Р 34.10-2001*/
        JCPXMLDSigInit.init();

        Logger.getLogger("LOG").info("two sign doc begin");
        twoSignDoc(KeyPairAlgorithm, JCP.PROVIDER_NAME, JCP.GOST_EL_SIGN_NAME,
            JCP.PROVIDER_NAME, certName1, certName2, Id1, Id2, signMethod,
                digestMethod, testDoc, signTwo);
        Logger.getLogger("LOG").info("two sign doc end\ntwo sign doc verify");
        twoSignDocVer(signTwo);

    } catch (Exception e) {
        e.printStackTrace();
    }

}

/**
 * @param keyPairAlgorithm алгоритм ГОСТ Р 34.10-2001 (для генерирования
 * ключевой пары)
 * @param certName1 имя субъекта (оно же издателя) для генерирования первого
 * самоподписанного сертификата
 * @param certName2 имя субъекта (оно же издателя) для генерирования второго
 * самоподписанного сертификата
 * @param Id1 идентификатор первого узла подписи
 * @param Id2 идентификатор второго узла подписи
 * @param signMethod алгоритм подписи (ГОСТ Р 34.10-2001)
 * @param digestMethod алгоритм хеширования, используемый при подписи (ГОСТ Р
 * 34.11-94)
 * @param testDoc подписываемый документ
 * @param signTwo подписанный документ
 * @throws Exception e
 */
public static void twoSignDoc(String keyPairAlgorithm, String keyProvider,
    String signAlgorithm, String signProvider, String certName1,
    String certName2, String Id1, String Id2, String signMethod,
    String digestMethod, String testDoc, String signTwo) throws Exception {

    /* Генерирование двух ключевых пар в соответствии с которыми будут
    осуществляться две независимые подписи XML-документа*/

    // создание генератора ключевой пары ЭЦП
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(keyPairAlgorithm, keyProvider);

    // генерирование первой ключевой пары
    KeyPair keypair1 = keyGen.generateKeyPair();

    // получение открытого ключа первой ключевой пары
    PublicKey publicKey1 = keypair1.getPublic();

    // получение закрытого ключа первой ключевой пары
    PrivateKey privateKey1 = keypair1.getPrivate();

    // генерирование второй ключевой пары
    KeyPair keypair2 = keyGen.generateKeyPair();

    // получение открытого ключа второй ключевой пары
    PublicKey publicKey2 = keypair2.getPublic();

    // получение закрытого ключа второй ключевой пары
    PrivateKey privateKey2 = keypair2.getPrivate();

    /* Генерирование двух самоподписанных сертфикатов в соответствии с ключевыми парами */

    // создание генератора самоподписанного сертификата
    GostCertificateRequest request1 = new GostCertificateRequest(signProvider);
    GostCertificateRequest request2 = new GostCertificateRequest(signProvider);

    // генерирование первого самоподписанного сертификата, возвращаемого в DER-кодировке
    byte[] encodedCert1 = request1.getEncodedSelfCert(keypair1, certName1, signAlgorithm);

    // инициализация генератора X509-сертификатов
    CertificateFactory cf = CertificateFactory.getInstance("X509");

    // генерирование X509-сертификата из закодированного представления первого сертификата
    X509Certificate cert1 = (X509Certificate) cf
            .generateCertificate(new ByteArrayInputStream(encodedCert1));

    // генерирование второго самоподписанного сертификата, возвращаемого в DER-кодировке
    byte[] encodedCert2 = request2.getEncodedSelfCert(keypair2, certName2, signAlgorithm);

    // генерирование X509-сертификата из закодированного представления второго сертификата
    X509Certificate cert2 = (X509Certificate) cf
            .generateCertificate(new ByteArrayInputStream(encodedCert2));

    /* Загружаем подписываемый XML-документ из файла */

    // инициализация объекта чтения XML-документа
    DocumentBuilderFactory dbf = DocumentBuilderFactoryHelper.newInstance();

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
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

    final Document doc;

    if (!Platform.isAndroid) {
        doc = documentBuilder.parse(testDoc);
    }
    else {
        try (FileInputStream is = new FileInputStream(testDoc)) {
            doc = documentBuilder.parse(is);
        }
    }

    /* Добавление первого узла подписи <ds:Signature> в загруженный XML-документ */

    // инициализация объекта формирования ЭЦП в соответствии с алгоритмом ГОСТ Р 34.10-2001
    XMLSignature sig = new XMLSignature(doc, "", signMethod);

    // определение идентификатора первого узла подписи
    sig.setId(Id1);

    // получение корневого узла XML-документа
    Element anElement = doc.getDocumentElement();

    // добавление в корневой узел XML-документа первого узла подписи
    anElement.appendChild(sig.getElement());

    /* Определение правил работы с XML-документом и добавление в первый узел подписи этих правил */

    // создание узла преобразований <ds:Transforms> обрабатываемого XML-документа
    Transforms transforms = new Transforms(doc);

    // добавление в узел преобразований правил работы с документом
    transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
    transforms.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS);
    String[][] filters = {{XPath2FilterContainer.SUBTRACT, "//ds:Signature"}};
    transforms.addTransform(Transforms.TRANSFORM_XPATH2FILTER,
            XPath2FilterContainer.newInstances(doc, filters));

    // добавление в первый узел подписи ссылок (узла <ds:Reference>), определяющих правила работы с
    // XML-документом (обрабатывается текущий документ с заданными в узле <ds:Transforms> правилами
    // и заданным алгоритмом хеширования)
    sig.addDocument("", transforms, digestMethod);

    /* Создание первой подписи всего содержимого XML-документа на основе закрытого ключа первой ключевой пары,
    заданных правил и алгоритмов */

    // создание внутри первого узла подписи узла <ds:KeyInfo> информации об открытом ключе первой ключевой
    // пары на основе сертификата
    sig.addKeyInfo(cert1);

    // создание первой подписи XML-документа
    sig.sign(privateKey1);

    /* Добавление второго узла подписи <ds:Signature> в загруженный XML-документ */

    // инициализация объекта формирования ЭЦП в соответствии с алгоритмом ГОСТ Р 34.10-2001
    XMLSignature sigSecond = new XMLSignature(doc, "", signMethod);

    // определение идентификатора второго узла подписи
    sigSecond.setId(Id2);

    // добавление в корневой узел XML-документа второго узла подписи
    anElement.appendChild(sigSecond.getElement());

    /* Определение правил работы с XML-документом и добавление в первый узел подписи этих правил */

    // создание узла преобразований <ds:Transforms> обрабатываемого XML-документа
    Transforms transforms2 = new Transforms(doc);

    // добавление в узел преобразований правил работы с документом
    transforms2.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
    transforms2.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS);
    transforms2.addTransform(Transforms.TRANSFORM_XPATH2FILTER,
            XPath2FilterContainer.newInstances(doc, filters));

    // добавление во второй узел подписи ссылок (узла <ds:Reference>), определяющих правила работы с
    // XML-документом (обрабатывается текущий документ с заданными в узле <ds:Transforms> правилами
    // и заданным алгоритмом хеширования)
    sigSecond.addDocument("", transforms2, digestMethod);

    /* Создание второй подписи всего содержимого XML-документа на основе закрытого ключа второй ключевой пары,
    заданных правил и алгоритмов */

    // создание внутри первого узла подписи узла <ds:KeyInfo> информации об открытом ключе второй ключевой
    // пары на основе сертификата
    sigSecond.addKeyInfo(cert2);

    // создание второй подписи XML-документа
    sigSecond.sign(privateKey2);

    /* Сохранение подписанного XML-документа в файл */

    // определение потока, в который осуществляется запись подписанного XML-документа
    FileOutputStream os = new FileOutputStream(signTwo);

    // инициализация объекта копирования содержимого XML-документа в поток
    TransformerFactory tf = TransformerFactoryHelper.newInstance();

    // создание объекта копирования содержимого XML-документа в поток
    Transformer trans = tf.newTransformer();

    // копирование содержимого XML-документа в поток
    trans.transform(new DOMSource(doc), new StreamResult(os));
    os.close();
}

/**
 * проверка двух независимых подписей всего XML-документа для алгоритма ГОСТ Р
 * 34.10-2001.
 *
 * @param signTwo подписанный документ
 * @throws Exception /
 */
public static void twoSignDocVer(String signTwo) throws Exception {
    // инициализация объекта чтения XML-документа
    DocumentBuilderFactory dbf = DocumentBuilderFactoryHelper.newInstance();

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
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();

    /* Загружаем подписанный XML-документ из файла */

    final Document doc;

    if (!Platform.isAndroid) {
        doc = documentBuilder.parse(signTwo);
    }
    else {
        try (FileInputStream is = new FileInputStream(signTwo)) {
            doc = documentBuilder.parse(is);
        }
    }

    /* Чтение узла подписи <ds:Signature> из XML-документа */

    // чтение из загруженного документа содержимого пространства имени Signature
    Element nscontext = doc.createElementNS(null, "namespaceContext");
    nscontext.setAttributeNS("http://www.w3.org/2000/xmlns/",
            "xmlns:" + "ds".trim(), Constants.SignatureSpecNS);

    // определенеи двух узлов подписи
    Element[] sigElement = new Element[2];

    // выбор из прочитанного содержимого пространства имени первого узла подписи <ds:Signature>
    sigElement[0] = (Element) XPathAPI.selectSingleNode(doc,
            "//ds:Signature[@Id='FirstSignature']", nscontext);

    // выбор из прочитанного содержимого пространства имени первого узла подписи <ds:Signature>
    sigElement[1] = (Element) XPathAPI.selectSingleNode(doc,
            "//ds:Signature[@Id='SecondSignature']", nscontext);

    /* Проверка двух подписей XML-документа на основе соответствующей информации об открытом ключе, хранящейся в
   XML-документе */

    // определение двух объектов проверки подписи
    XMLSignature[] signature = new XMLSignature[2];

    // инициализация первого объекта проверки подписи
    signature[0] = new XMLSignature(sigElement[0], "");

    // инициализация второго объекта проверки подписи
    signature[1] = new XMLSignature(sigElement[1], "");

    // процесс последовательной проверки двух подписей
    for (int i = 0; i < 2; i++) {

        // чтение узла <ds:KeyInfo> информации об открытом ключе
        KeyInfo ki = signature[i].getKeyInfo();

        // чтение сертификата их узла информации об открытом ключе
        X509Certificate certKey = ki.getX509Certificate();

        // если сертификат найден, то осуществляется проверка
        // подписи на основе сертфиката
        if (certKey != null) {
            Logger.getLogger("LOG")
                    .info("The XML signature " + (i + 1) + " is " +
                            (signature[i].checkSignatureValue(certKey)
                                    ? "valid (good)" : "invalid (bad)"));
        }
        // в противном случае осуществляется проверка на открытом ключе
        else {
            // чтение открытого ключа из узла информации об открытом ключе
            PublicKey pk = ki.getPublicKey();

            // если открытый ключ найден, то на нем осуществляется проверка подписи
            if (pk != null) {
                Logger.getLogger("LOG")
                        .info("The XML signature " + (i + 1) + " is " +
                                (signature[i].checkSignatureValue(pk)
                                        ? "valid (good)" : "invalid (bad)"));
            }
            // в противном случае проверка не может быть выполнена
            else throw new Exception(
                    "There are no information about public key. Verification couldn't be implemented");

        }
    }

}
}
