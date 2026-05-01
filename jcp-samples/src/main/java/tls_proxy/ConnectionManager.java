/**
 * $RCSfileConnectionManager.java,v $
 * version $Revision: 36379 $
 * created 15.08.2016 16:06 by afevma
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

import java.net.ServerSocket;
import java.net.Socket;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Класс ConnectionManager предназначен для управления подключением клиентов.
 *
 * @author Copyright 2004-2016 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class ConnectionManager extends Thread {

    /**
     * Слушаемый сокет.
     */
    private ServerSocket serverSocket;

    /**
     * Список соединений по адресу.
     */
    private final Vector<ClientConnection> clientConnections = new Vector<ClientConnection>();

    /**
     * Слушатель событий для передачи событий в вызывающий код.
     */
    private OnConnectionListener onConnectionListener;

    /**
     * Слушаемый порт.
     */
    private final int listenPort;

    /**
     * Проверка неработающих подключений.
     */
    private final Timer checkTimer;

    /**
     * Номер соединения.
     */
    private final AtomicInteger nextId = new AtomicInteger(1);

    /**
     * Конструктор.
     *
     * @param listenPort Слушаемый порт.
     * Может быть null.
     */
    public ConnectionManager(int listenPort) {
        super("server-" + listenPort);
        this.listenPort = listenPort;
        checkTimer = new Timer();
        checkTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkAndClose(false);
                }
            },
            ConfigReader.getInstance().getCheckInactiveTimeout(),
            ConfigReader.getInstance().getCheckInactiveTimeout()
        );
    }

    /**
     * Задание слушателя событий для передачи событий вызывающему коду.
     *
     * @param listener Слушатель событий.
     */
    public void setConnectionListener(OnConnectionListener listener) {
        onConnectionListener = listener;
    }

    @Override
    public void run() {

        try {

            serverSocket = new ServerSocket(listenPort);
            serverSocket.setSoTimeout(ConfigReader.getInstance().getServerSoTimeout());

            if (onConnectionListener != null) {
                MainLogger.fine(TAG() + " onServerSocketCreated().");
                onConnectionListener.onServerSocketCreated(listenPort, serverSocket);
            } // if

            boolean firstStart = true;

            while (!Thread.interrupted()) {
                if (firstStart) {
                    if (onConnectionListener != null) {
                        MainLogger.fine(TAG() + " onServerStart().");
                        onConnectionListener.onServerStart(listenPort, serverSocket);
                    } // if
                    firstStart = false;
                } // if

                MainLogger.info(TAG() + "waiting for client connection...");
                Socket clientSocket = serverSocket.accept();

                MainLogger.fine(TAG() + "client connection accepted: " + clientSocket);
                Address address = ConfigReader.getInstance().findAddress(listenPort);

                SecureConnectionManager secureConnectionManager = new SecureConnectionManager(address);
                MainLogger.info(TAG() + "starting client connection to " + address.getHost() + "...");

                ClientConnection clientConnection = new ClientConnection(nextId.getAndIncrement(),
                    clientSocket, secureConnectionManager.getSSLSocket());

                clientConnection.setOnConnectionListener(onConnectionListener);
                clientConnection.start();

                synchronized (clientConnections) {
                    clientConnections.add(clientConnection);
                    MainLogger.fine(TAG() + "client connection count: " + clientConnections.size());
                } // synchronized

            } // while

        } catch (Exception e) {
            MainLogger.error(TAG() + "server stream failed.", e);
        } finally {
            closeInternal();
        }

    }

    /**
     * Проверка соединений и удаление неактивных.
     *
     * @param forceClose True, если нужно закрыть все клиентские соединения.
     */
    private void checkAndClose(boolean forceClose) {
        synchronized (clientConnections) {
            MainLogger.fine(TAG() + "check client connections...");
            Iterator<ClientConnection> connectionIterator = clientConnections.iterator();
            while (connectionIterator.hasNext()) {
                ClientConnection connection = connectionIterator.next();
                if (forceClose || (!connection.isAlive() || connection.isInactive())) {
                    MainLogger.fine(TAG() + "close, stop & remove inactive client connection (force: " + forceClose + ").");
                    connection.close();
                    connectionIterator.remove();
                } // if
            } // while
            MainLogger.fine(TAG() + "check client connections completed. Client connection count is " + clientConnections.size());
        } // synchronized
    }

    /**
     * Завершение работы серверного потока.
     *
     */
    private synchronized void closeInternal() {
        // Остановка таймера.
        MainLogger.fine(TAG() + "cancel check timer.");
        checkTimer.cancel();
        // Завершение работы клиентских подключений и сервера.
        close();
        checkAndClose(true);
        if (onConnectionListener != null) {
            MainLogger.fine(TAG() + " onServerStop().");
            onConnectionListener.onServerStop(listenPort);
        } // if
    }

    /**
     * Завершение работы серверного потока.
     *
     */
    public void close() {
        // Вызываем завершение работы run().
        try {
            MainLogger.fine(TAG() + "close server socket.");
            serverSocket.close();
        } catch (Exception e) {
            // ignore
        }
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
