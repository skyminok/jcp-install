/**
 * $RCSfileOnConnectionListener.java,v $
 * version $Revision: 36379 $
 * created 28.09.2017 17:28 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <br>
 * Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package tls_proxy.event;

import java.net.ServerSocket;

/**
 * Класс OnConnectionListener предназначен для
 * реализации слушателя событий при подключении.
 *
 * @author Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public interface OnConnectionListener {

    /**
     * Событие создания серверного сокета.
     *
     * @param listenPort Слушаемый сервером порт.
     * @param serverSocket Созданный серверный сокет.
     */
    void onServerSocketCreated(int listenPort, ServerSocket serverSocket);

    /**
     * Событие подключения клиента и запуска его потока.
     *
     * @param id Идентификатор клиента.
     */
    void onClientStart(int id);

    /**
     * Событие запуска серверного потока.
     *
     * @param listenPort Слушаемый сервером порт.
     * @param serverSocket Созданный серверный сокет.
     */
    void onServerStart(int listenPort, ServerSocket serverSocket);

    /**
     * Событие остановки клиентского потока.
     *
     * @param id Идентификатор клиента.
     */
    void onClientStop(int id);

    /**
     * Событие остановки серверного потока.
     *
     * @param listenPort Слушаемый сервером порт.
     */
    void onServerStop(int listenPort);

    /**
     * Событие ошибки клиентского потока.
     *
     * @param id Идентификатор клиента.
     */
    void onClientError(int id);

    /**
     * Событие ошибки серверного потока.
     *
     * @param listenPort Слушаемый сервером порт.
     */
    void onServerError(int listenPort);

    /**
     * Событие завершения работы клиентского потока.
     *
     * @param id Идентификатор клиента.
     */
    void onClientExit(int id);

    /**
     * Событие завершения работы серверного потока.
     *
     * @param listenPort Слушаемый сервером порт.
     */
    void onServerExit(int listenPort);

}
