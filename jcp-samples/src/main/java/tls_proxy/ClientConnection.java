/**
 * $RCSfileClientConnection.java,v $
 * version $Revision: 36379 $
 * created 16.08.2016 10:09 by afevma
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

import tls_proxy.event.OnConnectionListener;

import javax.net.ssl.*;

import java.io.*;
import java.net.Socket;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Класс ClientConnection предназначен для
 * обслуживания клиентского подключения и
 * передачи данных между сокетами.
 *
 * @author Copyright 2004-2016 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class ClientConnection extends Thread {

    /**
     * Размер буфера данных по умолчанию.
     */
    private static final int BUFFER_SIZE = 16 * 1024; // 16 Kb

    /**
     * Таймаут обновления времени передачи данных.
     */
    private static final int TIMEOUT_UPDATE = 10 * 1000;

    /**
     * Сокет.
     */
    private final Socket socket;

    /**
     * Защищенный сокет.
     */
    private final SSLSocket secureSocket;

    /**
     * Входящий поток локального сокета.
     */
    private final InputStream localSocketInputStream;

    /**
     * Исходящий поток локального сокета.
     */
    private final OutputStream localSocketOutputStream;

    /**
     * Входящий поток защищенного сокета.
     */
    private final InputStream remoteSocketInputStream;

    /**
     * Исходящий поток защищенного сокета.
     */
    private final OutputStream remoteSocketOutputStream;

    /**
     * Последнее время активности соединения.
     * Используется для закрытия неактивных
     * соединений.
     */
    private final AtomicLong lastActivityTime = new AtomicLong(Calendar.getInstance().getTimeInMillis());

    /**
     * Слушатель событий.
     */
    private OnConnectionListener onConnectionListener;

    /**
     * Флаг завершения работы.
     */
    private final AtomicBoolean clientIsClosed = new AtomicBoolean(false);

    /**
     * Служебный класс для перенаправления данных.
     *
     */
    class StreamCallable implements Callable<Void> {

        /**
         * Входящий поток.
         */
        private final InputStream inputStream;

        /**
         * Исходящий поток.
         */
        private final OutputStream outputStream;

        /**
         * Конструктор.
         *
         * @param inputSocket Входящий сокет.
         * @param outputSocket Исходящий сокет.
         * @throws Exception
         */
        public StreamCallable(Socket inputSocket, Socket outputSocket) throws Exception {
            this.inputStream = inputSocket.getInputStream();
            this.outputStream = outputSocket.getOutputStream();
        }

        @Override
        public Void call() throws Exception {
            try {
                MainLogger.fine(TAG() + "stream started.");
                long inactiveTimeout = ConfigReader.getInstance().getInactiveTimeout();
                boolean firstRead = true;
                byte[] buffer = new byte[BUFFER_SIZE];
                while (!Thread.interrupted()) {
                    int readBufferSize;
                    while ((readBufferSize = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, readBufferSize);
                        if (inactiveTimeout > 0) {
                            long current = Calendar.getInstance().getTimeInMillis();
                            long diff = (current - lastActivityTime.get());
                            // Фиксируем время действия.
                            if (firstRead || diff > TIMEOUT_UPDATE) {
                                lastActivityTime.set(current);
                            }
                            firstRead = false;
                        }
                    }
                    try { Thread.sleep(10); } catch (Exception e) {}
                }
            } catch (Exception e) {
                MainLogger.error(TAG() + "stream failed.", e);
                Thread.currentThread().interrupt();
            } finally {
                close(inputStream);
                close(outputStream);
            }
            MainLogger.fine(TAG() + "stream finished.");
            return null;
        }
    }

    /**
     * Связь локальный сокет -> удаленный сокет.
     */
    private final StreamCallable local2remote;

    /**
     * Связь локальный удаленный сокет -> сокет.
     */
    private final StreamCallable remote2local;

    /**
     * Пул потоков.
     */
    private ExecutorService service;

    /**
     * Идентификатор клиента.
     */
    private final int clientId;

    /**
     * Конструктор.
     *
     * @param id Идентификатор клиента.
     * @param socket Сокет.
     * @param secureSocket Защищенный сокет.
     * @throws Exception
     */
    public ClientConnection(int id, Socket socket, SSLSocket secureSocket) throws Exception {
        super("client-" + id);
        this.clientId = id;
        this.socket = socket;
        this.secureSocket = secureSocket;
        this.secureSocket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
            @Override
            public void handshakeCompleted(HandshakeCompletedEvent handshakeCompletedEvent) {
                try {
                    SSLSocket ssl_socket = handshakeCompletedEvent.getSocket();
                    if (ssl_socket != null) {
                        SSLSession ssl_session = ssl_socket.getSession();
                        if (ssl_session != null) {
                            MainLogger.info(TAG() + "selected protocol is " + ssl_session.getProtocol());
                        } // if
                    } // if
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("TLS connection # ").append(clientId).append(" parameters:");
                    buffer.append("\n\t* selected cipher suite is ").append(handshakeCompletedEvent.getCipherSuite());
                    buffer.append("\n\t* selected local principal is ").append(handshakeCompletedEvent.getLocalPrincipal());
                    buffer.append("\n\t* peer principal is ").append(handshakeCompletedEvent.getPeerPrincipal());
                    MainLogger.info(TAG() + buffer.toString());
                } catch (SSLPeerUnverifiedException e) {
                    // ignore
                } catch (Exception e) {
                    // ignore
                }
            }
        });
        localSocketInputStream  = socket.getInputStream();
        localSocketOutputStream = socket.getOutputStream();
        remoteSocketInputStream  = secureSocket.getInputStream();
        remoteSocketOutputStream = secureSocket.getOutputStream();
        local2remote = new StreamCallable(socket, secureSocket);
        remote2local = new StreamCallable(secureSocket, socket);
    }

    /**
     * Задание слушателя событий для передачи событий управляющему потоку.
     *
     * @param listener Слушатель событий.
     */
    public void setOnConnectionListener(OnConnectionListener listener) {
        onConnectionListener = listener;
    }

    @Override
    public void run() {
        MainLogger.info(TAG() + "client executor started.");
        service = Executors.newFixedThreadPool(2);
        try {
            if (onConnectionListener != null) {
                onConnectionListener.onClientStart(clientId);
            } // if
            service.invokeAll(Arrays.asList(local2remote, remote2local));
        } catch (Exception e) {
            MainLogger.error(TAG() + "executor failed.", e);
        } finally {
            close();
        }
        MainLogger.info(TAG() + "client executor finished.");
    }

    /**
     * Проверка активности. Связь считается неактивной,
     * если один из сокетов закрыт или исчерпан таймаут.
     *
     * @return true, если соединение неактивно.
     */
    public boolean isInactive() {
        final long inactiveTimeout = ConfigReader.getInstance().getInactiveTimeout();
        long current = Calendar.getInstance().getTimeInMillis();
        long diff = current - lastActivityTime.get();
        boolean inactive = (diff >= inactiveTimeout);
        MainLogger.info(TAG() + "client is qualified as " + (inactive ? "inactive" : "active") + " after " + diff + " ms (max limit is " + inactiveTimeout + " ms).");
        return clientIsClosed.get() || inactive || socket.isClosed() || secureSocket.isClosed();
    }

    /**
     * Завершение работы клиентского потока.
     *
     */
    public synchronized void close() {
        if (clientIsClosed.get()) {
            MainLogger.fine(TAG() + "client already closed.");
            return;
        } // if
        // Завершение работы сокетов.
        MainLogger.fine(TAG() + "close local & secure streams.");
        close(localSocketInputStream);
        close(localSocketOutputStream);
        close(remoteSocketInputStream);
        close(remoteSocketOutputStream);
        close(socket);
        close(secureSocket);
        // Завершение работы service.
        MainLogger.fine(TAG() + "shutdown client executor.");
        service.shutdownNow();
        if (onConnectionListener != null) {
            onConnectionListener.onClientStop(clientId);
        } // if
        clientIsClosed.set(true);
    }

    /**
     * Закрытие source.
     *
     * @param source Закрываемый источник.
     */
    private static void close(Closeable source) {
        if (source != null) {
            try {
                source.close();
            } catch (Exception e) {
                // ignore
            }
        } // if
    }

    /**
     * Тэг лога.
     *
     * @return тэг.
     */
    private String TAG() {
        return "[" + getName() + "] :: ";
    }

}
