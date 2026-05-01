/**
 * $RCSfile$
 * version $Revision$
 * created 03.07.2007 12:17:39 by kunina
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
package JTLS_samples;

import JTLS_samples.connection.SSLConfiguration;
import JTLS_samples.connection.SSLConnector;
import ru.CryptoPro.ssl.SSLServerSocketFactoryImpl;

import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Пример сервера.
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class Server implements Runnable {

 /**/
private ServerSocket serverSocket = null;
 /**/
private boolean alive = false;
 /**/
private int timeout = 1000;
 /**/
private Thread thread = null;
 /**/
public static final String http_header_separator = "\r\n\r\n";
 /**/
public String workingDir;

/**
 * Создание сервера с параметром аутентификация клиента (auth).
 *
 * @param port порт
 * @param auth setNeedClientAuth(auth)
 * @throws IOException ошибки ввода-вывода
 */
public void create(int port, boolean auth, String workDir) throws Exception {

    // // вариант 1
    //
    // final SSLServerSocketFactory sslSrvFact =
    //     (SSLServerSocketFactory) SSLServerSocketFactory
    //         .getDefault();

    // вариант 2

    final SSLServerSocketFactoryImpl sslSrvFact = new SSLServerSocketFactoryImpl();
    serverSocket = sslSrvFact.createServerSocket(port);

    ((SSLServerSocket) serverSocket).setNeedClientAuth(auth);
    workingDir = workDir;

}

/**
 * Создание сервера с параметром аутентификация клиента (auth).
 *
 * @param configuration Настройки подключения.
 * @param port порт
 * @throws IOException ошибки ввода-вывода
 */
public void create(SSLConfiguration configuration, int port,
    String workDir, String protocol, boolean noAlias)
    throws Exception {

    // вариант 1
    // final SSLServerSocketFactory sslSrvFact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

    // вариант 2
    // final SSLServerSocketFactoryImpl sslSrvFact = new SSLServerSocketFactoryImpl();

    // вариант 3
    SSLConnector sslConnector = new SSLConnector(configuration);

    if (noAlias) {
        sslConnector.prepareNoAlias(true); // не передаем алиас ключа
    }
    else {
        sslConnector.prepare(true);
    }

    final SSLServerSocketFactory sslSrvFact = sslConnector.create(protocol).getServerSocketFactory();

    serverSocket = sslSrvFact.createServerSocket();
    serverSocket.bind(new InetSocketAddress(port));

    ((SSLServerSocket) serverSocket).setNeedClientAuth(configuration.needClientAuth());
    workingDir = workDir;

}

/**
 * @throws Throwable
 */
protected void finalize() throws Throwable {
    if (serverSocket != null) serverSocket.close();
    super.finalize();
}

/**
 * Работает ли сервер.
 *
 * @return работает ли сервер
 */
public boolean isAlive() {
    return alive;
}

/**
 * Функция устанавливает timeout на чтение.
 *
 * @param t timeout
 */
public void setTimeout(int t) {
    timeout = t;
}

/**
 * Открытие соединения.
 *
 * @throws IOException ошибки ввода-вывода
 */
public void start() throws IOException {
    serverSocket.setSoTimeout(timeout);
    thread = new Thread(this);
    // thread.setPriority(Thread.MIN_PRIORITY);
    alive = true;
    thread.start();
}

/**
 * Остановка сервера.
 *
 * @throws IOException ошибки ввода-вывода
 */
public void stop() throws IOException {
    if (serverSocket != null) {
        serverSocket.close();
        serverSocket = null;
    }
    while (alive) {
        try {
            alive = false;
            thread.join();
        } catch (InterruptedException ex) {
            // ignore ex
            alive = true;
        }
    }
}

/**
 * Основная функция работы сервера.
 */
public void run() {
    Socket soc = null;
    while (alive) {
        try {
            try {
                soc = serverSocket.accept();
                soc.setSoTimeout(timeout);
                if (alive) proc(soc);
            } catch (SocketTimeoutException e) {
                // ignore e, check alive only
            } finally {
                if (soc != null) soc.close();
            }

        } catch (IOException ex) {
            Logger.getLogger("LOGGER").log(Level.SEVERE, "Server error:" + ex.toString());
            // ex.printStackTrace();
            // alive = false;
        }
    }
}

/**
 * Обмен данными с клиентом.
 *
 * @param soc сокет
 * @throws IOException ошибки ввода-вывода
 */
private void proc(Socket soc) throws IOException {
    final InputStream in = soc.getInputStream();
    final OutputStream out = soc.getOutputStream();
    // чтение запроса
    Logger.getLogger("LOGGER").log(Level.INFO, "Server: read request");
    final String req = new String(readHeader(in, http_header_separator.getBytes()));
    Logger.getLogger("LOGGER").log(Level.INFO, "Server: parse request");
    final String fName = parseRequest(req);
    final String filename = workingDir + File.separator + fName;
    // при запросе "shutdown" прекращение работы сервера
    if ("shutdown".equals(fName)) {
        alive = false;
        // soc.close();
    }
    Logger.getLogger("LOGGER").log(Level.INFO, "Server: read file");
    String info;
    final String s = "<html>\n" +
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
            "<b>Sample page</b></i></u></font></p>\n" +
            "\n" +
            "</body>\n" +
            "\n" +
            "</html>";
    final String ss = "<html>\n" +
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
            "<b>SERVER WILL SHUTDOWN AFTER THIS SESSION</b></i></font></p>\n" +
            "\n" +
            "</body>\n" +
            "\n" +
            "</html>";
    byte[] full = s.getBytes();
    if (!alive) full = ss.getBytes();
    try {
        full = readFile(filename);
        info = "HTTP/1.0 200 OK\r\nContent-Length: " + full.length + "\r\n\r\n";
    } catch (Exception e) {
        // info = "HTTP/1.0 404 not found\r\n \r\n\r\n";
        info = "HTTP/1.0 200 OK\r\nContent-Length: " + full.length + "\r\n\r\n";
        // info = "HTTP/1.1 404 Not Found\r\nContent-Length: "+ full.length + "\r\n\r\n"+s;
    }
    // отправка
    Logger.getLogger("LOGGER").log(Level.INFO, "Server: answer");
    out.write(info.getBytes());
    out.write(full);
    in.close();
    out.close();
}

/**
 * Чтение потока до конца заголовка. Может быть вызвано с new byte[] {(byte)' '}
 *
 * @param in входной поток
 * @param end конец заголовка
 * @return буфер (байтовый массив)
 * @throws IOException ошибки ввода-вывода
 */
public static byte[] readHeader(InputStream in, byte[] end) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int conformity = 0;
    int next;
    Logger.getLogger("LOGGER").log(Level.FINE, "Try reading (Server.readHeader)");
    do {
        next = in.read();
        if (next == -1) throw new IOException("Server: Error reading HTTP header");
        baos.write(next);
        if (next == end[conformity]) conformity++; else conformity = 0;
    } while (conformity != end.length);
    return baos.toByteArray();
}

/**
 * Разбор запроса, проверка и извлечение имени файла.
 *
 * @param r запрос
 * @return имя файла
 * @throws IOException ошибки ввода-вывода
 */
public static String parseRequest(String r) throws IOException {
    String filename = null;
    final String[] newStr = r.split(" ");
    if (!newStr[0].equals("GET")) throw new IOException("Server: Unknown request");
    if (newStr[1].length() > 0 && newStr[1].charAt(0) == '/') filename = newStr[1].substring(1);
    return filename;
}

/**
 * Чтение файла
 *
 * @param name имя
 * @return буфер
 * @throws IOException ошибки ввода-вывода
 */
public static byte[] readFile(String name) throws IOException {
    byte[] buffer;
    FileInputStream is = null;
    try {
        final File file = new File(name);
        is = new FileInputStream(file);
        buffer = new byte[(int) file.length()];
        int len;
        int total = 0;
        do {
            len = is.read(buffer, total, buffer.length - total);
            if (len == -1) throw new IOException("Server: Error reading file:" + name);
            total += len;
        } while (total != buffer.length);
    } finally {
        if (is != null) is.close();
    }
    return buffer;
}
}
