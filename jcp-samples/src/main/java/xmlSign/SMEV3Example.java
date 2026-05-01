package xmlSign;

import org.apache.xml.security.Init;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transform;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.utils.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCPxml.Consts;
import ru.CryptoPro.JCPxml.XmlInit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PrivilegedExceptionAction;
import java.security.cert.X509Certificate;

import static xades.util.IXAdESCommon.WORK_DIR;


/**
 * Пример SMEVExample демонстрирует создание и проверку подписи xml-файла средствами wss4j.
 * <br>
 * <br>
 * Полученный xml-файл можно проверить на соответствие схеме СМЭВ3 в онлайн-сервисе
 * {@link "https://lkuv.gosuslugi.ru/paip-portal/#/xml/message/validation"
 * https://lkuv.gosuslugi.ru/paip-portal/#/xml/message/validation}.
 * <br>
 * <br>
 * Пример взят из статьи {@link "https://habr.com/ru/companies/alfa/articles/350158/"
 * https://habr.com/ru/companies/alfa/articles/350158/}.
 */
public class SMEV3Example {

    /**
     * Имя входного файла
     */
    private static final String INPUT_FILE = "SendRequestRequestNoAttach.xml";

    /**
     * Путь к подписанному файлу
     */
    private static final String SIGNED_FILEPATH = WORK_DIR + "SMEV3SignedMessage.xml";

    /**
     * Контейнер с сертификатом
     */
    private static final String SECURITY_ALIAS = "alias0";
    private static final String SECURITY_PASSWORD  = "1";

    /**
     * Логгер
     */
    private static Logger logger = LoggerFactory.getLogger("LOGGER");

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Init.init();
        XmlInit.init();
        JCPInit.initProviders(false);

        //подпись
        Document signedDoc = sign(SECURITY_ALIAS, SECURITY_PASSWORD.toCharArray(), INPUT_FILE);

        // сохранение подписанного документа
        ru.CryptoPro.XAdES.util.XMLUtils.saveXml2File(signedDoc, SIGNED_FILEPATH, true);

        //проверка подписи
        verify(SIGNED_FILEPATH);
    }

    /**
     * Подпись XML-документа из файла.
     * @param alias контейнер для подписи
     * @param password пароль
     * @param inputFile имя входного файла
     * @return подписанный документ
     * @throws Exception
     */
    private static Document sign(String alias, char[] password, String inputFile) throws Exception {

        //Регистрация трансформа СМЭВ3
        Transform.register(SmevTransformSpi.ALGORITHM_URN, SmevTransformSpi.class.getName());

        //В XML-структуре подписи между элементами не допускается наличие текстовых узлов, в том числе переводов строки
        System.setProperty("org.apache.xml.security.ignoreLineBreaks", "true");

        // инициализация объекта чтения XML-документа
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        // установка флага, определяющего игнорирование пробелов в содержимом элементов при обработке XML-документа
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);

        // установка флага, определяющего преобразование узлов CDATA в текстовые узлы при обработке XML-документа
        documentBuilderFactory.setCoalescing(true);

        // установка флага, определяющего поддержку пространств имен при обработке XML-документа
        documentBuilderFactory.setNamespaceAware(true);

        // загрузка содержимого подписываемого документа на основе установленных флагами правил из массива байтов data
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(SMEV3Example.class.getResourceAsStream("/" + inputFile));

        /*
         * Добавление узла подписи <ds:Signature> в загруженный XML-документ
         */

        // инициализация объекта формирования ЭЦП в соответствии с алгоритмом ГОСТ 34.10-2012 (256)
        XMLSignature signature = new XMLSignature(document, "", Consts.URN_GOST_SIGN_2012_256,
                Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

        // определение идентификатора первого узла подписи
        signature.setId("sigID");

        // получение корневого узла XML-документа
        QName QNAME_SIGNATURE = new QName("urn://x-artefacts-smev-gov-ru/services/message-exchange/types/1.1",
                "ns2", "CallerInformationSystemSignature");
        NodeList nodeList = document.getElementsByTagNameNS(QNAME_SIGNATURE.getNamespaceURI(), QNAME_SIGNATURE.getLocalPart());
        Element element = (Element) nodeList.item(0);

        // добавление в корневой узел XML-документа узла подписи
        if(element != null) {
            element.appendChild(signature.getElement());
        } else {
            throw  new Exception();
        }

        /*
         * Определение правил работы с XML-документом и добавление в узел подписи этих правил
         */

        // создание узла преобразований <ds:Transforms> обрабатываемого XML-документа
        Transforms transforms = new Transforms(document);

        // добавление в узел преобразований правил работы с документом
        transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
        transforms.addTransform(SmevTransformSpi.ALGORITHM_URN);

        // добавление в узел подписи ссылок (узла <ds:Reference>), определяющих правила работы с
        // XML-документом (обрабатывается текущий документ с заданными в узле <ds:Transforms> правилами
        // и заданным алгоритмом хеширования)
        signature.addDocument("#SIGNED_BY_CONSUMER", transforms, Consts.URI_GOST_DIGEST);

        /*
         * Создание подписи всего содержимого XML-документа на основе закрытого ключа, заданных правил и алгоритмов
         */

        // Контейнер пользователя.
        KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
        keyStore.load(null, null);
        JCPProtectionParameter parameter = new JCPProtectionParameter(password, true, false);
        JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry)keyStore.getEntry(alias, parameter);
        X509Certificate certificate  = (X509Certificate )entry.getCertificate();
        PrivateKey privateKey = entry.getPrivateKey();

        // создание внутри узла подписи узла <ds:KeyInfo> информации об открытом ключе на основе сертификата
        signature.addKeyInfo(certificate);

        // создание подписи XML-документа
        signature.sign(privateKey);

        // инициализация объекта копирования содержимого XML-документа в поток
        TransformerFactory transformerFactory = TransformerFactory.newInstance();

        // создание объекта копирования содержимого XML-документа в поток
        Transformer transformer = transformerFactory.newTransformer();

        // объект, в который будет записан подписанный XML-документ
        DOMResult domResult = new DOMResult();

        // копирование содержимого XML-документа
        transformer.transform(new DOMSource(document), domResult);

        return (Document) domResult.getNode();
    }

    /**
     * Проверка подписанного документа.
     * @param filepath путь к файлу
     * @return
     * @throws Exception
     */
    private static boolean verify(String filepath) throws Exception {
        boolean coreValidity = true;
        try {
            QName QNAME_SIGNATURE = new QName("http://www.w3.org/2000/09/xmldsig#", "ds", "Signature");

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new File(filepath));

            NodeList sigs = document.getElementsByTagNameNS(QNAME_SIGNATURE.getNamespaceURI(), QNAME_SIGNATURE.getLocalPart());
            XMLSignature signature;
            System.out.println(sigs.getLength());
            sigSearch: {
                for (int i = 0; i < sigs.getLength(); i++) {
                    Element sigElement = (Element) sigs.item(i);
                    String sigId = sigElement.getAttribute("Id");
                    if (sigId != null) {
                        signature = new org.apache.xml.security.signature.XMLSignature(sigElement, "");
                        break sigSearch;
                    }
                }
                throw new Exception("Подпись не найдена.");
            }
            KeyInfo ki = signature.getKeyInfo();

            X509Certificate certificate = ki.getX509Certificate();
            if (!signature.checkSignatureValue(certificate.getPublicKey())) {
                coreValidity = false;
                logger.info("Подпись не валидна.");
            } else {
                logger.info("Подпись валидна.");
            }
        } catch(Exception e) {
            logger.info("Ошибка во время выполнения проверки подписи:", e);
            coreValidity = false;
        }

        return coreValidity;
    }
}
