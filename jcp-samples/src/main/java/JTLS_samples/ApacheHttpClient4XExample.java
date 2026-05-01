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
package JTLS_samples;

import ComLine.ComLine;

import JTLS_samples.connection.AbstractTLSExample;
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
import ru.CryptoPro.JCP.Util.JCPInit;
import util.ResolveProvider;

import javax.net.ssl.*;
import java.io.*;
import java.util.Arrays;
import java.util.Properties;

/**
 * Пример использования apache http client 4.x и JTLS.
 * Если вы используете соединение с двухсторонней аутентификацией, то ключ клиента
 * должен(!) находиться в папке контейнеров JCP (иначе ошибка 403).
 *
 * -host ref-x86-xp -port 443 -get index.html.en -save C:\save.html [-auth -allow]
 *
 * Параметры:
 *  -host для задания хоста подключения;
 *  -port для задания порта подключения;
 *  -get для задания ресурса;
 *  -save для задания пути и файла для сохранения запрашиваемого ресурса;
 *  -auth для задания клиентской аутентификации;
 *  -allow для отключения проверки соответствия адреса ресурса и CN серверного
 * сертификата;
 *  -keyStoreType для задания типа ключевого контейнера;
 *  -keyStoreAlias для задания алиаса контейнера (JCSP);
 *  -keyStorePassword для задания пароля к ключевому контейнеру;
 *  -trustStoreType для задания типа хранилища доверенных сертификатов;
 *  -trustStorePath для задания пути к хранилищу доверенных сертификатов;
 *  -trustStorePassword для задания пароля к хранилищу доверенных сертификатов;
 *  -help для вывода справки.
 *
 * Пример:
 *  java -Dcom.sun.security.enableCRLDP=true -Dcom.ibm.security.enableCRLDP=true
 *  JTLS_samples.ApacheHttpClient4XExample -host ref-x86-xp -port 443 -url index.html.en
 *  -save C:\index.html.en -auth -allow
 *  -trustStorePath c:\Projects\CryptoPro\CryptoProJCP\data\KEYS\local_ca\truststore.store
 *  -trustStorePassword 1 -keyStorePassword 1
 *
 * 17/08/2012
 *
 */
public class ApacheHttpClient4XExample extends AbstractTLSExample {

    /**
     * Защищенный контекст соединения.
     */
    private  SSLContext sslContext = null;

    /**
     * Порт соединения.
     */
    private int sslPort = 443;

    /**
     * Ресурс для скачивания.
     */
    private String urlAddress = null;

    /**
     * Нужно проверить соответствие CN сертификата адресу хоста.
     */
    private boolean allowAllHostnameVerifier = false;

    /**
     * Таймаут ожидания чтения/записи, msec.
     */
    private int readWriteTimeout = THREAD_TIMEOUT * 1000;

    /**
     * Таймаут подключения, msec.
     */
    private int connectionTimeout = THREAD_TIMEOUT * 1000;

    /**
     * Http-клиент.
     */
    private HttpClient httpClient = null;

    /**
     * Конструктор.
     *
     */
    public ApacheHttpClient4XExample() {
        ;
    }

    /**
     * Конструктор.
     *
     * @param url - Адрес для соединения.
     * @param port - Номер порта.
     * @param context - SSL контекст соединения.
     * @param allowAll - True, чтобы не проверять соответствие
     * CN сертификата адресу хоста.
     */
    public ApacheHttpClient4XExample(String url, int port, SSLContext context, boolean allowAll) {
        urlAddress = url;
        sslPort = port;
        sslContext = context;
        allowAllHostnameVerifier = allowAll;
        init();
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
     * Задание порта для подключения.
     *
     * @param port Порт подключения.
     */
    public void setSslPort(int port) {
        sslPort = port;
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
     * @param allow True, если следует проверять назначение сертификата.
     */
    public void setAllowAllHostnameVerifier(boolean allow) {
        allowAllHostnameVerifier = allow;
    }

    /**
     * Инициализация параметров соединения и создание http-клиента.
     *
     */
    public void init() {

        SSLSocketFactory socketFactory = allowAllHostnameVerifier
            ? new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
            : new SSLSocketFactory(sslContext);

        // Регистрируем HTTPS схему.

        Scheme httpsScheme = new Scheme("https", sslPort, socketFactory);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(httpsScheme);

        // Параметры соединения.

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setSoTimeout(params, readWriteTimeout);
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setStaleCheckingEnabled(params,false);
        ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);
        httpClient = new DefaultHttpClient(cm, params);

    }

    /**
     * Задание таймаута ожидания чтения/записи.
     *
     * @param timeout Время в милисекундах.
     */
    public void setReadWriteTimeout(int timeout) {
        readWriteTimeout = timeout;
    }

    /**
     * Задание таймаута подключения.
     *
     * @param timeout Время в милисекундах.
     */
    public void setConnectionTimeout(int timeout) {
        connectionTimeout = timeout;
    }

    /**
     * Запуск примера.
     *
     * @param outputStream - Поток для сохранения вывода. Если null, то сохранять не надо.
     * @param shutDown True, если следует завершить работу менеджера.
     * @throws Exception
     */
    public void execute(OutputStream outputStream, boolean shutDown) throws Exception {

        // GET-запрос.

        HttpGet httpget = new HttpGet(urlAddress);
        HttpResponse response = httpClient.execute(httpget);
        HttpEntity entity = response.getEntity();

        System.out.println("----------------------------------------");
        System.out.println(response.getStatusLine());

        int status = response.getStatusLine().getStatusCode();
        if (status  != 200) {
            throw new Exception("Bad http response status " + status);
        }

        if (entity != null && outputStream != null) {

            // Получаем размер заголовка.
            InputStream is = entity.getContent();

            BufferedReader in = new BufferedReader(new InputStreamReader(is, "windows-1251"));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outputStream, "windows-1251"));

            try {

                // Выводим ответ.
                String line;

                while((line = in.readLine()) != null) {
                    out.write(line);
                }

                out.flush();

            }
            catch (Exception e) {
                throw e;
            }
            finally {

                if (shutDown) {
                    httpClient.getConnectionManager().shutdown();
                } // if

                in.close();

            }

        } // if

        EntityUtils.consume(entity);

    }

    /**
     * Дополнительная функция для добавления специфических
     * клиентских аргументов в общий список.
     */
    public void putAdditionalClientParams() {

        argumentMap.put(PARAM_SAVE,  "path to save downloaded page (def: \"\")");
        argumentMap.put(PARAM_AUTH,  "add if server requires client auth");
        argumentMap.put(PARAM_ALLOW, "add to disable check if host is in certificate");

    }

    /**
     * Дополнительная функция для добавления специфических
     * серверных аргументов в общий список.
     */
    public void putAdditionalServerParams() {
        ;
    }

    /**
     * Проверка аргументов, работа примера.
     *
     * @param args Аргументы командной строки.
     * @param trustManager Менеджер сертификатов.
     * @throws Exception
     */
    public void main0(String[] args, TrustManager trustManager) throws Exception {

        // Вывод справки.
        if (ComLine.getFunc(ComLine.help, args) || args.length == 0) {
            System.out.println( help(argumentMap, Arrays.asList(PARAM_CLIENT) ));
            return;
        } // if

        final Properties ArgList = new Properties();

        ArgList.setProperty(PROTOCOL, ComLine.getValue(PROTOCOL, args, ComLine.GOST_TLS));
        ArgList.setProperty(PARAM_HOST, ComLine.getValue(PARAM_HOST, args, "127.0.0.1"));
        ArgList.setProperty(PARAM_PORT, ComLine.getValue(PARAM_PORT, args, "443"));
        ArgList.setProperty(PARAM_GET,  ComLine.getValue(PARAM_GET,  args, "default.htm"));
        ArgList.setProperty(PARAM_SAVE, ComLine.getValue(PARAM_SAVE, args, ""));

        FileOutputStream fOutFile = null;

        if (ArgList.getProperty(PARAM_SAVE).length() != 0) {
            fOutFile = new FileOutputStream(ArgList.getProperty(PARAM_SAVE));
        } // if

        boolean auth = ComLine.getFunc(PARAM_AUTH, args);
        boolean allow = ComLine.getFunc(PARAM_ALLOW, args);
        boolean trustAll = ComLine.getFunc(PARAM_TRUST_ALL, args);

        /* Параметры ключевого контейнера и хранилища сертификатов */

        ArgList.setProperty(ComLine.keyStoreType,
            ComLine.getValue(ComLine.keyStoreType, args, ComLine.HDImageStore));

        ArgList.setProperty(ComLine.keyStoreAlias,
            ComLine.getValue(ComLine.keyStoreAlias, args, "null"));

        ArgList.setProperty(ComLine.trustStoreType,
            ComLine.getValue(ComLine.trustStoreType, args, ComLine.CertStore));

        ArgList.setProperty(ComLine.trustStorePath,
            ComLine.getValue(ComLine.trustStorePath, args, "null"));

        ArgList.setProperty(ComLine.keyStorePassword,
            ComLine.getValue(ComLine.keyStorePassword, args, "null"));

        ArgList.setProperty(ComLine.trustStorePassword,
            ComLine.getValue(ComLine.trustStorePassword, args, "null"));

        String keyStoreAlias = null;
        char[] keyStorePassword = null;

        if (!"null".equalsIgnoreCase(ArgList.getProperty(ComLine.keyStoreAlias))) {
            keyStoreAlias = ArgList.getProperty(ComLine.keyStoreAlias);
        } // if

        if (!"null".equalsIgnoreCase(ArgList.getProperty(ComLine.keyStorePassword))) {
            keyStorePassword = ArgList.getProperty(ComLine.keyStorePassword).toCharArray();
        } // if

        showSettings(ArgList, null);
        String protocol = ArgList.getProperty(PROTOCOL);

        // Конфигурация подключения.

        String trustStorePath = null;
        char[] trustStorePassword = null;

        if (!"null".equalsIgnoreCase(ArgList.getProperty(ComLine.trustStorePath))) {
            trustStorePath = ArgList.getProperty(ComLine.trustStorePath);
        } // if
        else {
            throw new Exception(ComLine.trustStorePath + " must be not null.");
        } // else

        if (!"null".equalsIgnoreCase(ArgList.getProperty(ComLine.trustStorePassword))) {
            trustStorePassword = ArgList.getProperty(ComLine.trustStorePassword).toCharArray();
        } // if

        SSLConfiguration sslConfig = new SSLConfiguration(
            ArgList.getProperty(ComLine.trustStoreType),
            trustStorePath,
            trustStorePassword,
            auth,
            ArgList.getProperty(ComLine.keyStoreType),
            keyStoreAlias,
            keyStorePassword
        );

        sslConfig.setTrustAll(trustAll);
        sslConfig.setTrustManager(trustManager);

        SSLConnector connector = new SSLConnector(sslConfig);
        connector.prepare(false);

        SSLContext sslContext = connector.create(protocol);
        final int port = Integer.valueOf(ArgList.getProperty(PARAM_PORT));

        // Полный адрес.

        final String remoteSource = "https://" + ArgList.getProperty(PARAM_HOST) + ":" + port + "/" + ArgList.getProperty(PARAM_GET);
        System.out.println("Remote address: " + remoteSource);

        // Параметры подключения.

        setUrlAddress(remoteSource);
        setSslPort(port);
        setAllowAllHostnameVerifier(allow);
        setSslContext(sslContext);

        // Выполняем пример.

        init();
        execute(fOutFile, true);

        if (fOutFile != null) {
            fOutFile.close();
        } // if

    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(ResolveProvider.JCSPEnabled);
        ApacheHttpClient4XExample example = new ApacheHttpClient4XExample();
        example.main0(args, null);
    }

    /**
     *
     * @param args
     * @param trustManager Менеджер сертификатов.
     * @throws Exception
     */
    public static void main(String[] args, TrustManager trustManager) throws Exception {
        ApacheHttpClient4XExample example = new ApacheHttpClient4XExample();
        example.main0(args, trustManager);
    }

}
