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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Пример клиента.
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class SeparateClient {

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
 * Создание сокета по заданному порту и хосту.
 *
 * @param hostname хост
 * @param p порт
 */
public SeparateClient(String hostname, int p) {
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
     * Основная функция работы клиента.
     *
     * @param sslContext Контекст для подключения.
     * @param fileName имя файла
     * @return код ошибки или 0 в случае успеха
     * @throws IOException ошибки ввода-вывода
     */
    public int get(SSLContext sslContext, String fileName, String testDir, boolean separateHandshake, boolean isTestClose) throws Exception {
        this.testDir = testDir;
        SSLSocket soc = null;
        try {
            SSLSocketFactory sslFact;
            if (sslContext == null) {
                sslContext = SSLContext.getInstance("Default", "JTLS");
                // sslFact = new SSLSocketFactoryImpl();
            }
            // else
            sslFact = sslContext.getSocketFactory();
            soc = (SSLSocket) sslFact.createSocket(host, port);
            soc.setSoTimeout(timeout);
            // SSLParameters params = soc.getSSLParameters();
            // params.setEndpointIdentificationAlgorithm("https");
            // soc.setSSLParameters(params);
            return proc(soc, fileName, separateHandshake, isTestClose);
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
 * @return код ошибки или 0 в случае успеха
 * @throws IOException ошибки ввода-вывода
 */
public int proc(SSLSocket soc, String file, boolean separateHandshake, boolean isTestClose) throws IOException, InterruptedException {

    Thread threadHandshake = null;
    if (separateHandshake){
        HandshakeThread handshakeThread = new HandshakeThread(soc);
        threadHandshake = new Thread(handshakeThread);
        threadHandshake.start();
    }
    ReaderThread readerThread = new ReaderThread(soc.getInputStream());
    Thread thread1 = new Thread(readerThread);
    thread1.start();
    Thread.sleep(200);

    final OutputStream out = soc.getOutputStream();
    // отправка запроса
    final String req = "GET /" + file + " HTTP/1.0\r\n\r\n";
    log.log(Level.INFO, " Client request: " + req);
    out.write(req.getBytes());
    out.flush();

    // преждевременное закрытие сокета
    if (isTestClose)
        soc.close();
    // ждем выполнения потоков
    if (threadHandshake !=null)
        threadHandshake.join();
    thread1.join();
    return 0;
}

}
