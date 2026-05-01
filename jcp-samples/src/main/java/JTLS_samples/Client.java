/**
 * $RCSfile$
 * version $Revision$
 * created 03.07.2007 10:07:20 by kunina
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

import ru.CryptoPro.ssl.SSLSocketFactoryImpl;
import ru.CryptoPro.ssl.gost.GostConstants;
import util.ResolveProvider;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Пример клиента. Переделан на явное получение контекста, т.к. есть глюки при
 * а) использовании ключей с одинаковым паролем для сервера и клиента (может
 * быть выбран серверный ключ для клиента и для сервера, даже если рядом лежит
 * клиентский с таким же паролем)
 * б) т.к. при создании контекста используется getDefault(), то есть вероятность
 * получить ранее созданный контекст, не имеющий контейнеров с доверенными сертификатами).
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class Client {

 /**/
public static final String hhtp_header_separator = "\r\n\r\n";
 /**/
public static final int DEFAULT_TIMEOUT = 1000;
 /**/
private int timeout = DEFAULT_TIMEOUT;
 /**/
private String host;
 /**/
private int port;
 /**/
private String testDir = null;
 /**/
private static final Logger log = Logger.getLogger("LOG");

/**
 * Необходимо ли оправить запрос HTTP 1.1.
  */
private boolean isHttp1_1 = false;

/**
 * Создание сокета по заданному порту и хосту.
 *
 * @param hostname хост
 * @param p порт
 */
public Client(String hostname, int p) {
    host = hostname;
    port = p;
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
 * Функция устанавливает HTTP 1.1
 * @param isSetHttp1_1
 */
public void setHttp1_1 (boolean isSetHttp1_1){
        isHttp1_1 = isSetHttp1_1;
    }

/**
 * Основная функция работы клиента.
 *
 * @param fileName имя файла
 * @return код ошибки или 0 в случае успеха
 * @throws IOException ошибки ввода-вывода
 */
public int get(String fileName, String outFileName, String testDir) throws Exception {
    this.testDir = testDir;
    SSLSocket soc = null;
    try {
        // вариант 1
        // final SSLSocketFactory sslFact =
        // (SSLSocketFactory) SSLSocketFactory.getDefault();
        // вариант 2
        final SSLSocketFactoryImpl sslFact = new SSLSocketFactoryImpl();
        soc = (SSLSocket) sslFact.createSocket(host, port);
        soc.setSoTimeout(timeout);
        // SSLParameters params = soc.getSSLParameters();
        // params.setEndpointIdentificationAlgorithm("https");
        // soc.setSSLParameters(params);
        return proc(soc, fileName, outFileName, false, false);
    } finally {
        if (soc != null)
            soc.close();
    }
}

    /**
     * Основная функция работы клиента.
     *
     * @param sslContext Контекст для подключения.
     * @param fileName имя файла
     * @return код ошибки или 0 в случае успеха
     * @throws IOException ошибки ввода-вывода
     */
    public int get(SSLContext sslContext, String fileName, String outFileName, String testDir) throws Exception {
        return get(sslContext, fileName, outFileName, testDir, false, false);
    }
/**
 * Основная функция работы клиента.
 *
 * @param sslContext Контекст для подключения.
 * @param fileName имя файла
 * @return код ошибки или 0 в случае успеха
 * @throws IOException ошибки ввода-вывода
 */
public int get(SSLContext sslContext, String fileName, String outFileName, String testDir, boolean separateThread, boolean isTestClose) throws Exception {
    this.testDir = testDir;
    SSLSocket soc = null;
    try {
        // вариант 1
        // final SSLSocketFactory sslFact = (SSLSocketFactory)
        // SSLSocketFactory.getDefault();
        //вариант 2
        // final SSLSocketFactoryImpl sslFact = new SSLSocketFactoryImpl();
        // вариант 3
        if (sslContext == null) {
            sslContext = SSLContext.getInstance("Default", "JTLS");
            // sslFact = new SSLSocketFactoryImpl();
        }
        // else
        final SSLSocketFactory sslFact = sslContext.getSocketFactory();
        soc = (SSLSocket) sslFact.createSocket(host, port);
        // Если используется Java CSP и при этом ГОСТ 2012
        // недоступен, то укажем явно только 3 сайфер-сюиты.
        //
        // if (!ResolveProvider.JCSPGost2012Enabled) {
        //     soc.setEnabledCipherSuites(new String[]{GostConstants.TLS_CIPHER_2001});
        // } // if
        //
        soc.setSoTimeout(timeout);
        return proc(soc, fileName, outFileName, separateThread, isTestClose);
    } finally {
        if (soc != null)
            soc.close();
    }
}

/**
 * Выполнение обмена данными с сервером.
 *
 * @param soc сокет
 * @param file имя файла
 * @param outfile файл для записи
 * @return код ошибки или 0 в случае успеха
 * @throws IOException ошибки ввода-вывода
 */
public int proc(Socket soc, String file, String outfile, boolean separateThread, boolean isTestClose) throws IOException {
    final InputStream in = soc.getInputStream();
    final OutputStream out = soc.getOutputStream();
    // отправка запроса
    final String req;
    if (isHttp1_1) {
        req = "GET /" + file + " HTTP/1.1\r\nHost: " + host + ":" + port + "\r\nUser-Agent: WebClient\r\nAccept: */*\r\nConnection: close\r\n\r\n";
    } else {
        req = "GET /" + file + " HTTP/1.0\r\n\r\n";
    }
    log.log(Level.INFO, " Client request: " + req);
    out.write(req.getBytes());
    out.flush();
    if (soc instanceof SSLSocket) {
        SSLSession session = ((SSLSocket) soc).getSession();
        log.log(Level.INFO, " Cipher suite: " + session.getCipherSuite());
        if (session.getLocalPrincipal() != null) {
            log.log(Level.INFO, "Local certificate: " + session.getLocalPrincipal());
        }
        if (session.getPeerPrincipal() != null) {
            log.log(Level.INFO, "Remote certificate: " + session.getPeerPrincipal());
        }
    }
    if (separateThread) {
        ReaderThread readerThread = new ReaderThread(soc.getInputStream());
        Thread thread1 = new Thread(readerThread);
        thread1.start();
        // преждевременное закрытие сокета
        if (isTestClose)
            soc.close();
    }

    else {
        // разбор ответа
        log.log(Level.INFO, " Client: parse answer");
        final String answer = new String(readHeader(in, hhtp_header_separator.getBytes()));
        int fileLength = 0;
        try {
            fileLength = parseAnswer(answer);
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
            return -1;
        }
        if (soc instanceof SSLSocket) {
            SSLSession session = ((SSLSocket) soc).getSession();
            if (session.getLocalPrincipal() != null) {
                log.log(Level.INFO, "Local certificate: " + session.getLocalPrincipal());
            }
        }
        // чтение
        final byte[] body = readBody(in, fileLength);
        // проверка
        boolean isread = true;
        byte[] buffer = body;
        if (testDir != null)
            try {
                buffer = readFile(testDir + File.separator + file);
                if (fileLength != body.length)
                    throw new IOException("Invalid length in HTTP:" + file);
                if (buffer.length != fileLength)
                    throw new IOException("Invalid file length:" + file);
                for (int i = 0; i < buffer.length; i++)
                    if (buffer[i] != body[i])
                        throw new IOException("Invalid file:" + file);
            } catch (IOException e) {
                isread = false;
                log.log(Level.INFO, " ClientVerify: " + e.toString());
                //log.log(Level.INFO, "Client:" + e.toString());
            }
        if (isread && outfile != null) {
            final String outPath = outfile + new File(file).getName();
            final FileOutputStream fos = new FileOutputStream(outPath);
            fos.write(buffer);
            fos.close();
            log.log(Level.INFO, "answer was writing to: " + outPath);
        } else {
            if (outfile != null) {
                final String outPath = outfile + ".html";
                final FileOutputStream fos = new FileOutputStream(outPath);
                String s = "<p align=\"left\">" +
                        "<font color=\"#FF9909\" size=\"5\" face=\"Times New Roman\">" +
                        "<u>ANSWER:</u>" +
                        "</font></p>";
                fos.write(s.getBytes());
                fos.write(answer.getBytes());
                s = "<p align=\"left\">" +
                        "<font color=\"#FF9909\" size=\"5\" face=\"Times New Roman\">" +
                        "<u>PAGE:</u>" +
                        "</font></p>";
                fos.write(s.getBytes());
                fos.write(buffer);
                fos.close();
                log.log(Level.INFO, "answer was writing to: " + outPath);
            }
        }
    }
    return 0;
}

/**
 * Чтение потока до конца заголовка.Может быть вызвано с new byte[] {(byte)' '}
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
    log.log(Level.FINE, " Try reading (Client.readHeader)");
    do {
        next = in.read();
        if (next == -1)
            throw new IOException(" Client: Error reading HTTP header");
        baos.write(next);
        if (next == end[conformity])
            conformity++;
        else
            conformity = 0;
    } while (conformity != end.length);
    return baos.toByteArray();
}

/**
 * Чтение известного количества байтов.
 *
 * @param in InputStream
 * @param len length
 * @return буфер
 * @throws IOException ошибки ввода-вывода
 */
public static byte[] readBody(InputStream in, int len) throws IOException {
    // Если есть размер сообщения (прочитан из хидеров), то используем его.
    if (len > 0) {
        final byte[] buf = new byte[len];
        int next;
        int pos = 0;
        while (pos != len) {
            next = in.read();
            if (next == -1) {
                throw new IOException(" Error reading HTTP body");
            } // if
            buf[pos++] = (byte) next;
        } // while
        return buf;
    } // if
    // Если размера нет (такое бывает), то читаем, пока не получим конец файла.
    else {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        while (true) {
            int next = in.read();
            if (next == -1) {
                // Ничего не прочитали, сразу конец файла.
                if (buf.size() == 0) {
                    throw new IOException(" Error reading HTTP body");
                } // if
                break;
            } // if
            buf.write((byte) next);
        } // while
        return buf.toByteArray();
    } // else
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
            if (len == -1)
                throw new IOException(" Client: Error reading file:" + name);
            total += len;
        } while (total != buffer.length);
    } finally {
        if (is != null) is.close();
    }
    return buffer;
}

/**
 * Разбор ответа сервера и извлечение длины файла
 *
 * @param str строка ответа
 * @return длина файла
 * @throws IOException ошибки ввода-вывода
 */
public static int parseAnswer(String str) throws IOException {
    final String[] split = str.split("\r\n");
    if (!split[0].equalsIgnoreCase("HTTP/1.0 200 OK") &&
        !split[0].equalsIgnoreCase("HTTP/1.1 200 OK"))
        throw new IOException(split[0]);
    // throw new IOException("Unknown answer");
    int len = -1;
    for (int i = 1; i < split.length; i++)
        if (split[i].startsWith("Content-Length:")) {
            final String ss = split[i].substring("Content-Length:".length()).trim();
            len = Integer.parseInt(ss);
            break;
        }
    return len;
}

}
