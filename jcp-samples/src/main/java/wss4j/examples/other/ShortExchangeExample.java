/**
 * Copyright 2004-2012 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package wss4j.examples.other;

import org.apache.axis.Message;
import org.apache.axis.message.PrefixedQName;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.ws.security.*;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.util.Base64;
import org.apache.ws.security.util.UUIDGenerator;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.Crypto.Key.GostSecretKey;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Key.SecretKeySpec;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit;
import wss4j.examples.other.hack.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Пример короткого обмена сообщениями java-клиента (wss4j 1.6.3) и
 * .net-сервиса (без security context).
 */
public class ShortExchangeExample {

    /**
     * Callback для подписи сообщения и проверки локально, как
     * будто мы и клиент, и сервис.
     */
    public static CallbackHandler storeCallbackHandler =
        new CallbackHandlers.KeyStoreCallbackHandler();
    /**
     * Callback для проверки ответного сообщения сервиса. В callback'е
     * хранятся пары [encrypted key identifier, secret key spec] для того,
     * чтобы при получениии из derived key token по хешу wrapped key
     * (EncryptedKey будет отсутствовать в ответном сообщении) можно было
     * выбрать секретный ключ, из которого был получен данный derived key.
     */
    public  static CallbackHandler keyCallbackHandler =
        new CallbackHandlers.SecretKeyAndKeyStoreCallbackHandler();
    /**
     * Криптопровайдер хеширования и подписи.
     */
    public static Provider xmlDSigRi = null;
    /**
     * Объект для работы с контейнерами и т.п.
     */
    public static Crypto crypto = null;
    /**
     * Addressing, который есть в cxf, но у нас просто wss4j. Используется
     * при добавлении элементов типа Action, To, Reply, MessageID.
     */
    public static final String ACTION_NS = "http://www.w3.org/2005/08/addressing";
    /**
     * Фабрика формирования документов.
     */
    public static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    /**
     * Имя для http подключения.
     */
    public static final String AGENT_NAME =
        "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)";
    /**
     * Алиса сертификата (допустим, для упрощения у нас есть
     * контейнер сервиса, из которого мы будем получать сертификат).
     * Этот сертификат установлен на сервере и привязан к ключу.
     */
    public static final String SERVICE_ALIAS = "localhost";
    /**
     * Пароль для доступа к ключу клиента. (допустим, для упрощения
     * у нас есть контейнер сервиса, из которого мы будем получать
     * закрытый ключ). Может использоваться для отладки при локальной
     * проверке.
     */
    public static final String SERVICE_PASSWORD = "Pass1234";
    /**
     * Алиас сертификата и ключа клиента в контейнере клиента.
     */
    public static final String CLIENT_ALIAS = "gost_exch";
    /**
     * Пароль для доступа к ключу клиента.
     */
    public static final String CLIENT_PASSWORD = SERVICE_PASSWORD;
    /**
     * Папка для сохранения файлов.
     */
    public static final String TEST_DIR = "C:\\";
    /**
     * Адрес хоста с сервисом.
     */
    public static final String REMOTE_HOST = "http://192.168.111.228";
    /**
     * Адрес .net сервиса.
     */
    public static final String REMOTE_SERVICE = "/WSS4J_WCF_Sample/Service.svc";

    static {

        /**
         * Инициализация класса.
         */

        com.sun.org.apache.xml.internal.security.Init.init();

        // Инициализация JCP XML провайдера.
        if(!JCPXMLDSigInit.isInitialized()) {
            JCPXMLDSigInit.init();
        } // if

        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        factory.setNamespaceAware(true);

        // Загрузка провайдера.
        Security.insertProviderAt(new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI(), 1);

        // Переопределяем свойства встроенного XMLDSig.
        Security.getProvider("XMLDSig").put("XMLSignatureFactory.DOM",
            "ru.CryptoPro.JCPxml.dsig.internal.dom.DOMXMLSignatureFactory");
        Security.getProvider("XMLDSig").put("KeyInfoFactory.DOM",
            "ru.CryptoPro.JCPxml.dsig.internal.dom.DOMKeyInfoFactory");

        KeyStore keyStore = null;

        try {
            // Загружаем контейнер.
            keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
            keyStore.load(null, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Merlin merlin = new Merlin();
        merlin.setKeyStore(keyStore);

        crypto = merlin;

        ((CallbackHandlers.KeyStoreCallbackHandler)storeCallbackHandler)
            .addUser(CLIENT_ALIAS, CLIENT_PASSWORD);
        ((CallbackHandlers.KeyStoreCallbackHandler)storeCallbackHandler)
            .addUser(SERVICE_ALIAS, SERVICE_PASSWORD);
    }

    /**
     * Вывод содержимого документа в файл.
     *
     * @param doc XML документ.
     * @param outFileName Имя файла для сохранения документа.
     * @throws Exception
     */
    public static void saveXml2File(Document doc, String outFileName,
        boolean debug) throws Exception {

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        StreamResult fileResult = new StreamResult(new File(outFileName));
        transformer.transform(source, fileResult);

        // Вывод на экран, если необходимо.
        if (debug) {
            StreamResult consoleResult = new StreamResult(System.out);
            transformer.transform(source, consoleResult);
        } // if
    }

    /**
     * Отправка запроса по адресу httpAddress и получение ответа.
     *
     * @param httpAddress Адрес назначения (хост).
     * @param requestUrl URL сервиса.
     * @param data Строка с документом.
     * @return ответ сервиса в виде строки.
     * @throws IOException
     */
    public static String getHttpPostFile(String httpAddress,
       String requestUrl, String data) throws IOException {

        URL url = new URL(httpAddress + requestUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        System.out.println("URL: " + url);

        connection.setRequestProperty("User-Agent", AGENT_NAME);
        connection.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
        connection.setRequestProperty("Content-length", String.valueOf(data.length()));
        connection.setUseCaches(false);

        connection.connect();

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(data.getBytes("UTF-8"));
        outputStream.close();

        InputStream inputStream = null;

        int responseCode = connection.getResponseCode();
        System.out.println("Response: " + responseCode + ", " + connection.getResponseMessage());

        if (responseCode == 200) {
            inputStream = connection.getInputStream();
        }
        else {
            inputStream = connection.getErrorStream();
        }

        if (inputStream == null) {
            throw new IOException("Server has returned an empty output stream.");
        } // if

        String logBuffer = "";
        String content = "";
        String inputLine = "";

        BufferedReader in =
            new BufferedReader(new InputStreamReader(inputStream, "windows-1251"));

        while ((inputLine = in.readLine()) != null) {
            content += inputLine;
            logBuffer += inputLine + "\r\n";
        }

        in.close();
        System.out.println(logBuffer);

        if (responseCode != 200) {
            throw new IOException("Server has returned an invalid http" +
                " code: " + responseCode);
        } // if

        return content;
    }

    /**
     * Создания образца документа для отправки сервису.
     *
     * @return сформированный XML документ.
     * @throws Exception
     */
    public static SOAPEnvelope createEnvelope(String number) throws Exception {

        SOAPEnvelope env = new SOAPEnvelope(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);

        // String body =
        //        "<GetData xmlns=\"http://tempuri.org/\">"
        //    +       "<value>3</value>"
        //    +   "</GetData>";

        Name getData = new PrefixedQName("http://tempuri.org/", "GetData", "");

        SOAPBodyElement bodyElement = new SOAPBodyElement(getData);
        SOAPElement value = bodyElement.addChildElement("value");
        value.addTextNode(number);

        env.addBodyElement(bodyElement);

        return env;
    }

    /**
     * Чтение XML документа из файла и конвертация в SOAPEnvelope.
     * @param fileName Имя файла документа.
     * @return сформированный XML документ.
     * @throws Exception
     */
    public static SOAPEnvelope getSOAPEnvelopeFromFile(String fileName) throws Exception {
        try (InputStream input = new FileInputStream( new File(fileName) )) {
            Message msg = new Message(input);
            return msg.getSOAPEnvelope();
        }
    }

    /**
     * @param args
     * @throws
     * @throws Exception
     */
    public static void mymain(String[] args) throws Exception {

        //##############################################################################################################
        // 1. Общие приготовления - формирование документа, добавление
        // различных элементов, генерация ключа клиента.

        // Конфигурация для проверки документов
        WSSConfig config = new WSSConfig();
        config.setWsiBSPCompliant(false);

        // Переопределяем обработчики токенов
        QName el1 = new QName("http://www.w3.org/2001/04/xmlenc#", "EncryptedKey");
        QName el2 = new QName("http://schemas.xmlsoap.org/ws/2005/02/sc", "DerivedKeyToken");
        QName el3 = new QName("http://www.w3.org/2001/04/xmlenc#", "ReferenceList");
        QName el4 = new QName("http://www.w3.org/2000/09/xmldsig#", "Signature");
        config.setProcessor(el1, new MyEncryptedKeyProcessor());
        config.setProcessor(el2, new MyDerivedKeyTokenProcessor());
        config.setProcessor(el3, new MyReferenceListProcessor());
        config.setProcessor(el4, new MySignatureProcessor());

        // Ссылка на Body в пространстве SOAP12.
        final WSEncryptionPart encP = new WSEncryptionPart(
            WSConstants.ELEM_BODY, WSConstants.URI_SOAP12_ENV,
            "Content");

        // Пространство имен wsu (security-utility).
        final Name securityUtilityNamespace = new PrefixedQName(WSConstants.XMLNS_NS, "wssu", "xmlns");

        // А) Создаем секретный ключ клиента. В дальнейшем он будет
        // закеширован. Надо указать провайдер, т.к. при последующих вызовах
        // может вмешаться BC.

        KeyGenerator kg = KeyGenerator.getInstance(CryptoProvider.GOST_CIPHER_NAME,
            CryptoProvider.PROVIDER_NAME);
        SecretKey secretKey = kg.generateKey();

        // Б) Готовим простой документ без хидера, только с телом,
        // подходящим для отправки сервису.

        SOAPEnvelope envelope = createEnvelope("3");
        SOAPHeader header = envelope.getHeader();

        // Г) Добавление узла a:Action
        // Образец:
        // <a:Action s:mustUnderstand="1" u:Id="_5"
        // xmlns:u="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
        // xmlns:a="http://www.w3.org/2005/08/addressing">http://tempuri.org/IService/GetData</a:Action>

        SOAPHeaderElement headerActionElement =
            header.addHeaderElement(envelope.createName("Action", "a", ACTION_NS));

        headerActionElement.setMustUnderstand(true);
        headerActionElement.addAttribute(securityUtilityNamespace, WSConstants.WSU_NS);
        headerActionElement.setActor(null);
        headerActionElement.addTextNode("http://tempuri.org/IService/GetData");

        // Д) Добавление узла a:MessageID
        // Образец:
        // <a:MessageID u:Id="_6" xmlns:u="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
        // xmlns:a="http://www.w3.org/2005/08/addressing">urn:uuid:a83ad5cb-96a6-441c-b1c8-6500d23459c5</a:MessageID>

        SOAPHeaderElement headerMessageIDElement =
            header.addHeaderElement(envelope.createName("MessageID", "a", ACTION_NS));

        headerMessageIDElement.addAttribute(securityUtilityNamespace, WSConstants.WSU_NS);
        headerMessageIDElement.setActor(null);

        // TODO почему-то не срабатывает, хотя удаляет.
        //Name name = new PrefixedQName("http://schemas.xmlsoap.org/soap/envelope/",
        //    "mustUnderstand", "");
        //headerMessageIDElement.removeAttribute(name);

        headerMessageIDElement.addTextNode("uuid:" + UUIDGenerator.getUUID());

        // Е) Добавление узла a:ReplyTo (в документе
        // записан в одну строку, а то хеш не сходится)
        // Образец:
        // <a:ReplyTo u:Id="_7" xmlns:u="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
        // xmlns:a="http://www.w3.org/2005/08/addressing"><a:Address>http://www.w3.org/2005/08/addressing/anonymous
        // </a:Address></a:ReplyTo>

        SOAPHeaderElement headerReplyToElement =
            header.addHeaderElement(envelope.createName("ReplyTo", "a", ACTION_NS));
        headerReplyToElement.addAttribute(securityUtilityNamespace, WSConstants.WSU_NS);
        headerReplyToElement.setActor(null);

        SOAPElement replyToAddressElement = headerReplyToElement.addChildElement("Address", "a");
        replyToAddressElement.addTextNode(ACTION_NS + "/anonymous");

        // Ж) Добавление узла a:To
        // Образец:
        // <a:To s:mustUnderstand="1" u:Id="_8" xmlns:u="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd"
        // xmlns:a="http://www.w3.org/2005/08/addressing">http://localhost/WSS4J_WCF_Sample/Service.svc</a:To>

        SOAPHeaderElement headerToElement =
            header.addHeaderElement(envelope.createName("To", "a", ACTION_NS));

        headerToElement.addAttribute(securityUtilityNamespace, WSConstants.WSU_NS);
        headerToElement.setActor(null);
        headerToElement.setMustUnderstand(true);

        headerToElement.addTextNode("http://localhost/WSS4J_WCF_Sample/Service.svc");

        // З) Добавление Security в документ

        Document doc = envelope.getAsDocument();

        // Выведем элемент Body сообщения клиента
        Element clientBodyElement = WSSecurityUtil
            .findElement(doc.getDocumentElement(),
                    WSConstants.ELEM_BODY, WSConstants.URI_SOAP12_ENV);

        System.out.println("Запрос к отправке: ");
        org.apache.ws.security.util.XMLUtils.ElementToStream(clientBodyElement, System.out);
        System.out.println();

        WSSecHeader secHeader = new WSSecHeader();
        secHeader.setMustUnderstand(true);
        secHeader.insertSecurityHeader(doc);

        // И) Добавление штампа времени Timestamp в хидер (в документе
        // записан в одну строку, а то хеш не сходится)
        // Образец:
        // <u:Timestamp u:Id="uuid-6ca38d29-a750-4d05-a6ea-cd287be2bdb9-2"
        // xmlns:u="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
        // <u:Created>2013-02-26T08:49:51.376Z</u:Created>
        // <u:Expires>2013-02-26T08:54:51.376Z</u:Expires>
        // </u:Timestamp>

        WSSecTimestamp timestamp = new WSSecTimestamp();
        timestamp.setTimeToLive(300);

        // Документ с проставленным штампом времени.
        Document timestampedDoc = timestamp.build(doc, secHeader);
        timestamp.getElement().setAttributeNS(WSConstants.XMLNS_NS, "xmlns:wsu", WSConstants.WSU_NS);

        // saveXml2File(timestampedDoc, "C:\\timestamped.xml", false);

        //##############################################################################################################
        // 2. Добавляем элемент EncryptedKey в документ. Это
        // симметричный секретный ключ клиента S, зашифрованный
        // на ключе согласования A, полученном из одномоментно
        // сгенерированной пары эфемерных ключей и открытого ключа
        // получателя. При расшифровке на стороне получателя будет
        // использоваться закрытый ключ получателя.

        WSSecEncryptedKey encrKeyBuilder = new WSSecEncryptedKey();

        // Алиас сертификата получателя.
        encrKeyBuilder.setUserInfo(SERVICE_ALIAS);
        encrKeyBuilder.setSymmetricKey(secretKey);

        // Транспортный алгоритм.
        encrKeyBuilder.setKeyEncAlgo("urn:ietf:params:xml:ns:cpxmlsec:algorithms:transport-gost2001");
        // Алгоритм шифрования ключа.
        encrKeyBuilder.setSymmetricEncAlgorithm("urn:ietf:params:xml:ns:cpxmlsec:algorithms:gost28147");
        // Используем отпечаток для поиска сертификата.
        encrKeyBuilder.setKeyIdentifierType(WSConstants.THUMBPRINT_IDENTIFIER);

        // Готовим EncryptedKey по документу с проставленным
        // штампом времени.
        encrKeyBuilder.prepare(timestampedDoc, crypto);

        // Идентификатор wrapped key для ссылки на него из DerivedKeyToken.
        String tokenEncryptedKeyIdentifier = encrKeyBuilder.getId();

        // #############################################################################################################
        // 3. Добавление DerivedKeyToken со ссылкой на EncryptedKey
        // (tokenEncryptedKeyIdentifier). Этот derived key будет
        // использоваться для подписи на симметричном ключе на
        // алгоритме hmac-gostr3411. Подпись содержит 6 хешей различных
        // элементов, в том числе и Body.

        MyWSSecDKSign sigSymBuilder = new MyWSSecDKSign();
        sigSymBuilder.setExternalKey(secretKey, tokenEncryptedKeyIdentifier);
        sigSymBuilder.setDigestAlgorithm("urn:ietf:params:xml:ns:cpxmlsec:algorithms:gostr3411");
        sigSymBuilder.setSignatureAlgorithm("urn:ietf:params:xml:ns:cpxmlsec:algorithms:hmac-gostr3411");
        sigSymBuilder.setCustomValueType("http://docs.oasis-open.org/wss/oasis-wss-soap-message-security-1.1#EncryptedKey");

        // Ссылка на штамп времени
        WSEncryptionPart timestampSignPart = new WSEncryptionPart("Timestamp", WSConstants.WSU_NS, "");

        // Ссылка на a:Action
        WSEncryptionPart actionSignPart = new WSEncryptionPart("Action", ACTION_NS, "");

        // Ссылка на a:MessageID
        WSEncryptionPart messageIDSignPart = new WSEncryptionPart("MessageID", ACTION_NS, "");

        // Ссылка на a:ReplyTo
        WSEncryptionPart replyToSignPart = new WSEncryptionPart("ReplyTo", ACTION_NS, "");

        // Ссылка на a:To
        WSEncryptionPart toSignPart = new WSEncryptionPart("To", ACTION_NS, "");

        // Подписываемые симметричным ключом части.
        List<WSEncryptionPart> symSignParts = new ArrayList<WSEncryptionPart>();
        symSignParts.add(timestampSignPart);
        symSignParts.add(actionSignPart);
        symSignParts.add(messageIDSignPart);
        symSignParts.add(replyToSignPart);
        symSignParts.add(toSignPart);
        symSignParts.add(encP);

        sigSymBuilder.setParts(symSignParts);

        // Документ с штампом времени и подписью на симметричном ключе.
        Document docSignedBySymKey = sigSymBuilder.build(timestampedDoc, secHeader);

        Element symSecRef = WSSecurityUtil.findElement(sigSymBuilder.getSignatureElement(),
            "SecurityTokenReference", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        // Удаление Id у security token reference
        //Element symSecRef = sigSymBuilder.getSecurityTokenReference().getElement();
        //symSecRef.removeAttributeNS(WSConstants.WSU_NS, "Id");
        // Добавление wsu к security token reference
        symSecRef.setAttributeNS(WSConstants.XMLNS_NS, "xmlns:wsse", WSConstants.WSSE_NS);
        symSecRef.setAttributeNS(WSConstants.XMLNS_NS, "xmlns:wsu", WSConstants.WSU_NS);

        // saveXml2File(docSignedBySymKey, "C:\\cryptopro.sym.signed.xml", false);

        // #############################################################################################################
        // 4. Добавляем BinarySecurityToken для проверки
        // подписи на асимметричном ключе. Подписываем
        // подпись, выполненную на симметричном ключе.

        WSSecSignature sigAsymBuilder = new WSSecSignature();
        sigAsymBuilder.setUserInfo(CLIENT_ALIAS, CLIENT_PASSWORD);
        sigAsymBuilder.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
        sigAsymBuilder.setSignatureAlgorithm("urn:ietf:params:xml:ns:cpxmlsec:algorithms:gostr34102001-gostr3411");
        sigAsymBuilder.setDigestAlgo("urn:ietf:params:xml:ns:cpxmlsec:algorithms:gostr3411");

        // Для подписания подписи, сделанной на симметричном ключе.
        // Этот же объект используется в роли ссылки при шифровании
        // блоков, т.к. надо будет шифровать эту подпись.
        WSEncryptionPart signSymSignPart = new WSEncryptionPart(sigSymBuilder.getSignatureId(), "Element");
        sigAsymBuilder.setParts(Collections.singletonList(signSymSignPart));

        // Документ с штампом времени, подписями на симметричном и
        // асимметричном ключе
        Document docSignedBySymKeyAndAsymKey = sigAsymBuilder.build(docSignedBySymKey, crypto, secHeader);

        // Element binarySecurityToken = WSSecurityUtil.findElement(secHeader.getSecurityHeader(),
        //    "BinarySecurityToken", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        // sigAsymBuilder.getBinarySecurityTokenElement();
        // binarySecurityToken.setAttributeNS(WSConstants.XMLNS_NS, "xmlns:wsu", WSConstants.WSU_NS);

        // Удаление Id у security token reference
        Element asymSecRef = sigAsymBuilder.getSecurityTokenReference().getElement();
        //asymSecRef.removeAttributeNS(WSConstants.WSU_NS, "Id");
        // Добавление wsu к security token reference
        asymSecRef.setAttributeNS(WSConstants.XMLNS_NS, "xmlns:wsse", WSConstants.WSSE_NS);
        asymSecRef.setAttributeNS(WSConstants.XMLNS_NS, "xmlns:wsu", WSConstants.WSU_NS);

        // saveXml2File(docSignedBySymKeyAndAsymKey, "C:\\cryptopro.sym.asym.signed.xml", false);

        // #############################################################################################################
        // 5. Перестановка элементов подписей так, чтобы было
        // похоже на .net'овский пример.

        Element securityHeaderElement = secHeader.getSecurityHeader();

        // SIG-8 hmac
        String sig1Sym = sigSymBuilder.getSignatureId();
        // SIG-9 gostr3410
        String  sig2Asym = sigAsymBuilder.getId();

        Element sig1SymElement = WSSecurityUtil.findElementById(securityHeaderElement, sig1Sym, false);
        Element sig2AsymElement = WSSecurityUtil.findElementById(securityHeaderElement, sig2Asym, false);

        // Удаляем SIG-8 из его места
        securityHeaderElement.removeChild(sig1SymElement);
        // Добавляем SIG-8 после SIG-9
        securityHeaderElement.insertBefore(sig1SymElement, sig2AsymElement);

        // ##########################################################################################################
        // 6. Добавление DerivedKeyToken со ссылкой на EncryptedKey
        // (tokenEncryptedKeyIdentifier). Этот derived key будет
        // использоваться для зашифрования на симметричном ключе
        // на алгоритме gostr28147. В запросе шифруются подписи (на
        // симметричном и асимметричном ключах) и Body - всего 3
        // элемента.

        MyWSSecDKEncrypt encrBuilder = new MyWSSecDKEncrypt();
        encrBuilder.setSymmetricEncAlgorithm("urn:ietf:params:xml:ns:cpxmlsec:algorithms:gost28147");
        encrBuilder.setExternalKey(secretKey, tokenEncryptedKeyIdentifier);
        encrBuilder.setCustomValueType("http://docs.oasis-open.org/wss/oasis-wss-soap-message-security-1.1#EncryptedKey");

        // Ссылка на подпись на асимметричном ключе
        WSEncryptionPart encrAsymSignPart = new WSEncryptionPart(sigAsymBuilder.getId(), "Element");

        // 3 шифруемых элемента
        List<WSEncryptionPart> encryptionParts = new ArrayList<WSEncryptionPart>();
        encryptionParts.add(signSymSignPart);
        encryptionParts.add(encrAsymSignPart);
        encryptionParts.add(encP);

        encrBuilder.setParts(encryptionParts);

        // Документ с штампом времени и 3 зашифрованными элементами
        Document encryptedDocSignedBySymKeyAndAsymKey =
            encrBuilder.build(docSignedBySymKeyAndAsymKey, secHeader);

        // Добавляем EncryptedKey в документ. Важно добавлять его
        // после того, как добавили все необходимое: подписи и т.п.
        encrKeyBuilder.prependToHeader(secHeader);

        // #############################################################################################################
        // 7. Перестановка элементов derived token так, чтобы было
        // похоже на .net'овский пример.

        securityHeaderElement = secHeader.getSecurityHeader();

        // DK-10
        String dk1SignId = encrBuilder.getId();
        // DK-2
        String dk2EncrId = sigSymBuilder.getId();

        Element dk1SignElement = WSSecurityUtil.findElementById(securityHeaderElement, dk1SignId, false);
        Element dk2EncrElement = WSSecurityUtil.findElementById(securityHeaderElement, dk2EncrId, false);

        // Удаляем DK-2 с его места
        securityHeaderElement.removeChild(dk2EncrElement);
        // Добавляем DK-2 после DK-10
        securityHeaderElement.insertBefore(dk2EncrElement, dk1SignElement);

        // #############################################################################################################
        // 8. Перестановка элементов timestamp и элементов вне хидера
        // (Action, To и др.) так, чтобы было похоже на .net'овский
        // пример.

        securityHeaderElement = secHeader.getSecurityHeader();

        // TS-1
        String ts1Id = timestamp.getId();

        // EK-1
        String ek1Id = encrKeyBuilder.getId();

        Element ts1Element = WSSecurityUtil.findElementById(securityHeaderElement, ts1Id, false);
        Element ek1Element = WSSecurityUtil.findElementById(securityHeaderElement, ek1Id, false);

        // Удаляем TS-1 с его места
        securityHeaderElement.removeChild(ts1Element);
        // Добавляем TS-1 после EK-1
        securityHeaderElement.insertBefore(ts1Element, ek1Element);

        // Перемещаем Security в конец Header.

        // wsse:Security
        Element securityElement = WSSecurityUtil.findElement(doc.getDocumentElement(),
            "Security", WSConstants.WSSE_NS);

        // soapenv:Header
        Element headerElement = WSSecurityUtil.findElement(doc.getDocumentElement(),
            "Header", WSConstants.URI_SOAP12_ENV);

        // Удаляем wsse:Security с его места
        headerElement.removeChild(securityElement);
        // Добавляем wsse:Security в конец soapenv:Header
        headerElement.appendChild(securityElement);

        // #############################################################################################################
        // 9. Записываем полученный зашифрованный документ.

        saveXml2File(encryptedDocSignedBySymKeyAndAsymKey,
            TEST_DIR + "client_request.xml", false);

        // #############################################################################################################
        // 10. Проверка созданного документа локально.
        // Отключим ее, т.к. она предполагает, что у клиента
        // имеется контейнер сервиса, ведь надо расшифровать
        // EncryptedKey, а это можно сделать только закрытым
        // ключом. Запись в файл выполняется, чтобы убедиться
        // в корректности документа после чтения из файла.

        /* Не обязательна, т.к. требует наличия закрытого
           ключа сервиса для расшифровки EncryptedKey.

        SOAPEnvelope clientEnv = getSOAPEnvelopeFromFile(TEST_DIR + "client_request.xml");
        Document clientDoc = clientEnv.getAsDocument();

        WSSecurityEngine secLocalEngine = new WSSecurityEngine();
        secLocalEngine.setWssConfig(config);

        // Выполняем проверку документа
        List<WSSecurityEngineResult> localResults = secLocalEngine.processSecurityHeader(
            clientDoc, null, storeCallbackHandler, crypto);

        System.out.println("*** Результат проверки (клиент):");
        System.out.println(localResults);

        saveXml2File(clientDoc, TEST_DIR +
            "client_request.decrypted.and.verified.xml", false);
         */

        // #############################################################################################################
        // 11. Отправляем запрос к сервису в виде строки.
        // Полученный ответ записываем в файл. Запись в файл
        // выполняется, чтобы убедиться в корректности
        // документа после чтения из файла.

        String clientDocStr = org.apache.ws.security.util.XMLUtils
            .PrettyDocumentToString(encryptedDocSignedBySymKeyAndAsymKey);

        // Ответ сервиса
        String response = getHttpPostFile(REMOTE_HOST, REMOTE_SERVICE, clientDocStr);

        Array.writeFile(TEST_DIR + "service_response.xml", response.getBytes());

        // #############################################################################################################
        // 11. Проверяем полученный ответ сервиса.
        // Клиент обладает секретным ключом. Кешируем
        // его в callback в соответствии с хешем wrapped
        // key, чтобы при проверке ответа сервиса можно
        // было выбрать секретный ключ по хешу.

        SOAPEnvelope serviceResponseEnv =
            getSOAPEnvelopeFromFile(TEST_DIR + "service_response.xml");

        Document serviceResponseDoc = serviceResponseEnv.getAsDocument();

        // Идентификатор ключа представлен хешем wrapped key на
        // алгоритме SHA-1
        String encryptedKeyIdentifier = Base64.encode(WSSecurityUtil
            .generateDigest(encrKeyBuilder.getEncryptedEphemeralKey()));

        // Добавляем идентификатор EncryptedKey, на который
        // может ссылаться derived key, и его значение
        ((CallbackHandlers.SecretKeyAndKeyStoreCallbackHandler)keyCallbackHandler)
            .addSecretKey(encryptedKeyIdentifier, (SecretKeySpec) ((GostSecretKey)secretKey).getSpec());

        WSSecurityEngine secServiceEngine = new WSSecurityEngine();
        secServiceEngine.setWssConfig(config);

        // Выполняем проверку документа
        List<WSSecurityEngineResult> serviceResults = secServiceEngine.processSecurityHeader(
            serviceResponseDoc, null, keyCallbackHandler, crypto);

        System.out.println("*** Результат проверки (сервис):");
        System.out.println(serviceResults);

        // Выведем расшифрованный Body ответа сервиса
        Element serviceBodyElement = WSSecurityUtil
            .findElement(serviceResponseDoc.getDocumentElement(),
                    WSConstants.ELEM_BODY, WSConstants.URI_SOAP12_ENV);

        System.out.println("Ответ сервиса: ");
        org.apache.ws.security.util.XMLUtils.ElementToStream(serviceBodyElement, System.out);

        saveXml2File(serviceResponseDoc,
            TEST_DIR + "service_response.decrypted.and.verified.xml", false);
    }

    /**
     * @param args
     * @throws
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        mymain(args);
        mymain(args);
    }
}
