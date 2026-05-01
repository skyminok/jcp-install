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
package xmlSign.prfgost;

import org.apache.ws.security.message.WSSecHeader;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.utils.EncryptionConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.Crypto.Key.GostSecretKey;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Key.SecretKeyInterface;
import ru.CryptoPro.JCP.Key.SecretKeySpec;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.CryptParamsInterface;
import ru.CryptoPro.JCPxml.Consts;
import ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * Класс DeriveKeySimpleSchemeExample реализует короткий обмен между клиентом и
 * сервером (establishSecureContext=false), при этом - в усеченной форме, т.е. в
 * примере:
 * А) клиент зашифрует запрос;
 * Б) сервер расшифрует запрос, выведет его и зашифрует ответ клиенту;
 * В) клиент расшифрует ответ сервера.
 * Структура образцов документов не будет содержать блоков EncryptedKey и DerivedKeyToken,
 * но содержимое последних передается с помощью аналогичных полей класса TransportMessage.
 *
 * В примере используются 2 пары ключей (закрытый и открытый ключи): клиента и сервиса.
 *
 */
public class DeriveKeySimpleSchemeExample {

    /**
     * Алиас ключа и сертификата клиента.
     */
    protected static final String CLIENT_ALIAS = "gost_exch";

    /**
     * Пароль для доступа к ключу клиента.
     */
    protected static final char[] CLIENT_PASSWORD = "Pass1234".toCharArray();

    /**
     * Алиас ключа и сертификата сервиса.
     */
    protected static final String SERVICE_ALIAS = "localhost_cont";

    /**
     * Пароль для доступа к ключу сервиса.
     */
    protected static final char[] SERVICE_PASSWORD = null;

    /**
     * Фабрика для декодирования и создания документов.
     */
    private static DocumentBuilderFactory documentBuilderFactory =
        DocumentBuilderFactory.newInstance();

    /**
     * Перечисление типов операций надо документов: зашифрование и
     * расшифрование.
     */
    public static enum OperationType {OT_ENCRYPT, OT_DECRYPT};

    /**
     * Поле Label, использующееся по умолчанию (WS-Security).
     */
    public static final String LABEL = "WS-SecureConversation";

    /**
     * Приватный ключ клиента.
     */
    protected static PrivateKey clientPrivateKey = null;

    /**
     * Сертификат клиента.
     */
    protected static X509Certificate clientCertificate = null;

    /**
     * Приватный ключ сервиса.
     */
    protected static PrivateKey servicePrivateKey = null;

    /**
     * Сертификат сервиса. Считается, что он известен клиенту. На
     * этом сертификате шифруется секретный ключ, созданный клиентом.
     */
    protected static X509Certificate serviceCertificate = null;

    /**
     * Секретный ключ, однажды генерируемый клиентом в начале обмена.
     */
    private static SecretKey clientSecretKey = null;

    /**
     * Секретный ключ, однажды расшифрованный сервисом в начале обмена.
     */
    private static SecretKey serviceSecretKey = null;

    /**
     * Форма запроса клиента - упрощенный аналог XML документа с
     * блоками <EncryptedKey>, <DerivedKeyToke> с полем Nonce,
     * а также сами зашифрованные на производном ключе данные
     * <EncryptedData> в блоке <Body>.
     */
    public static class TransportMessage {

        /**
         * Зашифрованные секретный ключ, аналог:
         * <EncryptedKey ...>
         *     ...
         * </EncryptedKey>
         */
        public byte[] encryptedKey = null;
        /**
         * 16 байт случайных данных, аналог:
         * <DerivedKeyToken ...>
         *     <Nonce>
         *         ...
         *     </Nonce>
         * </DerivedKeyToken>
         */
        public byte[] nonce = null;
        /**
         * Документ с зашифрованными данными, содержащий аналог:
         * <EncryptedData>
         *     ...
         * </EncryptedData>.
         */
        public Document encryptedDocument = null;

        /**
         * Конструктор.
         *
         * @param encryptedKey Зашифрованный секретный ключ (167 байт).
         * @param nonce Случайная последовательность (16 байт).
         * @param doc XML документ с зашифрованными данными.
         */
        public TransportMessage(byte[] encryptedKey, byte[] nonce, Document doc) {
            this.encryptedKey = encryptedKey;
            this.nonce = nonce;
            this.encryptedDocument = doc;
        }
    }

    /**
     * Документ для обмена между клиентом и сервисом.
     *
     * @param message Сообщение.
     * @return Строка сообщения.
     */
    protected static String getMessage(String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<SOAP-ENV:Envelope "
            +   "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
            +   "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
            +   "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
            +   "<SOAP-ENV:Body>"
            +       "<add xmlns=\"http://ws.apache.org/counter/counter_port_type\">"
            +           "<value xmlns=\"\">" + message + "</value>"
            +       "</add>"
            +   "</SOAP-ENV:Body>"
            + "</SOAP-ENV:Envelope>";
    }

    /**
     * Функция зашифрования или расшифрования XML документа.
     *
     * @param doc XML документ для зашифрования/расшифрования.
     * @param element Элемент XML документа, который будет зашифрован
     * или расшифрован.
     * @param key Секретный ключ.
     * @param operationType Тип операции.
     * @return Исходящий XML документ (с зашифрованным блоком или
     * расшифрованными данными).
     * @throws Exception
     */
    private static Document proceedMessage(Document doc, Element element,
        SecretKey key, OperationType operationType) throws Exception {

        // Шифратор на алгоритме ГОСТ 28147.
        XMLCipher cipher = XMLCipher.getInstance(Consts.URI_GOST_CIPHER);
        cipher.init( operationType == OperationType.OT_ENCRYPT
            ? XMLCipher.ENCRYPT_MODE
            : XMLCipher.DECRYPT_MODE, key);

        if (operationType == OperationType.OT_ENCRYPT) {
            cipher.doFinal(doc, element, true);
        } // if
        else {
            cipher.doFinal(doc, element);
        } //else

        return doc;
    }

    /**
     * Функция зашифрования XML документа.
     *
     * @param doc XML документ для зашифрования.
     * @param key Секретный ключ.
     * @return XML документ с зашифрованным блоком (EncryptedData).
     * @throws Exception
     */
    public static Document encryptMessage(Document doc, SecretKey key) throws Exception {

        // Элемент для зашифрования.
        Element element = doc.getDocumentElement();

        return proceedMessage(doc, element, key, OperationType.OT_ENCRYPT);
    }

    /**
     * Функция расшифрования XML документа.
     *
     * @param doc XML документ для расшифрования.
     * @param key Секретный ключ.
     * @return XML документ с расшифрованным блоком.
     * @throws Exception
     */
    public static Document decryptMessage(Document doc, SecretKey key) throws Exception {

        // Элемент для расшифрования.
        Element encryptedDataElement = (Element) doc.getElementsByTagNameNS(
            EncryptionConstants.EncryptionSpecNS, EncryptionConstants._TAG_ENCRYPTEDDATA).item(0);

        return proceedMessage(doc, encryptedDataElement, key, OperationType.OT_DECRYPT);
    }

    /**
     * Функция конвертации строки в SOAP XML документ.
     *
     * @param xml XML документ в виде строки.
     * @return объект w3c Document.
     * @throws Exception
     */
    private static Document toSOAPPart(String xml) throws Exception {
        InputStream in = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        return builder.parse(in);
    }

    /**
     * Получение SOAP XML документа из некоторого образца.
     *
     * @param message XML документ в виде строки.
     * @return объект w3c Document.
     * @throws Exception
     */
    public static Document getDocumentSample(String message) throws Exception {

        Document doc = toSOAPPart(message);
        WSSecHeader secHeader = new WSSecHeader();
        secHeader.insertSecurityHeader(doc);

        return doc;
    }

    /**
     * SOAP XML документ клиента.
     *
     * @return объект w3c Document.
     * @throws Exception
     */
    public static Document getClientDocumentSample() throws Exception {
        return getDocumentSample(getMessage("From client."));
    }

    /**
     * SOAP XML документ сервиса.
     *
     * @return объект w3c Document.
     * @throws Exception
     */
    public static Document getServiceDocumentSample() throws Exception {
        return getDocumentSample(getMessage("From service."));
    }

    /**
     * Функция инициализации JCPxml, xmlsec, загрузки контейнеров.
     * Необходимо выполнять в начале примера.
     *
     */
    public static void init() {

        // Инициализируем JCPxml, xmlsec.

        org.apache.xml.security.Init.init();

        JCPInit.initProviders(false);

        if(!JCPXMLDSigInit.isInitialized()) {
            JCPXMLDSigInit.init();
        } // if

        documentBuilderFactory.setNamespaceAware(true);

        try {
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }


        // Загрузка ключей и сертификатов клиента и сервиса.

        try {

            KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
            keyStore.load(null, null);

            clientPrivateKey = (PrivateKey) keyStore.getKey(CLIENT_ALIAS, CLIENT_PASSWORD);
            clientCertificate = (X509Certificate) keyStore.getCertificate(CLIENT_ALIAS);

            servicePrivateKey = (PrivateKey) keyStore.getKey(SERVICE_ALIAS, SERVICE_PASSWORD);
            serviceCertificate = (X509Certificate) keyStore.getCertificate(SERVICE_ALIAS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Функция зашифрования секретного ключа. В ходе шифрования вырабатывается
     * пара эфемерных ключей, которая совместно с сертификатом получателя
     * участвует в выработке ключа согласования, на котором и шифруется
     * секретный ключ. В итоге получается структура GostR3410_KeyTransport.
     * Эта структура помещается в блок EncryptedKey документа (167 байт).
     *
     * @param secretKey Секретный ключ.
     * @param certificate Сертификат для зашифрования.
     * @return зашифрованный секретный ключ.
     * @throws Exception
     */
    protected static byte[] wrapSecretKey(SecretKey secretKey,
        X509Certificate certificate) throws Exception {

        Cipher wrapCipher = Cipher.getInstance(CryptoProvider.GOST_TRANSPORT);
        wrapCipher.init(Cipher.WRAP_MODE, certificate);

        return wrapCipher.wrap(secretKey);
    }

    /**
     * Функция расшифрования секретного ключа. При расшифровании
     * используются данные из структуры GostR3410_KeyTransport и
     * закрытый ключ, при помощи которых создается ключ согласования,
     * которым расшифровывается секретный ключ.
     *
     * @param wrappedSecretKey Зашифрованный секретный ключ.
     * @param privateKey Закрытый ключ для расшифрования.
     * @return расшифрованный секретный ключ.
     * @throws Exception
     */
    protected static SecretKey unwrapSecretKey(byte[] wrappedSecretKey,
        PrivateKey privateKey) throws Exception {

        Cipher unwrapCipher = Cipher.getInstance(CryptoProvider.GOST_TRANSPORT);
        unwrapCipher.init(Cipher.UNWRAP_MODE, privateKey);

        // Расшифрованный секретный ключ.
        SecretKey secretKey = (SecretKey) unwrapCipher.unwrap(wrappedSecretKey,
            null, Cipher.SECRET_KEY);

        return secretKey;
    }

    /**
     * Функция получения нового секретного ключа из другого секретного ключа
     * (secret) с участием nonce и label.
     *
     * @param secret Исходный секретный ключ.
     * @param clientLabel Клиентский label.
     * @param serviceLabel Сервисный label.
     * @param nonce Случайная последовательность.
     * @return Новый секретный ключ (derivedKey).
     * @throws Exception
     */
    public static SecretKey deriveSecretKey(SecretKeySpec secret, String clientLabel,
        String serviceLabel, byte[] nonce) throws Exception {

        // Складываем 2 label'а - клиентский и серверный.
        byte[] label = (clientLabel + serviceLabel).getBytes("UTF-8");

        // Соединяем label'ы и seed (nonce) в один блок.
        byte[] value = new byte[label.length + nonce.length];
        System.arraycopy(label, 0, value, 0, label.length);
        System.arraycopy(nonce, 0, value, label.length, nonce.length);

        byte[][] data = new byte[1][];
        data[0] = value;

        // Готовим исходящий массив байтов для последующего создания
        // ключа из него. Размер массива - 32 байта.
        byte[] digest = new byte[SecretKeySpec.KEY_LEN];

        // Производим выработку digest.
        secret.methodGOSTR3411PRF(data, digest, false);

        // Формруем новый ключ (derived key).
        SecretKeyInterface derivedSecretKeySpec = new SecretKeySpec(digest,
            (CryptParamsInterface)secret.getParams());

        return new GostSecretKey(derivedSecretKeySpec);
    }

    /**
     * Функция генерации случайной последовательности длиной size.
     *
     * @param size Размер получаемого массива.
     * @return случайная последовательность.
     * @throws Exception
     */
    protected static byte[] generateNonce(int size) throws Exception {

        SecureRandom random = SecureRandom.getInstance(JCP.CP_RANDOM);
        random.setSeed(System.nanoTime());

        byte[] nonce = new byte[size];
        random.nextBytes(nonce);

        return nonce;
    }

    /**
     * Тестовая функция формирования запроса клиента в виде некоторого
     * XML документа с зашифрованным блоком, который обработает (в
     * данном случае - просто расшифрует и выведет) сервис.
     *
     * @return запрос сервису.
     * @throws Exception
     */
    public static TransportMessage clientSendRequest() throws Exception {

        // Перед обращением клиент генерит симметричный (далее -
        // секретный) ключ.
        KeyGenerator kg = KeyGenerator.getInstance(CryptoProvider.GOST_CIPHER_NAME);
        clientSecretKey = kg.generateKey();

        SecretKeySpec secretKeySpec = (SecretKeySpec) ((GostSecretKey)clientSecretKey).getSpec();

        // Клиент шифрует секретный ключ.
        byte[] wrappedSecretKey = wrapSecretKey(clientSecretKey, serviceCertificate);

        // Генерируется Nonce, который попадет в элемент DerivedKeyToken.
        byte[] nonce = generateNonce(16);

        // Производится новый секретный ключ (derived key). Label'ы статические
        // и известны заранее.
        SecretKey derivedSecretKey = deriveSecretKey(secretKeySpec, LABEL, LABEL, nonce);

        // Берется образец XML документа.
        Document doc = getClientDocumentSample();
        System.out.println("Source client message:\n" +
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc));

        // Шифрование XML документа.
        Document encryptedDoc = encryptMessage(doc, derivedSecretKey);
        System.out.println("Encrypted client message:\n" +
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc));

        return new TransportMessage(wrappedSecretKey, nonce, encryptedDoc);
    }

    /**
     * Тестовая функция обработки запроса клиента сервисом. Вся необходимая
     * информация (помимо документа) поступает из clientRequest.
     *
     * @param clientRequest Запрос клиента. Содержит имитацию блоков EncryptedKey
     * и DerivedKeyToken.
     * @return ответ клиенту.
     * @throws Exception
     */
    public static TransportMessage serviceProcess(TransportMessage clientRequest)
        throws Exception {

        // Сервис извлекает секретный ключ из блока EncryptedKey с помощью
        // своего приватного ключа.
        serviceSecretKey = unwrapSecretKey(clientRequest.encryptedKey, servicePrivateKey);
        SecretKeySpec keySpec = (SecretKeySpec) ((GostSecretKey) serviceSecretKey).getSpec();

        // Производится секретный ключ (derived key), которым шифровалось сообщение.
        SecretKey derivedSecretKey = deriveSecretKey(keySpec, LABEL, LABEL, clientRequest.nonce);

        // Расшифровываем сообщение.
        Document doc = decryptMessage(clientRequest.encryptedDocument, derivedSecretKey);
        System.out.println("Decrypted client message:\n" +
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc));

        // Генерируется Nonce, который попадет в элемент DerivedKeyToken.
        byte[] nonce = generateNonce(16);

        // Производится новый секретный ключ (derived key). Label'ы статические
        // и известны заранее.
        SecretKey newDerivedSecretKey = deriveSecretKey(keySpec, LABEL, LABEL, nonce);

        // Берется образец XML документа.
        doc = getServiceDocumentSample();
        System.out.println("Source service message:\n" +
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc));

        // Шифрование XML документа.
        Document encryptedDoc = encryptMessage(doc, newDerivedSecretKey);
        System.out.println("Encrypted service message:\n" +
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(encryptedDoc));

        // EncryptedKey больше не передаем.
        return new TransportMessage(null, nonce, encryptedDoc);
    }

    /**
     * Тестовая функция обработки ответа сервиса. Вся необходимая
     * информация (помимо документа) поступает из serviceResponse.
     *
     * @param serviceResponse Ответ сервиса. Содержит имитацию блока
     * DerivedKeyToken.
     * @throws Exception
     */
    public static void clientProcess(TransportMessage serviceResponse)
        throws Exception {

        // Производится секретный ключ (derived key), которым шифровалось сообщение.
        SecretKeySpec secretKeySpec = (SecretKeySpec) ((GostSecretKey)clientSecretKey).getSpec();
        SecretKey derivedSecretKey = deriveSecretKey(secretKeySpec, LABEL, LABEL, serviceResponse.nonce);

        // Расшифровываем сообщение.
        Document doc = decryptMessage(serviceResponse.encryptedDocument, derivedSecretKey);
        System.out.println("Decrypted service message:\n" +
            org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(doc));
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        TransportMessage clientRequest = clientSendRequest();
        TransportMessage serviceResponse = serviceProcess(clientRequest);
        clientProcess(serviceResponse);

    }

    static {
        init();
    }

}
