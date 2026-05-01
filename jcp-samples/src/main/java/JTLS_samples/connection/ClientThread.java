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

import ComLine.ComLine;

import JTLS_samples.ApacheHttpClient4XExample;
import JTLS_samples.Client;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * Класс потока клиента для получения файла с локального или удаленного сервера.
 *
 * 30/04/2013
 *
 */
public class ClientThread extends Thread {

    /**
     * Настройки клиента.
     */
    private ClientConfiguration clientConfig = null;
    /**
     * Имя потока.
     */
    private String clientName = null;
    /**
     * Время выполнения всех операций в потоке, мсек.
     */
    private long executionTime = 0;
    /**
     * Произошла ошибка.
     */
    private boolean failure = false;
    /**
     * Флаг сохранения результатов в файл.
     */
    private boolean needTrace = false;
    /**
     * Протокол.
     */
    private String protocol = ComLine.GOST_TLS;

    /**
     * Конструктор.
     *
     * @param name Имя потока.
     * @param config Настройки клиента.
     * @param trace True, если следует сохранять результаты в файл.
     * @param protocol SSL протокол.
     */
    public ClientThread(String name, ClientConfiguration config, boolean trace, String protocol) {
        this.clientName = name;
        this.clientConfig = config;
        this.needTrace = trace;
        this.protocol = protocol;
    }

    /**
     * Функция получения файла.
     */
    public void run() {

        // Замеряем время.
        long startTime = getCurrentTime();

        try {

            SSLConfiguration sslConfig = clientConfig.isFull()
                ? new SSLConfiguration(clientConfig)
                : new SSLConfiguration(clientConfig.getUseClientAuth());

            sslConfig.setTrustAll(clientConfig.isTrustAll());
            sslConfig.setTrustManager(clientConfig.getTrustManager());

            SSLConnector connector = new SSLConnector(sslConfig);
            connector.prepare(false);

            SSLContext sslContext = connector.create(protocol);
            String outFileNameFormat = (clientConfig.getFileStore() != null ? clientConfig.getFileStore() : "") + "_" + clientName + "__Request_#%s__";

            if (!clientConfig.getUseApache()) { // Стандартным способом...

                // Создаем самописного клиента.

                Client client = new Client(clientConfig.getHost(), clientConfig.getPort());
                client.setTimeout(clientConfig.getThreadTimeout());
                client.setHttp1_1(clientConfig.getIsHttp1_1());

                // Получаем файл нужное число раз.

                for (int i = 0; i < clientConfig.getLoadingCount(); i++) {

                    String outFile = needTrace ? String.format(outFileNameFormat, i) : null;
                    String testDir = (!clientConfig.getExternalWebServer() && clientConfig.isLocal()) ? clientConfig.getFileSource() : null;

                    if (client.get(sslContext, clientConfig.getDownloadingFile(), outFile, testDir) != 0) {
                        throw new IOException("Couldn't get data.");
                    } // if

                } // for
            } // if
            else {

                // Или используя apache http client 4.x

                ApacheHttpClient4XExample apacheClient = new ApacheHttpClient4XExample(
                    clientConfig.getDownloadingUrl(),
                    clientConfig.getPort(),
                    sslContext,
                    clientConfig.getAllowAllHostnameVerifier()
                );

                apacheClient.setReadWriteTimeout(clientConfig.getThreadTimeout());
                apacheClient.setConnectionTimeout(clientConfig.getThreadTimeout());

                // Получаем файл нужное число раз без сохранения.

                int count = clientConfig.getLoadingCount();
                int shutDownCount = count - 1;

                for (int i = 0; i < count; i++) {

                    File downloadingSource = new File(clientConfig.getDownloadingFile());
                    String outFile = String.format(outFileNameFormat, i) + downloadingSource.getName();
                    OutputStream outFileStream = needTrace ? new FileOutputStream(outFile) : null;

                    try {
                        apacheClient.execute(outFileStream, (i >= shutDownCount));
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        if (outFileStream != null) {
                            outFileStream.close();
                        } // if
                    }

                } // for
            } // else

        } catch (Exception e) {

            final RuntimeException ex = new RuntimeException("Transfer of '" + clientConfig.getDownloadingFile() + "' failed: " + e.getMessage());
            failure = true;

            ex.initCause(e);
            throw ex;

        } finally {
            executionTime = getCurrentTime() - startTime;
        }

    }

    /**
     * Получение имени потока.
     *
     * @return имя потока.
     */
    public String getThreadName() {
        return clientName;
    }

    /**
     * Получение текущего времени в мсек.
     *
     * @return время в мсек.
     */
    private long getCurrentTime() {
        return Calendar.getInstance().getTime().getTime();
    }

    /**
     * Получение времени выполнения задания в мсек.
     *
     * @return время в мсек.
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Случилась ли ошибка в ходе выполнения задачи.
     *
     * @return True, если произошла ошибка в ходе выполнения.
     */
    public boolean failed() {
        return failure;
    }
}
