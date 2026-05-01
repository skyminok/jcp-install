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

import JTLS_samples.connection.SSLConfiguration;
import JTLS_samples.connection.SSLConnector;

import ru.CryptoPro.JCP.JCP;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import ComLine.ComLine;
import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.JCPLogger;
import ru.CryptoPro.ssl.ServerLicense;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;

/**
 * Сервер.
 *
 */
public class TLSServer extends Thread implements TLSBase {

    /**
     * Серверный сокет.
     */
    private SSLServerSocket serverSocket = null;
    /**
     * Таймаут ожидания чтения, ms.
     */
    protected int socketTimeout = 30 * 1000;
    /**
     * Таймаут ожидания сокета, ms.
     */
    protected int acceptTimeout = 180 * 60 * 1000; // 3 часа
    /**
     * Пул потоков сервера.
     */
    private final ExecutorService CLIENT_POOL =
        Executors.newFixedThreadPool(8);
    /**
     * Разделитель заголовков.
     */
    public static final String http_header_separator = "\r\n\r\n";
    /**
     * Папка сервера.
     */
    public final String serverWorkingDir;

    /**
     * Простая страница для передачи клиенту.
     * Содержит %s для указания клиента.
     */
    private static final String SAMPLE_PAGE = "<html>\n" +
        "\n" +
        "<head>\n" +
        "<meta http-equiv=\"Content-Language\" content=\"en-us\">\n" +
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">\n" +
        "<title>Server</title>\n" +
        "</head>\n" +
        "\n" +
        "<body>\n" +
        "\n" +
        "<p align=\"center\"><font color=\"#FF9999\" size=\"5\" face=\"Times New Roman\"><u><i>\n" +
        "<b>Sample page for %s</b></i></u></font></p>\n" +
        "\n" +
        "</body>\n" +
        "\n" +
        "</html>";

    /**
     * Страница завершения работы сервера.
     * Содержит %s для указания клиента,
     * кто завершил.
     */
    private static final String SHUTDOWN_PAGE = "<html>\n" +
        "\n" +
        "<head>\n" +
        "<meta http-equiv=\"Content-Language\" content=\"en-us\">\n" +
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">\n" +
        "<title>Server</title>\n" +
        "</head>\n" +
        "\n" +
        "<body>\n" +
        "\n" +
        "<p align=\"center\"><font color=\"#FF5050\" size=\"5\" face=\"Times New Roman\"><i>\n" +
        "<b>SERVER WILL SHUTDOWN AFTER THIS SESSION from %s</b></i></font></p>\n" +
        "\n" +
        "</body>\n" +
        "\n" +
        "</html>";

    /**
     * Конструктор.
     *
     * @param configuration Конфигурация TLS.
     * @param port Порт сервера.
     * @param workDir Папка сервера.
     * @param protocol Протокол TLS.
     * @param suites Список сюит.
     * @param cacheSize Размер кеша сессий.
     * @param sessionTO Время жизни сессии.
     * @param timeout Таймаут подключения.
     * @throws Exception
     */
    public TLSServer(SSLConfiguration configuration, int port, String workDir,
        String protocol, String[] suites, int cacheSize, int sessionTO, int timeout)
        throws Exception {

        // вариант 3
        SSLConnector sslConnector = new SSLConnector(configuration);
        sslConnector.prepare(true);

        SSLContext context = sslConnector.create(protocol);
        SSLSessionContext sessionContext = context.getServerSessionContext();

        if (cacheSize != -1) {
            sessionContext.setSessionCacheSize(cacheSize);
        }

        if (sessionTO != -1) {
            sessionContext.setSessionTimeout(sessionTO);
        }

        final SSLServerSocketFactory sslSrvFact = context.getServerSocketFactory();

        serverSocket = (SSLServerSocket) sslSrvFact.createServerSocket(port);
        serverSocket.setNeedClientAuth(configuration.needClientAuth());
        serverSocket.setSoTimeout(acceptTimeout); // это таймаут на accept

        if (suites != null) {
            serverSocket.setEnabledCipherSuites(suites);
        }

        serverWorkingDir = workDir;
        socketTimeout = timeout;

    }

    /**
     * Конструктор.
     *
     * @param sslContext Защищенный контекст.
     * @param clientAuth True, если требуется клиентская
     * аутентификация.
     * @param port Порт сервера.
     * @param workDir Папка сервера.
     * @param suites Список сюит.
     * @param cacheSize Размер кеша сессий.
     * @param sessionTO Время жизни сессии.
     * @param timeout Таймаут подключения.
     * @throws Exception
     */
    public TLSServer(SSLContext sslContext, boolean clientAuth, int port,
        String workDir, String[] suites, int cacheSize, int sessionTO,
        int timeout) throws Exception {

        SSLSessionContext sessionContext = sslContext.getServerSessionContext();

        if (cacheSize != -1) {
            sessionContext.setSessionCacheSize(cacheSize);
        }

        if (sessionTO != -1) {
            sessionContext.setSessionTimeout(sessionTO);
        }

        final SSLServerSocketFactory sslSrvFact = sslContext.getServerSocketFactory();

        serverSocket = (SSLServerSocket) sslSrvFact.createServerSocket(port);
        serverSocket.setNeedClientAuth(clientAuth);
        serverSocket.setSoTimeout(acceptTimeout); // это таймаут на accept

        if (suites != null) {
            serverSocket.setEnabledCipherSuites(suites);
        }

        serverWorkingDir = workDir;
        socketTimeout = timeout;

    }

    @Override
    protected void finalize() throws Throwable {

        try {
            interrupt();
        } finally {
            super.finalize();
        }

    }

    @Override
    public void interrupt() {

        JCP_LOG.log(Level.INFO, "Server thread is being interrupted.");
        super.interrupt(); // прерываем серверный поток

        if (serverSocket != null) {

            try {
                serverSocket.close();
            } catch (IOException e) {}

            serverSocket = null;

        } // if

        // Ждем обрабатывающие потоки.

        try {

            CLIENT_POOL.shutdownNow();
            CLIENT_POOL.awaitTermination(30, TimeUnit.SECONDS);

        } catch (InterruptedException e) {}

    }

    /**
     * Основная функция работы сервера.
     *
     */
    @Override
    public void run() {

        final String TAG = "[server-" + Thread.currentThread().getName() + "] :: ";
        JCP_LOG.log(Level.INFO, TAG + "thread has been started.");

        // Проверяем лицензию. Если ее нет - выходим.

        /*try {

            JCP_LOG.log(Level.INFO, TAG + "checking server license...");

            ServerLicense license = new ServerLicense();
            license.check(null);

            JCP_LOG.log(Level.INFO, TAG + "server license has been" +
                " checked successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            JCP_LOG.log(Level.SEVERE, TAG + e.toString());
            return; // ошибка
        }*/

        // Цикл сервера.

        JCP_LOG.log(Level.INFO, TAG + "starting server working circle...");
        while (!isInterrupted()) { // пока не прервали поток, ждем сокеты

            try {

                // Получаем клиентский сокет.
                //
                // Примечание: серверный сокет разорвать можно
                // только закрытием самого сокета, т.е. в interrupt.
                // Если клиент пришлет shutdown, то разрыв произойдет
                // в обработчике клиентского потока.
                //
                // Таймаут на accept задан в конструкторе.

                Socket clientSocket = serverSocket.accept(); // может периодически кидать исключения по таймату
                clientSocket.setSoTimeout(socketTimeout);

                InetAddress id = clientSocket.getInetAddress();
                String remoteClientName = id.getCanonicalHostName();
                String remoteClientIP = id.getHostAddress();

                JCP_LOG.log(Level.INFO, TAG + "socket has been accepted: "
                    + clientSocket + " from client: " + remoteClientName
                        + " [" + remoteClientIP + "]");

                // Запускаем поток обработки клиента. Поток сервера
                // может быть прерван в клиенте. Добавляем клиента
                // в пул.

                ClientWorkingThread clientThread = new ClientWorkingThread(
                    clientSocket, this);

                CLIENT_POOL.submit(clientThread);

            } catch (IOException e) { // предположительно, из-за accept()
                e.printStackTrace();
                JCP_LOG.log(Level.SEVERE, TAG + e.toString());
                // если поток прерван, то выйдем из цикла
            }

        } // while

        JCP_LOG.log(Level.INFO, TAG + "server working" +
            " circle has been stopped.");

    }

    /**
     * Класс для обработки клиентского сокета.
     */
    class ClientWorkingThread implements Runnable {

        /**
         * Клиентский сокет.
         */
        private final Socket clientSocket;

        /**
         * Серверный поток. Передется сюда, чтобы быть
         * прерванным, если клиент пришлет shutdown.
         */
        private final Thread serverThread;

        /**
         * Конструктор.
         *
         * @param clientSocket Клиентский сокет.
         * @param serverThread Серверный поток.
         */
        ClientWorkingThread(Socket clientSocket, Thread serverThread) {
            this.clientSocket = clientSocket;
            this.serverThread = serverThread;
        }

        @Override
        public void run() {

            final String NAME = Thread.currentThread().getName();
            final String TAG  = "[client-job-" + NAME + "] :: ";

            JCP_LOG.log(Level.FINE, TAG + "thread has been started.");

            InputStream in   = null;
            OutputStream out = null;

            try {

                // Обмен данными с клиентом.

                JCP_LOG.log(Level.FINE, TAG + "preparing streams...");

                in  = clientSocket.getInputStream();
                out = clientSocket.getOutputStream();

                // Чтение запроса клиента.

                JCP_LOG.log(Level.FINE, TAG + "reading client request...");

                final String req = new String(parseHeader(in,
                    http_header_separator.getBytes(), TAG));

                JCP_LOG.log(Level.FINE, TAG + "parsing client request...");
                final String fName = parseRequest(req, TAG);

                JCP_LOG.log(Level.FINE, TAG + "client is requesting" +
                    " remote file name: " + fName);

                final String filename = serverWorkingDir + File.separator + fName;
                JCP_LOG.log(Level.FINE, TAG + "local file name = " + fName);

                // При запросе "shutdown" прекращение работы сервера.

                boolean shutDown = "shutdown".equalsIgnoreCase(fName);
                JCP_LOG.log(Level.FINE, TAG + "server is sending answer...");

                if (shutDown) { // если закрытие - просто страницу

                    JCP_LOG.log(Level.INFO, TAG + "server is being shutdowned...");
                    String message = String.format(SHUTDOWN_PAGE, NAME);

                    String http_answer = "HTTP/1.0 200 OK\r\nContent-Length: "
                        + message.length() + "\r\n\r\n";

                    byte[] answer = message.getBytes();
                    out.write(http_answer.getBytes());
                    out.write(answer);

                } // if
                else { // иначе читаем запрошенный файл

                    FileInputStream fIn = null;
                    File localFile = new File(filename);

                    JCP_LOG.log(Level.FINE, TAG + "reading local file: " +
                        localFile.getAbsolutePath() + "...");

                    boolean first = true;
                    if (localFile.exists()) { // файл существует

                        // Отправка файла.

                        JCP_LOG.log(Level.FINE, TAG + "server is" +
                            " sending the file...");

                        try {

                            fIn = new FileInputStream(filename);
                            byte[] buffer = new byte[BUFFER_SIZE];

                            int readBytes;
                            while ((readBytes = fIn.read(buffer, 0, BUFFER_SIZE)) > 0) {

                                if (first) { // если первая порция данных - отправим заголовок

                                    // Загловок не содержит длину, т.к. она неизвестна.
                                    // Клиент должен читать поток до конца (без длины).

                                    String http_answer = "HTTP/1.0 200 OK\r\n\r\n";
                                    out.write(http_answer.getBytes());

                                    first = false;

                                } // if

                                out.write(buffer, 0, readBytes);

                            } // while

                        } catch (Exception e) {

                            // Если не смогли прочитать файл вообще и не
                            // успели отправить заголовки - просто шлем
                            // страницу. Если уже отправили и заголовок,
                            // и кусок файла, а потом сломались - кинем
                            // исключение.

                            JCP_LOG.log(Level.FINE, TAG + "server cannot " +
                                "read local file " + fName + ": " + e.getMessage());

                            if (first) { // заголовок не был отправлен

                                JCP_LOG.log(Level.FINE, TAG + "server is sending" +
                                    " a sample page after file reading has failed...");

                                String message = String.format(SAMPLE_PAGE, NAME);
                                byte[] data = message.getBytes();

                                String http_answer = "HTTP/1.0 200 OK\r\nContent-Length: "
                                    + data.length + "\r\n\r\n"; // с длиной

                                out.write(http_answer.getBytes());
                                out.write(data);

                            } // if
                            else {
                                throw e;
                            } // else

                        } finally {

                            if (fIn != null) {
                                try {
                                    fIn.close();
                                } catch (Exception e) {}
                            } // if

                        }

                    } // if
                    else { // файла нет

                        // Отправка страницы.

                        JCP_LOG.log(Level.FINE, TAG + "server is sending" +
                            " a sample page...");

                        String message = String.format(SAMPLE_PAGE, NAME);
                        byte[] data = message.getBytes();

                        String http_answer = "HTTP/1.0 200 OK\r\nContent-Length: "
                            + data.length + "\r\n\r\n"; // с длиной

                        out.write(http_answer.getBytes());
                        out.write(data);

                    } // else

                } // else

                // Прерываем цикл, следующего accept() не будет.

                if (shutDown) { // запрос на закрытие

                    JCP_LOG.log(Level.INFO, TAG + "this thread has" +
                        " interrupted the server thread (shutdown).");

                    serverThread.interrupt(); // прерываем работу и закрываем сокет сервера

                } // if

            } catch (Exception e) {
                e.printStackTrace();
                JCP_LOG.log(Level.SEVERE, TAG + e.getMessage());
            } finally {

                if (in != null) { // входящий поток
                    try {
                        in.close();
                    } catch (Exception e) {}
                } // if

                if (out != null) { // исходящий поток
                    try {
                        out.close();
                    } catch (Exception e) {}
                } // if

                try {
                    clientSocket.close(); // клиентский сокет
                } catch (Exception e) {}

            }

            JCP_LOG.log(Level.FINE, TAG + "thread has finished its job.");

        }

    }

    /**
     * Чтение потока до конца заголовка. Может быть вызвано
     * с new byte[] {(byte)' '}
     *
     * @param in входной поток
     * @param end конец заголовка
     * @return буфер (байтовый массив)
     * @throws IOException ошибки ввода-вывода
     */
    private static byte[] parseHeader(InputStream in, byte[] end,
        String tag) throws IOException {

        final ByteArrayOutputStream baos = new
            ByteArrayOutputStream();

        try {

            int conformity = 0;
            int next;

            do {

                next = in.read();

                if (next == -1) {
                    throw new IOException(tag + "error reading HTTP header.");
                } // if

                baos.write(next);

                if (next == end[conformity]) {
                    conformity++;
                } else {
                    conformity = 0;
                }

            } while (conformity != end.length);

        } finally {
            try {
                baos.close();
            } catch (Exception e) {}
        }

        return baos.toByteArray();

    }

    /**
     * Разбор запроса, проверка и извлечение имени файла.
     *
     * @param r запрос
     * @return имя файла
     * @throws IOException ошибки ввода-вывода
     */
    private static String parseRequest(String r, String tag)
        throws IOException {

        String filename = null;
        final String[] newStr = r.split(" ");

        if (!newStr[0].equals("GET")) {
            throw new IOException(tag + "unknown request: " + newStr[0]);
        } // if

        if (newStr[1].length() > 0 && newStr[1].charAt(0) == '/') {
            filename = newStr[1].substring(1);
        } // if

        return filename;

    }

    /**
     * Создание файла в папке.
     *
     * @param workDir Папка.
     * @throws Exception
     */
    private static void makeFile(String workDir) throws Exception {

        FileOutputStream os = null;
        FileOutputStream osDigest = null;

        try {

            final File file = new File(workDir, BIG_FILE_NAME);
            os = new FileOutputStream(file);

            final File fileDigest = new File(workDir, DIGEST_FILE_NAME);
            osDigest = new FileOutputStream(fileDigest);

            byte[] buffer = new byte[BUFFER_SIZE];
            MessageDigest md = MessageDigest.getInstance(DIGEST_NAME);

            for(int i = 0; i < BIG_FILE_FACTOR; i++) { // 100 Mb

                Arrays.fill(buffer, (byte) i);
                md.update(buffer);
                os.write(buffer);

            } // for

            byte[] digest = md.digest();
            osDigest.write(digest);

        } finally {

            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {}
            } // if

            if (osDigest != null) {
                try {
                    osDigest.close();
                } catch (Exception e) {}
            } // if

        }

    }

    /**
     * Запуск.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (ComLine.getFunc(ComLine.help, args)) {
            JCP_LOG.info(HELP_SERVER);
        } else if(ComLine.getFunc(GEN_FILE, args)) {

            // Инициализация провайдеров.

            JCPInit.initProviders(false);

            makeFile(ComLine.getValue(ComLine.servDir,
                args, new File(".").getCanonicalPath()));

        } else {

            try {

                // Заполнение таблицы свойств значениями параметров
                // командной строки или значениями по умолчанию при
                // отсутствии первых.

                final Properties ArgList = new Properties();

                // порт
                ArgList.setProperty(ComLine.PORT,
                    ComLine.getValue(ComLine.PORT, args, "443"));

                // protocol
                ArgList.setProperty(ComLine.protocol,
                    ComLine.getValue(ComLine.protocol, args,
                        ComLine.GOST_TLS));

                // keyStoreType
                ArgList.setProperty(ComLine.keyStoreType,
                    ComLine.getValue(ComLine.keyStoreType, args,
                        ComLine.HDImageStore));

                // trustStoreType
                ArgList.setProperty(ComLine.trustStoreType,
                    ComLine.getValue(ComLine.trustStoreType, args,
                        ComLine.CertStore));

                // trustStorePath
                ArgList.setProperty(ComLine.trustStorePath,
                    ComLine.getValue(ComLine.trustStorePath, args, null));

                // keyStoreAlias
                ArgList.setProperty(ComLine.keyStoreAlias,
                    ComLine.getValue(ComLine.keyStoreAlias, args, "null"));

                // keyStorePassword
                ArgList.setProperty(ComLine.keyStorePassword,
                    ComLine.getValue(ComLine.keyStorePassword, args, "null"));

                // trustStorePassword
                ArgList.setProperty(ComLine.trustStorePassword,
                    ComLine.getValue(ComLine.trustStorePassword, args, null));

                // authentication of client
                ArgList.setProperty(ComLine.auth,
                    ComLine.getBooleanValue(ComLine.auth, args, "false"));

                // server working dir
                ArgList.setProperty(ComLine.servDir,
                    ComLine.getValue(ComLine.servDir,
                        args, new File(".").getCanonicalPath()));

                // провайдер ключевого носителя и подписи
                ArgList.setProperty(ComLine.storeprovider,
                    ComLine.getValue(ComLine.storeprovider, args,
                        JCP.PROVIDER_NAME));

                // cipherSuites
                ArgList.setProperty(CIPHER_SUITES,
                    ComLine.getValue(CIPHER_SUITES, args, "null"));

                // sessionTO
                ArgList.setProperty(SESSION_TO,
                    ComLine.getValue(SESSION_TO, args, "null"));

                // socketTO
                ArgList.setProperty(SOCKET_TO, // ms
                    ComLine.getValue(SOCKET_TO, args, "3000000"));

                // cacheSize
                ArgList.setProperty(CACHE_SIZE,
                    ComLine.getValue(CACHE_SIZE, args, "null"));

                // cacheLimit (не используется)
                ArgList.setProperty(CACHE_LIMIT,
                    ComLine.getValue(CACHE_LIMIT, args, "null"));

                // protocol
                final String protocol = ArgList.getProperty(ComLine.protocol);

                // Инициализация провайдеров.

                final String provider = ArgList.getProperty(
                    ComLine.storeprovider);

                boolean useSspi = Arrays.asList(args).contains(USE_SSPI);
                JCPInit.initProviders(provider.equalsIgnoreCase(
                    DefaultProvider.JCSP_PROVIDER_NAME), useSspi);

                // Проверка типа хранилища. При неверном вводе присваивается
                // значение по умолчанию HDImageStore.

                final String ks;
                if (provider.equalsIgnoreCase(JCP.PROVIDER_NAME)) {

                    ks = ArgList.getProperty(ComLine.keyStoreType);

                    if (!ks.equalsIgnoreCase(ComLine.HDImageStore)
                        && !ks.equalsIgnoreCase(ComLine.FloppyStore)
                        && !ks.equalsIgnoreCase(ComLine.RTStore)
                        && !ks.equalsIgnoreCase(ComLine.J6CFStore)) {

                        ArgList.setProperty(ComLine.storetype, ComLine.HDImageStore);
                        JCP_LOG.info("Incorrect key store type: " + ks +
                            ". Value by default is appropriated: " +
                                ComLine.HDImageStore);

                    } // if

                } // if

                // Папка сервера.

                if (!new File(ArgList.getProperty(ComLine.servDir)).isDirectory()) {
                    ArgList.setProperty(ComLine.servDir, new File(".").getCanonicalPath());
                }

                // Конфигурация подключения.

                String trustStorePasswordString = ArgList.getProperty(ComLine.trustStorePassword);
                char[] trustStorePassword = trustStorePasswordString != null
                    ? trustStorePasswordString.toCharArray() : null;

                String keyStoreAliasString = ArgList.getProperty(ComLine.keyStoreAlias);
                keyStoreAliasString = (!keyStoreAliasString.equalsIgnoreCase("null"))
                    ? keyStoreAliasString : null;

                String keyStorePasswordString = ArgList.getProperty(ComLine.keyStorePassword);
                char[] keyStorePassword = (!keyStorePasswordString.equalsIgnoreCase("null"))
                    ? keyStorePasswordString.toCharArray() : null;

                final int sslPort = Integer.decode(ArgList.getProperty(ComLine.PORT));
                boolean clientAuth = false;

                if (ArgList.getProperty(ComLine.auth).equalsIgnoreCase("true"))
                    clientAuth = true;

                final int socketTO = Integer.decode(ArgList.getProperty(SOCKET_TO));
                String storeProvider = ArgList.getProperty(ComLine.storeprovider);
                SSLConfiguration sslConfig;

                if (storeProvider.equalsIgnoreCase("null")) {
                    sslConfig = new SSLConfiguration(ArgList.getProperty(ComLine.trustStoreType),
                        ArgList.getProperty(ComLine.trustStorePath),
                        trustStorePassword,
                        clientAuth,
                        ArgList.getProperty(ComLine.keyStoreType),
                        keyStoreAliasString,
                        keyStorePassword
                    );
                } else {
                    sslConfig = new SSLConfiguration(ArgList.getProperty(ComLine.trustStoreType),
                        ArgList.getProperty(ComLine.trustStorePath),
                        trustStorePassword,
                        clientAuth,
                        ArgList.getProperty(ComLine.keyStoreType),
                        keyStoreAliasString,
                        keyStorePassword,
                        storeProvider
                    );
                }

                int sessionTO = -1;

                if (!ArgList.getProperty(SESSION_TO).equalsIgnoreCase("null")) {
                    sessionTO = Integer.decode(ArgList.getProperty(SESSION_TO));
                }

                int cacheSize = -1;

                if (!ArgList.getProperty(CACHE_SIZE).equalsIgnoreCase("null")) {
                    cacheSize = Integer.decode(ArgList.getProperty(CACHE_SIZE));
                }

                String[] suites = null;
                String cipherSuites = ArgList.getProperty(CIPHER_SUITES);

                if (!cipherSuites.equalsIgnoreCase("null")) {
                    suites = cipherSuites.split(",");
                }

                // Сервер с аутентификацией auth.

                final TLSServer server = new TLSServer(
                    sslConfig, sslPort, ArgList.getProperty(ComLine.servDir),
                        protocol, suites, cacheSize, sessionTO, socketTO);

                server.start();

                if (!server.isAlive()) {
                    throw new IOException();
                } // if

            } catch (NullPointerException e) {
                e.printStackTrace();
                JCP_LOG.info(HELP_SERVER);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                JCP_LOG.info(HELP_SERVER);
            }

        } // else

    }

}
