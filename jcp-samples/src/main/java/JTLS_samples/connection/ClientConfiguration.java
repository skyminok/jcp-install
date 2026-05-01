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
package JTLS_samples.connection;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.ssl.Provider;

import javax.net.ssl.TrustManager;
import java.io.PrintStream;

/**
 * Класс для хранения настроек клиента.
 *
 * 23/04/2013
 *
 */
public class ClientConfiguration {

    /**
     * Хост для подключения.
     */
    private String host;
    /**
     * Порт для подключения.
     */
    private int port;
    /**
     * Файл для скачивания.
     */
    private String file;
    /**
     * Нужно ли использовать клиентскую аутентификацию.
     */
    private boolean clientAuth = false;
    /**
     * Кол-во клиентов.
     */
    private int clientCount = 1;
    /**
     * Количество загрузок файлов.
     */
    private int loadingCount = 10;
    /**
     * Нужно ли использовать apache http client 4.x вместо simple socket.
     */
    private boolean apache = false;

    /**
     * Нужно ли использовать HTTP 1.1.
     */
    private boolean isHttp1_1 = false;

    /**
     * Нужно ли отключить проверку CN == hostname.
     */
    private boolean allowAllHostnameVerifier = false;
    /**
     * Используем локальный веб-сервер, запущенный в этой системе. Например,
     * томкат. Он локальный (localhost), но не самописный. Если задан, то
     * самописный сервер не используется, даже если задан.
     */
    private boolean externalWebServer = false;

    /**
     * Папка, откуда следует брать файлы для проверки
     * скачиваемых файлов. Используется при "локальных"
     * тестах.
     */
    private String fileSource = null;

    /**
     * Папка, куда следует класть скачиваемые файлы.
     */
    private String fileStore = null;

    /**
     * Время работы потока в секундах.
     */
    private int threadTimeout = AbstractTLSExample.THREAD_TIMEOUT;

    /**
     * Время работы сервера в секундах.
     */
    private int serverTimeout = AbstractTLSExample.THREAD_TIMEOUT;

    /**
     * Тип хранилища доверенных сертификатов.
     */
    private String trustStoreType = JCP.HD_STORE_NAME;

    /**
     * Путь к хранилищу сертификатов.
     */
    private String trustStore = null;

    /**
     * Пароль для доступа к хранилищу сертификатов.
     */
    private String trustStorePassword = null;

    /**
     * Тип ключевого контейнера.
     */
    private String keyStoreType = JCP.HD_STORE_NAME;

    /**
     *Алиас ключевого контейнера.
     */
    private String keyStoreAlias = null;

    /**
     * Пароль для доступа к ключевому контейнеру.
     */
    private String keyStorePassword = null;

    /**
     * True, если проверка другой стороны отключена.
     */
    protected boolean trustAll;

    /**
     * Менеджер сертификатов.
     */
    protected TrustManager trustManager = null;

    /**
     * Конструктор.
     *
     * @param host Хост.
     * @param port Порт.
     * @param file Файл для скачивания.
     * @param source Папка, откуда следует брать файлы для проверки
     * скачиваемых файлов. Используется при "локальных" тестах.
     * @param store Папка, куда следует класть скачиваемые файлы.
     */
    public ClientConfiguration(String host, int port, String file,
        String source, String store) {
        this.host = host;
        this.port = port;
        this.file = file;
        this.fileSource = source;
        this.fileStore = store;
    }

    /**
     * Если true, то доверие другой стороне без
     * проверок сертификатов.
     *
     * @param trustAll True, если полное доверие
     * другой стороне.
     */
    public void setTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
    }

    /**
     * Если true, то доверие другой стороне без
     * проверок сертификатов.
     *
     * @return true, если полное доверие другой
     * стороне.
     */
    public boolean isTrustAll() {
        return trustAll;
    }

    /**
     * Узнать хост для подключения.
     *
     * @return хост.
     */
    public String getHost() {
        return host;
    }

    /**
     * Задать хост для подключения.
     *
     * @param value хост для подключения.
     */
    public void setHost(String value) {
        host = value;
    }

    /**
     * Узнать порт для подключения.
     *
     * @return порт.
     */
    public int getPort() {
        return port;
    }

    /**
     * Узнать файл для скачивания.
     *
     * @return имя файла.
     */
    public String getDownloadingFile() {
        return file;
    }

    /**
     * Задать режим двойной аутентификации.
     *
     * @param value True, если использовать двойную аутентификацию.
     */
    public void setUseClientAuth(boolean value) {
        clientAuth = value;
    }

    /**
     * Узнать режим аутентификации.
     *
     * @return True, если используется двойная аутентификация.
     */
    public boolean getUseClientAuth() {
        return clientAuth;
    }

    /**
     * Задать количество клиентов.
     *
     * @param value Количество клиентов.
     */
    public void setClientCount(int value) {
        clientCount = value;
    }

    /**
     * Узнать количество клиентов.
     *
     * @return количество клиентов.
     */
    public int getClientCount() {
        return clientCount;
    }

    /**
     * Задать количество загрузок файла.
     *
     * @param value Количество загрузок.
     */
    public void setLoadingCount(int value) {
        loadingCount = value;
    }

    /**
     * Узнать количество загрузок файла.
     *
     * @return количество загрузок.
     */
    public int getLoadingCount() {
        return loadingCount;
    }

    /**
     * Задать необходимость использования apache http client 4.x вместо simple socket.
     *
     * @param value True, если нужно использовать apache http client 4.x.
     */
    public void setUseApache(boolean value) {
        apache = value;
    }

    /**
     * Узнать, нужно ли использовать apache http client 4.x вместо simple socket.
     *
     * @return true, если нужно использовать apache http client 4.x.
     */
    public boolean getUseApache() {
        return apache;
    }

    /**
     * Задать необходимость использования HTTP 1.1.
     *
     * @param value True, если нужно использовать HTTP 1.1.
     */
    public void setIsHttp1_1(boolean value) {
        isHttp1_1 = value;
    }

    /**
     * Узнать, нужно ли использовать HTTP 1.1
     *
     * @return true, если нужно использовать HTTP 1.1.
     */
    public boolean getIsHttp1_1() {
        return isHttp1_1;
    }

    /**
     * Узнать хранилище загружаемых файлов. Может использоваться при проверке скачиваемого файла
     * и его копии в этой папке, которую использует локальный сервер в "локальных" тестах.
     *
     * @return путь к папке.
     */
    public String getFileSource() {
        return fileSource;
    }

    /**
     * Узнать хранилище для файлов клиента.
     *
     * @return путь к папке.
     */
    public String getFileStore() {
        return fileStore;
    }

    /**
     * Задать необходимость проверки соответствия CN сертификата адресу хоста.
     *
     * @param value True, если нужно проверять.
     */
    public void setAllowAllHostnameVerifier(boolean value) {
        allowAllHostnameVerifier = value;
    }

    /**
     * Узнать, нужна ли проверка соответствия CN сертификата адресу хоста.
     *
     * @return True, если нужно проверять.
     */
    public boolean getAllowAllHostnameVerifier() {
        return allowAllHostnameVerifier;
    }

    /**
     * Преобразование хоста, порта и пути к файлу в url.
     *
     * @return url для скачивания файла.
     */
    public String getDownloadingUrl() {
        return "https://" + host + ":" + port + "/" + file;
    }

    /**
     * Определяет, является ли хост локальным.
     *
     * @return True, если хост локальный.
     */
    public boolean isLocal() {
        return host == null || host.equalsIgnoreCase("localhost")
            || host.equalsIgnoreCase("127.0.0.1");
    }

    /**
     * Используется ли локальный "чужой" сервер.
     *
     * @param external True, если используется.
     */
    public void setExternalWebServer(boolean external) {
        externalWebServer = external;
    }

    /**
     * Используется ли локальный "чужой" сервер.
     *
     * @return True, если используется.
     */
    public boolean getExternalWebServer() {
        return externalWebServer;
    }

    /**
     * Задать таймаут работы потока.
     *
     * @param timeout Количество секунд.
     */
    public void setThreadTimeout(int timeout) {
        threadTimeout = timeout;
    }

    /**
     * Узнать максимальное время работы потока.
     *
     * @return количество милисекунд.
     */
    public int getThreadTimeout() {
        return threadTimeout * 1000;
    }

    /**
     * Задать таймаут работы сервера.
     *
     * @param timeout Количество секунд.
     */
    public void setServerTimeout(int timeout) {
        serverTimeout = timeout;
    }

    /**
     * Узнать максимальное время работы сервера.
     *
     * @return количество милисекунд.
     */
    public int getServerTimeout() {
        return serverTimeout * 1000;
    }

    /**
     * Является ли конфигурация достаточной, чтобы использовать
     * расширенный конструктор {@link JTLS_samples.connection.SSLConfiguration}.
     *
     * @return
     */
    public boolean isFull() {
        return trustStore != null && trustStorePassword != null;
    }

    /**
     * Задание типа хранилища доверенных сертификатов.
     *
     * @param type тип хранилища.
     */
    public void setTrustStoreType(String type) {
        trustStoreType = type;
    }

    /**
     * Получение типа хранилища доверенных сертификатов.
     *
     * @return тип хранилища.
     */
    public String getTrustStoreType() {
        return trustStoreType;
    }

    /**
     * Задание пути к хранилищу доверенных сертификатов.
     *
     * @param pass путь к хранилищу.
     */
    public void setTrustStore(String pass) {
        trustStore = pass;
    }

    /**
     * Получение пути к хранилищу доверенных сертификатов.
     *
     * @return путь к хранилищу.
     */
    public String getTrustStore() {
        return trustStore;
    }

    /**
     * Задание пароля для хранилища доверенных сертификатов.
     *
     * @param password пароль для хранилища доверенных сертификатов.
     */
    public void setTrustStorePassword(String password) {
        trustStorePassword = password;
    }

    /**
     * Получение пароля для хранилища доверенных сертификатов.
     *
     * @return пароль для хранилища доверенных сертификатов.
     */
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    /**
     * Задание типа ключевого контейнера.
     *
     * @param type тип ключевого контейнера.
     */
    public void setKeyStoreType(String type) {
        keyStoreType = type;
    }

    /**
     * Получение типа ключевого контейнера.
     *
     * @return тип ключевого контейнера.
     */
    public String getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * Задание алиаса ключевого контейнера.
     *
     * @param alias алиас ключевого контейнера.
     */
    public void setKeyStoreAlias(String alias) {
        keyStoreAlias = alias;
    }

    /**
     * Получение алиаса ключевого контейнера.
     *
     * @return алиас ключевого контейнера.
     */
    public String getKeyStoreAlias() {
        return keyStoreAlias;
    }

    /**
     * Задание пароля для ключевого контейнера.
     *
     * @param password пароль для ключевого контейнера.
     */
    public void setKeyStorePassword(String password) {
        keyStorePassword = password;
    }

    /**
     * Получение пароля для ключевого контейнера.
     *
     * @return пароль для ключевого контейнера.
     */
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * Задание менеджера сертификатов.
     *
     * @param trustManager Менеджер сертификатов.
     */
    public void setTrustManager(TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    /**
     * Получение менеджера сертификатов.
     *
     * @return менеджер сертификатов.
     */
    public TrustManager getTrustManager() {

        if (trustManager == null) {
            return SSLConfiguration.TRUST_MANAGER_ALL;
        } // if

        return trustManager;

    }

    /**
     * Вывод в поток опций.
     *
     * @param stream Поток для вывода.
     */
    public void list(PrintStream stream) {

        stream.println("Host: " + host);
        stream.println("Port: " + port);
        stream.println("File: " + file);
        stream.println("HTTP 1.1: " + isHttp1_1);
        stream.println("Client auth: " + clientAuth);
        stream.println("Client count: " + clientCount);
        stream.println("Loading count: " + loadingCount);
        stream.println("Use apache 4.x: " + apache);
        stream.println("Allow all host names: " + allowAllHostnameVerifier);
        stream.println("File source dir: " + fileSource);
        stream.println("Store for downloaded files: " + fileStore);
        stream.println("Thread timeout: " + threadTimeout);
        stream.println("Server timeout: " + serverTimeout);
        stream.println("Trusted store type: " + trustStoreType);
        stream.println("Trusted store path: " + trustStore);
        stream.println("Trusted store password: " + trustStorePassword);
        stream.println("Key store type: " + keyStoreType);
        stream.println("Key store alias: " + keyStoreAlias);
        stream.println("Key store password: " + keyStorePassword);
        stream.println("Trust all: " + trustAll);

    }
}
