/**
 * Copyright 2004-2019 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package ru.CryptoPro.tlsTest;

import ComLine.ComLine;
import JTLS_samples.connection.SSLConfiguration;
import JTLS_samples.connection.SSLConnector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCP.tools.HexString;
import ru.CryptoPro.JCP.Util.JCPInit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * Клиент.
 *
 */
public class TLSClient implements TLSBase,
    Callable<Map.Entry<Long, Integer>> {

    /**
     * Таймаут потока.
     */
    public static final int THREAD_TIMEOUT = 10 * 60;
    /**
     * Защищенный контекст соединения.
     */
    private SSLContext sslContext = null;
    /**
     * Порт соединения.
     */
    private int sslPort = 443;
    /**
     * Ресурс для скачивания.
     */
    private String urlAddress = null;
    /**
     * Ресурс для скачивания.
     */
    private String urlDigestAddress = null;
    /**
     * Файл для записи.
     */
    private String outFile = null;
    /**
     * Нужно прочитать файл большого объема.
     */
    private boolean getBigFile = false;
    /**
     * Количество запросов к серверу.
     */
    private int requestCount = 0;
    /**
     * Сюиты.
     */
    private String[] cipherSuites = null;
    /**
     * Нужно проверить соответствие CN сертификата адресу хоста.
     */
    private boolean allowAllHostnameVerifier = false;
    /**
     * Таймаут ожидания чтения, ms.
     */
    private int socketTimeout = THREAD_TIMEOUT * 1000;
    /**
     * Http-клиент.
     */
    private HttpClient httpClient = null;
    /**
     * Конструктор.
     */
    public TLSClient() {
        ;
    }

    /**
     * Задание URL-ресурса для скачивания.
     *
     * @param address Адрес ресурса.
     */
    public void setUrlAddress(String address) {
        urlAddress = address;
    }

    /**
     * Задание URL-ресурса для проверхи хэша.
     *
     * @param urlAddress Адрес.
     */
    public void setUrlDigestAddress(String urlAddress){
        urlDigestAddress = urlAddress;
    }

    /**
     * Функция устанавливает признак того, что нужно
     * прочитать большой файл данных.
     *
     * @param isBigFile True, если нужно прочитать файл.
     */
    public void setIsBigFile(boolean isBigFile) {
        getBigFile = isBigFile;
    }

    /**
     * Задание файла для записи.
     *
     * @param outputFile Адрес ресурса.
     */
    public void setOutFile(String outputFile) {
        outFile = outputFile;
    }

    /**
     * Задание порта для подключения.
     *
     * @param port Порт подключения.
     */
    public void setSslPort(int port) {
        sslPort = port;
    }

    /**
     * Задание списка сюит.
     *
     * @param suites Список сюит.
     */
    public void setCipherSuites(String[] suites){
        cipherSuites = suites;
    }

    /**
     * Задание SSL-контекста для защищенного подключения.
     *
     * @param context контекст подключения.
     */
    public void setSslContext(SSLContext context) {
        sslContext = context;
    }

    /**
     * Выполнение проверки имени удаленного хоста.
     *
     * @param allow True, если следует проверять
     * назначение сертификата.
     */
    public void setAllowAllHostnameVerifier(boolean allow) {
        allowAllHostnameVerifier = allow;
    }

    /**
     * Инициализация параметров соединения и создание http-клиента.
     */
    public void init() {

        String[] protocols = sslContext.getDefaultSSLParameters().getProtocols();

        SSLSocketFactory socketFactory = new SSLSocketFactory(
            sslContext,
            protocols,
            cipherSuites,
            allowAllHostnameVerifier ? SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER : null
        );

        // Регистрируем HTTPS схему.

        Scheme httpsScheme = new Scheme("https", sslPort, socketFactory);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(httpsScheme);

        // Параметры соединения.

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setSoTimeout(params, socketTimeout);
        HttpConnectionParams.setConnectionTimeout(params, socketTimeout);
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
        httpClient = new DefaultHttpClient(cm, params);

    }

    /**
     * Задание таймаута подключения.
     *
     * @param timeout Время в милисекундах.
     */
    public void setSocketTimeout(int timeout) {
        socketTimeout = timeout;
    }

    /**
     * Задание количества запросов.
     *
     * @param count Количество запросов.
     */
    public void setRequestCount(int count) {
        requestCount = count;
    }

    /**
     * Запуск примера.
     *
     * @param outputStream - Поток для сохранения вывода.
     * Если null, то сохранять не надо.
     * @param shutDown True, если следует завершить работу
     * менеджера.
     * @throws Exception
     */
    public void execute(OutputStream outputStream, boolean shutDown, String tag)
        throws Exception {

        JCP_LOG.log(Level.FINE, tag + "executing request...");

        try {

            // GET-запрос.

            JCP_LOG.log(Level.FINE, tag + "sending request to "
                + urlAddress + "...");

            HttpGet httpget = new HttpGet(urlAddress);
            HttpResponse response = httpClient.execute(httpget);

            JCP_LOG.log(Level.FINE, tag + "checking response...");

            HttpEntity entity = response.getEntity();
            int status = response.getStatusLine().getStatusCode();

            if (status != 200) {
                throw new Exception(tag + "bad http response status: " + status);
            } // if

            if (entity != null) {

                // Получаем размер заголовка.

                JCP_LOG.log(Level.FINE, tag + "parsing response...");
                InputStream is = entity.getContent();

                // Длина может быть -1, тогда читаем поток до конца.

                int length = (int)entity.getContentLength();
                JCP_LOG.log(Level.FINE, tag + "response size: " + length);

                JCP_LOG.log(Level.FINE, tag + "initializing digest" +
                    " (big file = " + getBigFile + ")...");

                // Хеш нужен только в случае большого файла.

                MessageDigest md = null;

                if (getBigFile) {
                    md = MessageDigest.getInstance(DIGEST_NAME);
                } // if

                byte[] buffer = new byte[BUFFER_SIZE];
                JCP_LOG.log(Level.FINE, tag + "reading input stream...");

                int read;
                while ((read = is.read(buffer, 0, BUFFER_SIZE)) > 0) {

                    if (getBigFile) {
                        md.update(buffer, 0, read);
                    } // if
                    else if (outputStream != null) {
                        outputStream.write(buffer, 0, read);
                    } // else

                } // while

                JCP_LOG.log(Level.FINE, tag + "destroying entity...");
                EntityUtils.consume(entity);

                if (getBigFile) {

                    JCP_LOG.log(Level.FINE, tag + "preparing digest...");
                    byte[] digest = md.digest();

                    if (outputStream != null) {
                        outputStream.write(digest);
                    } // if

                    JCP_LOG.log(Level.FINE, tag + "expected digest: "
                        + HexString.toHexNoSpaces(digest));

                    // Получаем хэш от сервера.

                    JCP_LOG.log(Level.FINE, tag + "sending request to "
                        + urlDigestAddress + "...");

                    HttpGet httpGetDigest = new HttpGet(urlDigestAddress);
                    HttpResponse responseDigest = httpClient.execute(httpGetDigest);

                    JCP_LOG.log(Level.FINE, tag + "checking response...");

                    HttpEntity entityDigest = responseDigest.getEntity();
                    int statusDigest = responseDigest.getStatusLine().getStatusCode();

                    if (statusDigest != 200) {
                        throw new Exception(tag + "bad http digest response status: "
                            + statusDigest);
                    } // if

                    if (entityDigest == null) {
                        throw new Exception(tag + "no entity in http digest response");
                    } // if

                    JCP_LOG.log(Level.FINE, tag + "parsing response...");
                    InputStream isDigest = entityDigest.getContent();

                    // Длина может быть -1, тогда читаем поток до конца.

                    int lengthDigest = (int)entityDigest.getContentLength();
                    JCP_LOG.log(Level.FINE, tag + "response size: " + lengthDigest);

                    byte[] bufferDigest = new byte[DIGEST_SIZE];
                    int digestRead = isDigest.read(bufferDigest);

                    if (digestRead != DIGEST_SIZE) {
                        throw new Exception(tag + "invalid digest size: "
                            + digestRead);
                    } // if

                    JCP_LOG.log(Level.FINE, tag + "destroying entity...");
                    EntityUtils.consume(entityDigest);

                    JCP_LOG.log(Level.FINE, tag + "actual digest: "
                        + HexString.toHexNoSpaces(bufferDigest));

                    if (!Array.compare(digest, bufferDigest)) {
                        throw new Exception(tag + "digest is invalid");
                    } // if

                } // if

            } // if

        } finally {

            if (shutDown) {
                httpClient.getConnectionManager().shutdown();
            } // if

        }

        JCP_LOG.log(Level.FINE, tag + "executing completed.");

    }

    /**
     * Функция получения файла.
     *
     */
    @Override
    public Map.Entry<Long, Integer> call() {

        final String NAME = Thread.currentThread().getName();
        final String TAG  = "[client-" + NAME + "] :: ";

        final long ID = Thread.currentThread().getId();
        int status = 0;

        JCP_LOG.log(Level.FINE, TAG + "thread #" + ID
            + " has been started.");

        for (int i = 0; (i < requestCount) && (status == 0); i++) {

            FileOutputStream fOutFile = null;

            try {

                init();

                if (outFile != null) {
                    fOutFile = new FileOutputStream(outFile);
                }

                // Если big, то можно читать в память, хешируя.

                execute(fOutFile, true, TAG);
                if (fOutFile != null) {

                    fOutFile.close();
                    fOutFile = null;

                }

            } catch (Exception e) {

                e.printStackTrace();
                JCP_LOG.log(Level.SEVERE, TAG + e.getMessage());
                status = -1;

            } finally {

                if (fOutFile != null) {
                    try {
                        fOutFile.close();
                    } catch (Exception e) {
                        status = -1;
                    }
                } // if

            }

        } // for

        JCP_LOG.log(Level.FINE, TAG + "thread has finished its job.");
        return new AbstractMap.SimpleEntry<Long, Integer>(ID, status);

    }

    /**
     * Client [-port port] [-server serverName] [-keyStoreType HDImageStore]
     * [-trustStoreType HDImageStore] -trustStorePath C:/*.* -trustStorePassword
     * trust_pass -keyStorePassword key_pass [-fileget gettingFileName]
     * [-fileout outputFilePath]
     * <br>
     * </DD> <DL> <DT><b> -port </b>  <DD>порт сервера <DD>(по умолчанию
     * 443)</DD>
     * <DT><b> -server </b> <DD>имя сервера <DD>(по умолчанию localhost)<br>
     * <DT><b>
     * -keyStoreType </b> <DD>тип ключевого носителя HDImageStore (жесткий
     * диск), FloppyStore (дискета), J6CFStore (карточки),
     * RutokenStore (Рутокен)
     * <DD>(по умолчанию HDImageStore)</DD> <DT><b> -trustStoreType </b>
     * <DD>тип
     * носителя для хранилища доверенных сертификатов HDImageStore (жесткий
     * диск), FloppyStore (дискета) <DD>(по умолчанию HDImageStore)</DD> <DT><b>
     * -trustStorePath </b>
     * <DD>путь к хранилищу доверенных сертификатов</DD> <DT><b>
     * -trustStorePassword
     * </b> <DD>пароль на хранилище доверенных сертификатов</DD> <DT><b>
     * -keyStorePassword </b>  <DD>пароль на ключ</DD> <DT><b> -fileget</b>
     * <DD>имя ресурса <DD>(по умолчанию index.html)</DD> <DT><b> -fileout </b>
     * <DD>путь к файлу вывода <DD>(по умолчанию out.html)<br></DT> </DL>
     *
     * @param args аргументы командной строки
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        main(args, null);
    }

    /**
     * Client [-port port] [-server serverName] [-keyStoreType HDImageStore]
     * [-trustStoreType HDImageStore] -trustStorePath C:/*.* -trustStorePassword
     * trust_pass -keyStorePassword key_pass [-fileget gettingFileName]
     * [-fileout outputFilePath]
     * <br>
     * </DD> <DL> <DT><b> -port </b>  <DD>порт сервера <DD>(по умолчанию
     * 443)</DD>
     * <DT><b> -server </b> <DD>имя сервера <DD>(по умолчанию localhost)<br>
     * <DT><b>
     * -keyStoreType </b> <DD>тип ключевого носителя HDImageStore (жесткий
     * диск), FloppyStore (дискета), OCFStore или J6CFStore (карточки),
     * RutokenStore (Рутокен)
     * <DD>(по умолчанию HDImageStore)</DD> <DT><b> -trustStoreType </b>
     * <DD>тип
     * носителя для хранилища доверенных сертификатов HDImageStore (жесткий
     * диск), FloppyStore (дискета) <DD>(по умолчанию HDImageStore)</DD> <DT><b>
     * -trustStorePath </b>
     * <DD>путь к хранилищу доверенных сертификатов</DD> <DT><b>
     * -trustStorePassword
     * </b> <DD>пароль на хранилище доверенных сертификатов</DD> <DT><b>
     * -keyStorePassword </b>  <DD>пароль на ключ</DD> <DT><b> -fileget</b>
     * <DD>имя ресурса <DD>(по умолчанию index.html)</DD> <DT><b> -fileout </b>
     * <DD>путь к файлу вывода <DD>(по умолчанию out.html)<br></DT> </DL>
     *
     * @param args аргументы командной строки
     * @param trustManager менеджер сертификатов
     * @throws IOException
     */
    public static void main(String[] args, TrustManager trustManager) throws Exception {

        // Запоминаем текущий уровень логирования.

        Level currentJcpLevel = JCP_LOG.getLevel();
        Level currentTlsLevel = TLS_LOG.getLevel();

        if (ComLine.getFunc(ComLine.help, args)) {
            JCP_LOG.info(HELP_CLIENT);
        } // if
        else {

            final Properties ArgList = new Properties();
            boolean logEnabled = Arrays.asList(args).contains(LOG_ENABLED);

            // Отключение логирования.

            if (!logEnabled) {

                JCP_LOG.info("Log is disabled.");

                JCP_LOG.setLevel(Level.OFF);
                TLS_LOG.setLevel(Level.OFF);

                Handler consoleHandler = new ConsoleHandler();
                consoleHandler.setFormatter(new SimpleFormatter());

                consoleHandler.setLevel(Level.OFF);

                JCP_LOG.addHandler(consoleHandler);
                TLS_LOG.addHandler(consoleHandler);

            } // if
            else {

                JCP_LOG.info("Log is enabled.");

                JCP_LOG.setLevel(Level.ALL);
                TLS_LOG.setLevel(Level.ALL);

            } // else

            ExecutorService THREAD_POOL = Executors.newFixedThreadPool(16);

            try {

                // Заполнение таблицы свойств значениями параметров
                // командной строки или значениями по умолчанию при
                // отсутствии первых.

                // порт
                ArgList.setProperty(ComLine.PORT, ComLine.getValue(ComLine.PORT, args, "443"));

                // хост
                ArgList.setProperty(ComLine.SERVER, ComLine.getValue(ComLine.SERVER, args, "localhost"));

                // protocol
                ArgList.setProperty(ComLine.protocol, ComLine.getValue(ComLine.protocol, args, ComLine.GOST_TLS));

                // keyStoreType
                ArgList.setProperty(ComLine.keyStoreType, ComLine.getValue(ComLine.keyStoreType, args, ComLine.HDImageStore));

                // trustStoreType
                ArgList.setProperty(ComLine.trustStoreType, ComLine.getValue(ComLine.trustStoreType, args, ComLine.CertStore));

                // trustStorePath
                ArgList.setProperty(ComLine.trustStorePath, ComLine.getValue(ComLine.trustStorePath, args, null));

                // keyStoreAlias
                ArgList.setProperty(ComLine.keyStoreAlias, ComLine.getValue(ComLine.keyStoreAlias, args, "null"));

                // keyStorePassword
                ArgList.setProperty(ComLine.keyStorePassword, ComLine.getValue(ComLine.keyStorePassword, args, "null"));

                // trustStorePassword
                ArgList.setProperty(ComLine.trustStorePassword, ComLine.getValue(ComLine.trustStorePassword, args, null));

                // Get file (filename)
                ArgList.setProperty(ComLine.fileget, ComLine.getValue(ComLine.fileget, args, "null"));

                // file for output (fileout path)
                ArgList.setProperty(ComLine.fileout, ComLine.getValue(ComLine.fileout, args, "null"));

                // authentication of client
                ArgList.setProperty(ComLine.auth, ComLine.getBooleanValue(ComLine.auth, args, "false"));

                // get big file
                ArgList.setProperty(GET_BIG_FILE, ComLine.getBooleanValue(GET_BIG_FILE, args, "false"));

                // провайдер ключевого носителя и подписи
                ArgList.setProperty(ComLine.storeprovider, ComLine.getValue(ComLine.storeprovider, args, JCP.PROVIDER_NAME));

                // cipherSuites
                ArgList.setProperty(CIPHER_SUITES, ComLine.getValue(CIPHER_SUITES, args, "null"));

                // socketTO, ms
                ArgList.setProperty(SOCKET_TO, ComLine.getValue(SOCKET_TO, args, "3000000"));

                // threadCount
                ArgList.setProperty(THREAD_COUNT, ComLine.getValue(THREAD_COUNT, args, "10"));

                // threadTO
                ArgList.setProperty(THREAD_TO, // ms
                    ComLine.getValue(THREAD_TO, args, "3000000"));

                // requestCount
                ArgList.setProperty(REQUEST_COUNT, ComLine.getValue(REQUEST_COUNT, args, "10"));

                // Проверка типа хранилища. При неверном вводе присваивается
                // значение по умолчанию HDImageStore.

                final String ks;
                ks = ArgList.getProperty(ComLine.keyStoreType);

                // Инициализация провайдеров.

                final String provider = ArgList.getProperty(ComLine.storeprovider);
                if (provider.equalsIgnoreCase(JCP.PROVIDER_NAME)) {

                    if (ks.equalsIgnoreCase(ComLine.HDImageStore)) {
                    } else if (ks.equalsIgnoreCase(ComLine.FloppyStore)) {
                    } else if (ks.equalsIgnoreCase(ComLine.RTStore)) {
                    } else if (ks.equalsIgnoreCase(ComLine.J6CFStore)) {
                    } else if (ks.equalsIgnoreCase(ComLine.NO_STORE)) {
                    } else {

                        ArgList.setProperty(ComLine.keyStoreType, ComLine.HDImageStore);
                        JCP_LOG.info("Incorrect key store type: " + ks +
                            ". Value by default is appropriated: " +
                                ComLine.HDImageStore);

                    }

                } // if

                boolean useSspi = Arrays.asList(args).contains(USE_SSPI);
                JCPInit.initProviders(provider.equalsIgnoreCase(
                        DefaultProvider.JCSP_PROVIDER_NAME), useSspi);

                //=============================================================================
                // Конфигурация подключения.

                final String protocol = ArgList.getProperty(ComLine.protocol);
                final int sslPort = Integer.decode(ArgList.getProperty(ComLine.PORT));

                final String sslHost = ArgList.getProperty(ComLine.SERVER);
                boolean clientAuth = false;

                if (ArgList.getProperty(ComLine.auth).equalsIgnoreCase("true")) {
                    clientAuth = true;
                } // if

                String trustStorePasswordString = ArgList.getProperty(ComLine.trustStorePassword);
                char[] trustStorePassword = trustStorePasswordString != null ? trustStorePasswordString.toCharArray() : null;

                String keyStoreAliasString = ArgList.getProperty(ComLine.keyStoreAlias);
                keyStoreAliasString = (!keyStoreAliasString.equalsIgnoreCase("null")) ? keyStoreAliasString : null;

                String keyStorePasswordString = ArgList.getProperty(ComLine.keyStorePassword);
                char[] keyStorePassword = (!keyStorePasswordString.equalsIgnoreCase("null")) ? keyStorePasswordString.toCharArray() : null;

                final int socketTO = Integer.decode(ArgList.getProperty(SOCKET_TO));
                final int threadCount = Integer.decode(ArgList.getProperty(THREAD_COUNT));

                final int threadTO = Integer.decode(ArgList.getProperty(THREAD_TO));
                final int requestCount = Integer.decode(ArgList.getProperty(REQUEST_COUNT));

                String[] suites = null;
                String cipherSuites = ArgList.getProperty(CIPHER_SUITES);

                if (!cipherSuites.equalsIgnoreCase("null")) {
                    suites = cipherSuites.split(",");
                } // if

                String storeProvider = ArgList.getProperty(ComLine.storeprovider);
                SSLConfiguration sslConfig;

                if (storeProvider.equalsIgnoreCase("null")) {
                    sslConfig = new SSLConfiguration(
                        ArgList.getProperty(ComLine.trustStoreType),
                        ArgList.getProperty(ComLine.trustStorePath),
                        trustStorePassword,
                        clientAuth,
                        ArgList.getProperty(ComLine.keyStoreType),
                        keyStoreAliasString,
                        keyStorePassword
                    );
                } else {
                    sslConfig = new SSLConfiguration(
                        ArgList.getProperty(ComLine.trustStoreType),
                        ArgList.getProperty(ComLine.trustStorePath),
                        trustStorePassword,
                        clientAuth,
                        ArgList.getProperty(ComLine.keyStoreType),
                        keyStoreAliasString,
                        keyStorePassword,
                        storeProvider
                    );
                }

                sslConfig.setTrustAll(true);
                sslConfig.setTrustManager(trustManager);

                SSLConnector connector = new SSLConnector(sslConfig);
                connector.prepare(false);

                boolean getBigFile = false;

                if (ArgList.getProperty(GET_BIG_FILE).equalsIgnoreCase("true")) {
                    getBigFile = true;
                } // if

                // Полный адрес.

                String remoteSource = "https://" + sslHost + ":" + sslPort + "/";
                String outFileName = ArgList.getProperty(ComLine.fileout);

                if (outFileName.equalsIgnoreCase("null")) {
                    outFileName = null;
                } // if

                // Параметры подключения.

                List<TLSClient> clients = new ArrayList<TLSClient>(threadCount);
                for (int i = 0; i < threadCount; i++) {

                    TLSClient client = new TLSClient();
                    client.setSocketTimeout(socketTO);
                    client.setSslPort(sslPort);
                    client.setAllowAllHostnameVerifier(true);
                    client.setSslContext(connector.create(protocol));
                    client.setRequestCount(requestCount);
                    client.setCipherSuites(suites);

                    if (getBigFile) {
                        client.setIsBigFile(true);
                        client.setUrlAddress(remoteSource +  BIG_FILE_NAME);
                        client.setUrlDigestAddress(remoteSource + DIGEST_FILE_NAME);
                    } // if
                    else {
                        String fileName = ArgList.getProperty(ComLine.fileget);
                        if (fileName.equalsIgnoreCase("null")) {
                            client.setUrlAddress(remoteSource);
                        }  // if
                        else {
                            client.setUrlAddress(remoteSource + fileName);
                        } // else
                    } // else

                    if (outFileName != null) {
                        client.setOutFile(i + "_" + outFileName);
                    } // if

                    clients.add(client);

                } // for

                // Ждем клиентские потоки не более threadTO.

                List<Future<Map.Entry<Long, Integer>>> results = THREAD_POOL
                    .invokeAll(clients, threadTO, TimeUnit.MILLISECONDS);

                // Анализируем клиентские потоки.

                Vector<Long> errors = new Vector<Long>(0);
                for (Future<Map.Entry<Long, Integer>> result : results) {

                    // Останавливаем поток.

                    if (!result.isDone()) {
                        result.cancel(true);
                    } // if

                    Map.Entry<Long, Integer> state = result.get();

                    if (state.getValue() != 0) {
                        errors.add(state.getKey());
                    } // if

                } // for

                // Если были ошибки - тест не прошел.

                if (!errors.isEmpty()) {

                    String errorMessage = "Error in threads: ";

                    for (int i = 0; i < errors.size(); i++){
                        long threadID = errors.get(i);
                        errorMessage += threadID + ", ";
                    }

                    throw new IOException(errorMessage);

                } // if

            } catch (NullPointerException e) {
                e.printStackTrace();
                JCP_LOG.setLevel(currentJcpLevel);
                JCP_LOG.info(HELP_CLIENT);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                JCP_LOG.setLevel(currentJcpLevel);
                JCP_LOG.info(HELP_CLIENT);
            } finally {
                // Закрываем все.
                THREAD_POOL.shutdown();
                JCP_LOG.setLevel(currentJcpLevel);
                TLS_LOG.setLevel(currentTlsLevel);
            }

        } // else

    }

}
