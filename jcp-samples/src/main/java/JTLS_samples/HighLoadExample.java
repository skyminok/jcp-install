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
import JTLS_samples.connection.*;
import ru.CryptoPro.JCP.Util.DefaultProviders;
import ru.CryptoPro.JCP.Util.JCPInit;
import util.ResolveProvider;
import util.Tools;

import javax.net.ssl.TrustManager;
import java.util.*;

/**
 *
 * Пример создания нагрузки на веб-сервер.
 *
 * -client -host 192.168.1.10 -port 8443 -get default.htm -t 10 -n 10 -source "C:\source" -store "C:\store"
 * [-external -trace -ct X -apache4]
 *
 * Наиболее приоритетные команды:
 *  -server и -client
 * Они могут быть в одной строке.
 *
 * У клиента могут быть:
 *  -host и -port для подключения и -get для скачивания;
 *  -t для задания количества потоков (количество клиентов) и -n для задания количества
 * запросов;
 *  -apache4 для использования библиотек apache http client 4 вместо внутреннего Client;
 *  -source для задания папки с файлами для их передачи локальным сервером и для сравнения
 * полученных файлов с исходными;
 *  -store для задания папки сохранения получаемых файлов;
 *  -external для задания флага, что запущенный сервер - это не локальный внутренний сервер
 * из примера (-server), а, например, локальный томкат;
 *  -ct для задания максимального времени работы потока клиента.
 *
 * У сервера (не реализован) может быть:
 *  -name для задания имени локального внутреннего сервера (ip) и -listen для задания порта
 * прослушивания;
 *  -st для задания максимального времени работы локального сервера.
 *
 * Общие для всех параметры:
 *  -trace означает подробный вывод в лог;
 *  -keyStoreType для задания типа ключевого контейнера;
 *  -keyStoreAlias для задания алиаса контейнера (Java CSP);
 *  -keyStorePassword для задания пароля к ключевому контейнеру;
 *  -trustStoreType для задания типа хранилища доверенных сертификатов;
 *  -trustStorePath для задания пути к хранилищу доверенных сертификатов;
 *  -trustStorePassword для задания пароля к хранилищу доверенных сертификатов;
 *  -help для вызова справки.
 *
 * Пример:
 *
 *  java -Dcom.sun.security.enableCRLDP=true -Dcom.ibm.security.enableCRLDP=true JTLS_samples.HighLoadExample
 *  -client -host ref-x86-xp -port 443 -get "auth.htm" -t 5 -n 50 -source c:\Projects\CryptoPro\CryptoProJCP\data
 *  -store c:\Projects\CryptoPro\CryptoProJCP\temp\ -external -apache4
 *  -trustStorePath "c:\Projects\CryptoPro\CryptoProJCP\data\KEYS\local_ca\truststore.store" -trustStorePassword 1
 *  -keyStorePassword 1 -trace
 *
 *  23/04/2013
 *
 */
public class HighLoadExample extends AbstractTLSExample {

    /**
     * Количество потоков по умолчанию.
     */
    private static final int CLIENT_THREAD_COUNT = 2;

    /**
     * Количество запросов на поток по умолчанию.
     */
    private static final int REQUEST_PER_THREAD_COUNT = 2;

    /**
     * SSL-порт по умолчанию.
     */
    private static final int SSL_PORT = 443;

    /**
     * Вывод информации о скорости выполнения (оп/сек).
     */
    public static boolean trace = true;

    /**
     * Дополнительная функция для добавления специфических
     * клиентских аргументов в общий список.
     */
    public void putAdditionalClientParams() {

        argumentMap.put(PARAM_CLIENT_TIMEOUT,      "client thread's timeout (def: " + THREAD_TIMEOUT + " sec)");
        argumentMap.put(PARAM_T,                   "count of client threads (def: " + CLIENT_THREAD_COUNT + ")");
        argumentMap.put(PARAM_N,                   "count of client requests per thread (def: " + REQUEST_PER_THREAD_COUNT + ")");
        argumentMap.put(PARAM_FILE_SOURCE,         "directory provides files for server to send to client (def: null)");
        argumentMap.put(PARAM_FILE_STORE,          "directory to save downloaded files (def: null)");
        argumentMap.put(PARAM_EXTERNAL,            "add if server is local tomcat");
        argumentMap.put(PARAM_APACHE_HTTP_CLIENT4, "add if apache http client 4.x is as client");
        argumentMap.put(PARAM_TRACE,               "add if need to show trace (todo)");

    }

    /**
     * Дополнительная функция для добавления специфических
     * серверных аргументов в общий список.
     */
    public void putAdditionalServerParams() {

        argumentMap.put(PARAM_SERVER,         "");
        argumentMap.put(PARAM_NAME,           "local server name (def: \"127.0.0.1\")");
        argumentMap.put(PARAM_LISTEN,         "local server port to listen (def: " + SSL_PORT + ")");
        argumentMap.put(PARAM_SERVER_TIMEOUT, "thread timeout for local server (def: " + THREAD_TIMEOUT + " sec)");

    }

    /**
     * Проверка аргументов, работа примера.
     *
     * @param args Аргументы командной строки.
     * @param trustManager Менеджер сертификатов.
     */
    private void main0(String[] args, TrustManager trustManager) throws Exception {

        // Вывод справки.
        if (ComLine.getFunc(ComLine.help, args) || args.length == 0) {
            System.out.println(help( argumentMap, Arrays.asList(PARAM_SERVER, PARAM_CLIENT) ));
            return;
        } // if

        final Properties ArgList = new Properties();

        /* Параметры локального внутреннего сервера */

        // TODO доделать запуск локального сервера
        // boolean server = ComLine.getFunc(PARAM_SERVER, args);
        //
        // TODO пока только клиент
        // if (server) {
        //     throw new Exception("Server configuration not supported, use " + PARAM_CLIENT);
        // } // if

        ArgList.setProperty(PARAM_NAME,           ComLine.getValue(PARAM_NAME,           args, "127.0.0.1"));
        ArgList.setProperty(PARAM_LISTEN,         ComLine.getValue(PARAM_LISTEN,         args, String.valueOf(SSL_PORT)));
        ArgList.setProperty(PARAM_SERVER_TIMEOUT, ComLine.getValue(PARAM_SERVER_TIMEOUT, args, String.valueOf(THREAD_TIMEOUT)));

        /* Параметры клиента для подключения к серверу */

        // TODO пока только клиент
        boolean client = true; // ComLine.getFunc(PARAM_CLIENT, args);

        ArgList.setProperty(PROTOCOL,             ComLine.getValue(PROTOCOL,             args, ComLine.GOST_TLS));
        ArgList.setProperty(PARAM_HOST,           ComLine.getValue(PARAM_HOST,           args, "127.0.0.1"));
        ArgList.setProperty(PARAM_PORT,           ComLine.getValue(PARAM_PORT,           args, String.valueOf(SSL_PORT)));
        ArgList.setProperty(PARAM_GET,            ComLine.getValue(PARAM_GET,            args, "default.htm"));
        ArgList.setProperty(PARAM_FILE_SOURCE,    ComLine.getValue(PARAM_FILE_SOURCE,    args, "null"));
        ArgList.setProperty(PARAM_FILE_STORE,     ComLine.getValue(PARAM_FILE_STORE,     args, "null"));
        ArgList.setProperty(PARAM_T,              ComLine.getValue(PARAM_T,              args, String.valueOf(CLIENT_THREAD_COUNT)));
        ArgList.setProperty(PARAM_N,              ComLine.getValue(PARAM_N,              args, String.valueOf(REQUEST_PER_THREAD_COUNT)));
        ArgList.setProperty(PARAM_CLIENT_TIMEOUT, ComLine.getValue(PARAM_CLIENT_TIMEOUT, args, String.valueOf(THREAD_TIMEOUT)));

        boolean external   = ComLine.getFunc(PARAM_EXTERNAL,            args);
        boolean useApache4 = ComLine.getFunc(PARAM_APACHE_HTTP_CLIENT4, args);
        boolean isHttp1_1  = ComLine.getFunc(PARAM_HTTP_1_1,            args);
        boolean trustAll   = ComLine.getFunc(PARAM_TRUST_ALL,           args);
        trace = ComLine.getFunc(PARAM_TRACE,                            args);

        /* Параметры ключевого контейнера и хранилища сертификатов */

        ArgList.setProperty(ComLine.keyStoreType,       ComLine.getValue(ComLine.keyStoreType,       args, ComLine.HDImageStore));
        ArgList.setProperty(ComLine.keyStoreAlias,      ComLine.getValue(ComLine.keyStoreAlias,      args, "null"));
        ArgList.setProperty(ComLine.trustStoreType,     ComLine.getValue(ComLine.trustStoreType,     args, ComLine.CertStore));
        ArgList.setProperty(ComLine.trustStorePath,     ComLine.getValue(ComLine.trustStorePath,     args, "null"));
        ArgList.setProperty(ComLine.keyStorePassword,   ComLine.getValue(ComLine.keyStorePassword,   args, "null"));
        ArgList.setProperty(ComLine.trustStorePassword, ComLine.getValue(ComLine.trustStorePassword, args, "null"));

        // TODO пока только клиент к некоему серверу
        if (client) {
            if (loading(ArgList, external, useApache4, isHttp1_1, trustAll, trustManager)) {
                throw new Exception("Failure during fulfilment!");
            } // if
        } // if
        else {
            throw new Exception("Server configuration not supported, use " + PARAM_CLIENT);
        } // else

    }

    /**
     * Создание конфигурации по аргументам.
     *
     * @param args Аргументы.
     * @param external True, если используется локальный сервер (tomcat).
     * @param apache4 True, если следует использовать apache http client 4.
     * @param trustManager Менеджер сертификатов.
     * @return True, если выполнение успешно.
     */
    private static boolean loading(Properties args, boolean external,
        boolean apache4, boolean isHttp1_1, boolean trustAll, TrustManager
        trustManager) throws Exception {

        String protocol = args.getProperty(PROTOCOL);
        String fileSource = null;
        String fileStore = null;

        if (!"null".equalsIgnoreCase(args.getProperty(PARAM_FILE_SOURCE))) {
            fileSource = args.getProperty(PARAM_FILE_SOURCE);
        } // if

        if (!"null".equalsIgnoreCase(args.getProperty(PARAM_FILE_STORE))) {
            fileStore = args.getProperty(PARAM_FILE_STORE);
        } // if

        ClientConfiguration config = new ClientConfiguration(
            args.getProperty(PARAM_HOST),
            Integer.valueOf(args.getProperty(PARAM_PORT)),
            args.getProperty(PARAM_GET),
            fileSource,
            fileStore
        );

        config.setUseClientAuth(true);
        config.setExternalWebServer(external);
        config.setUseApache(apache4);
        config.setIsHttp1_1(isHttp1_1);

        config.setTrustAll(trustAll);
        config.setAllowAllHostnameVerifier(trustAll);
        config.setTrustManager(trustManager);

        // Проверка типа хранилища.
        final String ks;
        ks = args.getProperty(ComLine.keyStoreType);
        String sp = ResolveProvider.JCSPEnabled ?
                DefaultProviders.ALTERNATIVE_PROVIDER_NAME : DefaultProviders.DEFAULT_PROVIDER_NAME;

        String resultingKeyStoreType = ComLine.verifyKeyStoreTypeJavaTLS(ks,sp);
        if (!ks.equalsIgnoreCase(resultingKeyStoreType))
        {
            args.setProperty(ComLine.keyStoreType, resultingKeyStoreType);
            System.out.println("Incorrect key store type: " + ks +
                    ". Value by default is appropriated: " + resultingKeyStoreType);
        }

        if (!"null".equalsIgnoreCase(args.getProperty(ComLine.keyStoreAlias))) {
            config.setKeyStoreAlias(args.getProperty(ComLine.keyStoreAlias));
        } // if

        if (!"null".equalsIgnoreCase(args.getProperty(ComLine.keyStorePassword))) {
            config.setKeyStorePassword(args.getProperty(ComLine.keyStorePassword));
        } // if

        if (!"null".equalsIgnoreCase(args.getProperty(ComLine.trustStorePath))) {
            config.setTrustStore(args.getProperty(ComLine.trustStorePath));
        } // if
        else {
            throw new Exception(ComLine.trustStorePath + " must be not null.");
        } // else

        if (!"null".equalsIgnoreCase(args.getProperty(ComLine.trustStorePassword))) {
            config.setTrustStorePassword(args.getProperty(ComLine.trustStorePassword));
        } // if

        config.setTrustStoreType(args.getProperty(ComLine.trustStoreType));
        config.setKeyStoreType(args.getProperty(ComLine.keyStoreType));

        config.setThreadTimeout(Integer.valueOf(args.getProperty(PARAM_CLIENT_TIMEOUT)));
        config.setServerTimeout(Integer.valueOf(args.getProperty(PARAM_SERVER_TIMEOUT)));
        config.setClientCount(Integer.valueOf(args.getProperty(PARAM_T)));
        config.setLoadingCount(Integer.valueOf(args.getProperty(PARAM_N)));
        config.setAllowAllHostnameVerifier(true);

        showSettings(args, config);
        return loading(config, protocol);

    }

    /**
     * Функция запуска теста.
     *
     * @param clientConfig Настройки для создания клиентского и серверного подключений.
     * @return True, если выполнение успешно.
     */
    public static boolean loading(ClientConfiguration clientConfig, String protocol) {

        boolean failed = false;

        if (clientConfig.getHost() == null || clientConfig.getHost().length() == 0) {
            clientConfig.setHost("localhost");
        } // if

        long totalThreadTime = 0;
        Server localServer = null;

        try {

            // Конфигурация для создания SSL-контекста сервера.

            SSLConfiguration sslServerConfig = clientConfig.isFull()
                ? new SSLConfiguration(ResolveProvider.JCSPEnabled, clientConfig)
                : new SSLConfiguration(ResolveProvider.JCSPEnabled, clientConfig.getUseClientAuth());

            // Запускаем локальный сервер, если требуется.

            if (!clientConfig.getExternalWebServer() && clientConfig.isLocal()) {

                localServer = new Server();
                localServer.create(sslServerConfig, clientConfig.getPort(), clientConfig.getFileSource(), protocol, false);
                localServer.setTimeout(clientConfig.getServerTimeout());
                localServer.start();

                if (!localServer.isAlive()) {
                    throw new Exception("Local server is not running.");
                } // if

            } // if

            final int clientWaitTimeout = 10 * 1000;
            if (clientConfig.getClientCount() == 0) {

                System.out.println("Wait for clients " + clientWaitTimeout/1000 + " sec...");
                Thread.sleep(clientWaitTimeout);

            } // if

            // Создаем клиентские потоки.

            ClientThread[] clientGroup = new ClientThread[clientConfig.getClientCount()];

            // Запускаем клиентские потоки - задаем имя потока, удаленный хост, скачиваемый файл и др.

            for (int i = 0; i < clientConfig.getClientCount(); i++) {
                clientGroup[i] = new ClientThread("HttpClient_#" + i, clientConfig, trace, protocol);
                clientGroup[i].start();
            } // for

            // Ждем клиентские потоки не более 10 минут.

            for (int i = 0; i < clientConfig.getClientCount(); i++) {
                clientGroup[i].join(clientConfig.getThreadTimeout());
            } // for

            // Останавливаем клиентские потоки.

            for (int i = 0; i < clientConfig.getClientCount(); i++) {

                if (clientGroup[i].isAlive()) {
                    clientGroup[i].stop();
                } // if

                // Хоть один, но вылетел. Не пугаемся, считаем и потом сообщим, что были ошибки.

                if (clientGroup[i].failed()) {
                    failed = true;
                    System.out.println("Thread " + clientGroup[i].getThreadName() + " failed.");
                } // if

                long threadTime = clientGroup[i].getExecutionTime();

                if (trace) {

                    // Среднее время и скорость по одному потоку.

                    System.out.println("---------- Thread " + clientGroup[i].getThreadName() + " ----------");
                    Tools.printInfo("Average speed of execution: ", (double) (clientConfig.getLoadingCount() * 1000) / (threadTime + 1), "op/s");
                    Tools.printInfo("Average time of an operation: ", (double) threadTime / (clientConfig.getLoadingCount() * 1000), "s");

                } // if

                totalThreadTime += threadTime;

            } // for

            if (clientConfig.getClientCount() > 0) {

                if (trace) {

                    // Среднее время и скорость по всем потокам.

                    System.out.println("\n-------------------------------------");

                    Tools.printInfo("Average speed of execution: ", (double) (clientConfig.getClientCount() * clientConfig.getLoadingCount() * 1000) / totalThreadTime, "op/s");
                    Tools.printInfo("Average time of an operation: ", (double) totalThreadTime / (clientConfig.getClientCount() * clientConfig.getLoadingCount() * 1000), "s");

                    System.out.println("\n-------------------------------------");

                    Tools.printInfo("Total speed of execution: ",
                        (double) (clientConfig.getClientCount() * clientConfig.getLoadingCount() * 1000)
                            / (totalThreadTime / clientConfig.getClientCount()), "op/s");

                } // if
            } // if

        } catch (Exception e) {
            e.printStackTrace();
            failed = true;
        }

        try {

            // Если был запущен локальный сервер, то останавливаем его.

            if (!clientConfig.getExternalWebServer() && clientConfig.isLocal()) {

                if (localServer == null) {
                    throw new Exception("Local http server is not running at the test.");
                } // if

                localServer.stop();
                Thread.sleep(100);

            } // if

        } catch (Exception e) {
            e.printStackTrace();
            failed = true;
        }

        return failed;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(ResolveProvider.JCSPEnabled);
        HighLoadExample example = new HighLoadExample();
        example.main0(args, null);
    }

    /**
     *
     * @param args
     * @param trustManager Менеджер сертификатов.
     */
    public static void main(String[] args, TrustManager trustManager) throws Exception {
        HighLoadExample example = new HighLoadExample();
        example.main0(args, trustManager);
    }

}
