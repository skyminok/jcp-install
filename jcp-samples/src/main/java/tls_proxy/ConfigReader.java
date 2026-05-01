/**
 * $RCSfileConfigReader.java,v $
 * version $Revision: 36379 $
 * created 15.08.2016 16:04 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <br>
 * Copyright 2004-2016 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package tls_proxy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ru.CryptoPro.JCP.JCP;

import ru.CryptoPro.ssl.Provider;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс ConfigReader предназначен для чтения
 * конфига.
 *
 * @author Copyright 2004-2016 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class ConfigReader implements ConfigParameters {

    /**
     * Множитель миллисекунд.
     */
    private static final int MILLISECONDS = 1000;

    /**
     * Период неактивного соединения.
     */
    private long inactiveTimeout = 2 * 60 * 1000;

    /**
     * Таймаут проверки неактивных соединений.
     */
    private long checkInactiveTimeout = 60 * 1000;

    /**
     * Период ожидания подключения.
     */
    private int serverSoTimeout = 10 * 60 * 1000;

    /**
     * Провайдер.
     */
    private String provider = JCP.PROVIDER_NAME;

    /**
     * Протокол.
     */
    private String protocol = Provider.ALGORITHM_12;

    /**
     * Cipher suites.
     */
    private String[] ciphers = null;

    /**
     * Путь к хранилищу сертификатов.
     */
    private String certStorePath = null;

    /**
     * Пароль к хранилищу сертификатов.
     */
    private String certStorePassword = null;

    /**
     * Тип хранилища сертификатов.
     */
    private String certStoreType = null;

    /**
     * Провайдер для работы с хранилищем сертификатов.
     */
    private String certStoreProvider = null;

    /**
     * Список адресов.
     */
    private final Map<Integer, Address> addresses = new ConcurrentHashMap<Integer, Address>();

    /**
     * Путь к конфигу.
     */
    private final String configFilePath;

    /**
     * Загруженная конфигурация.
     */
    private static ConfigReader INSTANCE = null;

    /**
     * Конструктор.
     *
     * @param configFilePath Путь к файлу конфига. Может быть null.
     */
    private ConfigReader(String configFilePath) {
        this.configFilePath = configFilePath;
        load();
    }

    /**
     * Инициализация объекта.
     *
     * @param configFilePath Путь к файлу конфига. Может быть null.
     */
    public synchronized static void init(String configFilePath) {
        if (INSTANCE == null) {
            INSTANCE = new ConfigReader(configFilePath);
        } // if
    }

    /**
     * Получение объекта конфига.
     *
     * @return объект конфига.
     */
    public static ConfigReader getInstance() {
        return INSTANCE;
    }

    /**
     * Загрузка конфигурации.
     *
     */
    private void load() {

        try {

            File configFile = new File(configFilePath != null ? configFilePath : CONFIG_NAME);
            MainLogger.info("*** Configuration file is " + configFile.getAbsolutePath());

            if (configFile.exists()) {

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

                Document document = dbFactory.newDocumentBuilder().parse(configFile);

                loadParameters(document);
                loadCertStore(document, configFilePath);
                loadAddresses(document);

            } // if
            else {
                throw new Exception("Config file '" + configFile.getAbsolutePath() + "' not found.");
            } // else

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Чтение параметров.
     *
     * @param document конфиг.
     * @throws Exception
     */
    private void loadParameters(Document document) throws Exception {

        MainLogger.info("** Loading parameters...");
        NodeList parameters = document.getElementsByTagName(PARAMETERS);

        if (parameters == null || parameters.getLength() == 0) {
            throw new Exception("Parameters not found.");
        } // if

        Element options = (Element) parameters.item(0);
        if (!options.hasAttributes()) {
            throw new Exception("Parameters values not found.");
        } // if

        String value = options.getAttribute(PARAMETERS_INACTIVITY_TIMEOUT);
        inactiveTimeout = Long.parseLong(value) * MILLISECONDS;

        value = options.getAttribute(PARAMETERS_CHECK_INACTIVITY_TIMEOUT);
        checkInactiveTimeout = Long.parseLong(value) * MILLISECONDS;

        value = options.getAttribute(PARAMETERS_SERVER_SO_TIMEOUT);
        serverSoTimeout = Integer.parseInt(value) * MILLISECONDS;

        provider = options.getAttribute(PARAMETERS_KEY_STORE_PROVIDER);
        protocol = options.getAttribute(PARAMETERS_TLS_PROTOCOL);

        String cipherSuites = options.getAttribute(PARAMETERS_TLS_CIPHERS);
        if (cipherSuites != null && !cipherSuites.isEmpty()) {
            ciphers = cipherSuites.split(TLS_CIPHER_SEPARATOR);
        } // if

        StringBuffer buffer = new StringBuffer();
        buffer.append("Loaded parameters are:");
        buffer.append("\n\t* ").append(PARAMETERS_INACTIVITY_TIMEOUT).append(": ").append(inactiveTimeout);
        buffer.append("\n\t* ").append(PARAMETERS_CHECK_INACTIVITY_TIMEOUT).append(": ").append(checkInactiveTimeout);
        buffer.append("\n\t* ").append(PARAMETERS_SERVER_SO_TIMEOUT).append(": ").append(serverSoTimeout);
        buffer.append("\n\t* ").append(PARAMETERS_KEY_STORE_PROVIDER).append(": ").append(provider);
        buffer.append("\n\t* ").append(PARAMETERS_TLS_PROTOCOL).append(": ").append(protocol);
        buffer.append("\n\t* ").append("cipher separator").append(": ").append(TLS_CIPHER_SEPARATOR);
        buffer.append("\n\t* ").append(PARAMETERS_TLS_CIPHERS).append(": ").append((ciphers != null ? Arrays.toString(ciphers) : "default"));
        MainLogger.info(buffer.toString());
    }

    /**
     * Декодирование хранилища сертификатов.
     *
     * @param document конфиг.
     * @throws Exception
     */
    protected void loadCertStore(Document document, String configFilePath) throws Exception {

        MainLogger.info("** Loading cert store...");

        NodeList certStores = document.getElementsByTagName(CERT_STORE);
        if (certStores == null || certStores.getLength() == 0) {
            throw new Exception("CertStore not found.");
        } // if

        Element certStore = (Element) certStores.item(0);
        if (!certStore.hasAttributes()) {
            throw new Exception("CertStore parameters not found.");
        } // if

        certStoreProvider = certStore.getAttribute(CERT_STORE_PROVIDER);
        certStoreType = certStore.getAttribute(CERT_STORE_TYPE);

        certStorePath = certStore.getAttribute(CERT_STORE_PATH);
        certStorePassword = certStore.getAttribute(CERT_STORE_PASSWORD);

        File certStoreFile = new File(certStorePath);
        String certStoreFileName = certStoreFile.getName();

        // Если в конфиге указано только имя файла хранилища,
        // без пути к нему, то используем тот же путь, что и
        // у конфига. Иначе, естественно, загружаем хранилище
        // по полному пути к нему.

        if (certStorePath.equals(certStoreFileName)) {
            File configFile = new File(configFilePath);
            String configFileName = configFile.getName();
            String configFileAbsolutePath = configFile.getAbsolutePath();
            int configNamePos = configFileAbsolutePath.indexOf(configFileName);
            String configPath = configFileAbsolutePath.substring(0, configNamePos);
            certStorePath = configPath + certStoreFileName;
        } // if

        StringBuffer buffer = new StringBuffer();
        buffer.append("Loaded trust store is:");
        buffer.append("\n\t* ").append(CERT_STORE_PROVIDER).append(": ").append(certStoreProvider);
        buffer.append("\n\t* ").append(CERT_STORE_TYPE).append(": ").append(certStoreType);
        buffer.append("\n\t* ").append(CERT_STORE_PATH).append(": ").append(certStorePath);
        buffer.append("\n\t* ").append(CERT_STORE_PASSWORD).append(": ").append("***");
        MainLogger.info(buffer.toString());

    }

    /**
     * Декодирование адресов.
     *
     * @param document конфиг.
     * @throws Exception
     */
    private void loadAddresses(Document document) throws Exception {
        MainLogger.info("** Loading addresses...");
        NodeList addressesNode = document.getElementsByTagName(ADDRESSES);
        if (addressesNode == null || addressesNode.getLength() == 0) {
            return;
        } // if
        Node singleAddressesNode = addressesNode.item(0);
        NodeList addressesNodeList = singleAddressesNode.getChildNodes();
        for (int i = 0; i < addressesNodeList.getLength(); i++) {
            Node address = addressesNodeList.item(i);
            if (address.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            } // if
            MainLogger.info("Loading next address...");
            NodeList addressContent = address.getChildNodes();
            if (addressContent == null || addressContent.getLength() == 0) {
                continue;
            } // if
            Address decodedAddress = Address.load(addressContent);
            if (decodedAddress != null) {
                addresses.put(decodedAddress.getListenPort(), decodedAddress);
            } // if
        } // for
    }

    /**
     * Поиск адреса по порту прослушивания.
     *
     * @param listenPort Слушаемый порт.
     * @return адрес или null.
     */
    public Address findAddress(int listenPort) {
        Set<Map.Entry<Integer, Address>> entries = addresses.entrySet();
        for (Map.Entry<Integer, Address> entry : entries) {
            if (entry.getKey() == listenPort) {
                return entry.getValue();
            } // if
        } // for
        return null;
    }

    /**
     * Получение списка адресов.
     *
     * @return список адресов.
     */
    public List<Address> getAddresses() {
        Set<Map.Entry<Integer, Address>> entries = addresses.entrySet();
        List<Address> addressList = new ArrayList<Address>();
        for (Map.Entry<Integer, Address> entry : entries) {
            addressList.add(entry.getValue());
        } // for
        return Collections.unmodifiableList(addressList);
    }

    /**
     * Получение отдельного адреса.
     *
     * @param listenPort Слушаемый порт.
     * @return адрес.
     */
    public Address getAddress(int listenPort) {
        return findAddress(listenPort);
    }

    /**
     * Проверка существования адреса в списке.
     *
     * @param address Адрес.
     * @return true, если существует.
     */
    public boolean addressExists(Address address) {
        return addresses.containsValue(address);
    }

    /**
     * Добавление в список и сохранение адреса.
     *
     * @param address Адрес.
     * @return  результат сохранения.
     */
    public boolean putAddress(Address address) {
        addresses.put(address.getListenPort(), address);
        if (!save()) {
            addresses.remove(address.getListenPort());
            return false;
        } // if
        return true;
    }

    /**
     * Удаление из списка адреса.
     *
     * @param listenPort Слушаемый порт.
     * @return результат сохранения.
     */
    public boolean removeAddress(int listenPort) {
        Address address = addresses.remove(listenPort);
        if (!save()) {
            addresses.put(listenPort, address);
            return false;
        } // if
        return true;
    }

    /**
     * Сохранение списка адресов.
     *
     * @return успех в случае сохранения.
     */
    private boolean save() {

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            Document document = dbFactory.newDocumentBuilder().newDocument();

            Element configElement = document.createElement(CONFIG);
            document.appendChild(configElement);

            Element paramsElement = document.createElement(PARAMETERS);
            configElement.appendChild(paramsElement);

            long it  = inactiveTimeout / MILLISECONDS;
            long cit = checkInactiveTimeout / MILLISECONDS;
            long sst = serverSoTimeout / MILLISECONDS;

            paramsElement.setAttribute(PARAMETERS_INACTIVITY_TIMEOUT, String.valueOf(it));
            paramsElement.setAttribute(PARAMETERS_CHECK_INACTIVITY_TIMEOUT, String.valueOf(cit));
            paramsElement.setAttribute(PARAMETERS_SERVER_SO_TIMEOUT, String.valueOf(sst));
            paramsElement.setAttribute(PARAMETERS_KEY_STORE_PROVIDER, provider);
            paramsElement.setAttribute(PARAMETERS_TLS_PROTOCOL, protocol);

            if (ciphers != null) {
                StringBuffer buffer = new StringBuffer();
                for (String cipher : ciphers) {
                    buffer.append(cipher).append(TLS_CIPHER_SEPARATOR);
                } // for
                paramsElement.setAttribute(PARAMETERS_TLS_CIPHERS, buffer.toString());
            } // if

            Element storeElement = document.createElement(CERT_STORE);
            configElement.appendChild(storeElement);

            storeElement.setAttribute(CERT_STORE_PROVIDER, certStoreProvider);
            storeElement.setAttribute(CERT_STORE_TYPE, certStoreType);
            storeElement.setAttribute(CERT_STORE_PATH, certStorePath);
            storeElement.setAttribute(CERT_STORE_PASSWORD, certStorePassword);

            Element addressesElement = document.createElement(ADDRESSES);
            configElement.appendChild(addressesElement);

            Set<Map.Entry<Integer, Address>> entries = addresses.entrySet();
            for (Map.Entry<Integer, Address> entry : entries) {

                Address address = entry.getValue();

                Element addressElement = document.createElement(ADDRESS);
                addressesElement.appendChild(addressElement);

                Element listenPortElement = document.createElement(ADDRESS_LISTEN_PORT);
                addressElement.appendChild(listenPortElement);
                listenPortElement.setTextContent(String.valueOf(address.getListenPort()));

                Element hostElement = document.createElement(ADDRESS_HOST);
                addressElement.appendChild(hostElement);
                hostElement.setTextContent(address.getHost());

                Element portElement = document.createElement(ADDRESS_PORT);
                addressElement.appendChild(portElement);
                portElement.setTextContent(String.valueOf(address.getPort()));

                Element pageElement = document.createElement(ADDRESS_PAGE);
                addressElement.appendChild(pageElement);
                pageElement.setTextContent(address.getPage());

                Element clientAuthElement = document.createElement(ADDRESS_CLIENT_AUTH_ENABLED);
                addressElement.appendChild(clientAuthElement);
                clientAuthElement.setTextContent(String.valueOf(address.isClientAuthEnabled()));

                Element keyTypeElement = document.createElement(ADDRESS_KEY_TYPE);
                addressElement.appendChild(keyTypeElement);
                keyTypeElement.setTextContent(address.getKeyType());

                Element keyAliasElement = document.createElement(ADDRESS_KEY_ALIAS);
                addressElement.appendChild(keyAliasElement);
                keyAliasElement.setTextContent(address.getKeyAlias());

                Element keyPassElement = document.createElement(ADDRESS_KEY_PASSWORD);
                addressElement.appendChild(keyPassElement);
                keyPassElement.setTextContent(address.getKeyPassword());

            } // for

            saveXML(configFilePath, document);
            return true;

        } catch (ParserConfigurationException e) {
            MainLogger.error("Saving XML failed.", e);
        } catch (FileNotFoundException e) {
            MainLogger.error("Saving XML failed.", e);
        } catch (TransformerConfigurationException e) {
            MainLogger.error("Saving XML failed.", e);
        } catch (TransformerException e) {
            MainLogger.error("Saving XML failed.", e);
        } catch (Exception e) {
            MainLogger.error("Saving XML failed.", e);
        }

        return false;

    }

    /**
     * Сохранение XML документа в файл.
     *
     * @param configFilePath Путь к файлу.
     * @param document XML документ.
     * @throws FileNotFoundException
     * @throws TransformerException
     */
    public static void saveXML(String configFilePath, Document document) throws FileNotFoundException, TransformerException {
        try (OutputStream configStream = new FileOutputStream(configFilePath)) {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(configStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Получение пути к хранилищу сертификатов.
     *
     * @return  путь к хранилищу сертификатов.
     */
    public String getCertStorePath() {
        return certStorePath;
    }

    /**
     * Получение пароля к хранилищу сертификатов.
     *
     * @return пароль к хранилищу сертификатов.
     */
    public String getCertStorePassword() {
        return certStorePassword;
    }

    /**
     * Получение типа хранилища сертификатов.
     *
     * @return  тип хранилища сертификатов.
     */
    public String getCertStoreType() {
        return certStoreType;
    }

    /**
     * Получение провайдера хранилища сертификатов.
     *
     * @return  провайдер хранилища сертификатов.
     */
    public String getCertStoreProvider() {
        return certStoreProvider;
    }

    /**
     * Получение периода неактивных соединений.
     *
     * @return   период неактивных соединений.
     */
    public long getInactiveTimeout() {
        return inactiveTimeout;
    }

    /**
     * Получение периода проверки неактивных соединений.
     *
     * @return   период проверки неактивных соединений.
     */
    public long getCheckInactiveTimeout() {
        return checkInactiveTimeout;
    }

    /**
     * Получение периода ожидания подключения.
     *
     * @return период ожидания подключения.
     */
    public int getServerSoTimeout() {
        return serverSoTimeout;
    }

    /**
     * Получение провайдера.
     *
     * @return провайдер.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Получение протокола.
     *
     * @return протокол.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Получение списка cipher suite.
     *
     * @return список cipher suite.
     */
    public String[] getCiphers() {
        return ciphers;
    }

    /**
     * Получение номера максимального порта в списке.
     *
     * @return номер порта.
     */
    public int getMaxListenPort() {
         Set<Integer> keys = addresses.keySet();
         return Collections.max(keys);
    }

}
