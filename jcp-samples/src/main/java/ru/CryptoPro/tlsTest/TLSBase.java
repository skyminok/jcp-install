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

import ComLine.ComLine;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.tools.JCPLogger;
import ru.CryptoPro.ssl.SSLLogger;


import java.util.logging.Logger;

/**
 * Общий класс. Параметры для запуска клиента/сервера.
 *
 */
public interface TLSBase {

    String CIPHER_SUITES = "-cipherSuites"; // cipher suites
    String SOCKET_TO = "-socketTO"; // socket timeout
    String SESSION_TO = "-sessionTO"; // server session timeout
    String CACHE_SIZE = "-cacheSize"; // server cache size
    String CACHE_LIMIT = "-cacheLimit"; // server cache limit
    String THREAD_COUNT = "-threadCount"; // client thread count
    String THREAD_TO = "-threadTO"; // client thread timeout
    String REQUEST_COUNT = "-requestCount"; // client request count
    String GEN_FILE = "-genFile"; // server command to generate a big file
    String GET_BIG_FILE = "-getBigFile"; // client command download a big file from the server
    String LOG_ENABLED = "-log"; // enable log on client
    String USE_SSPI = "-useSspi"; // use sspiSSL

    /**
     * Имя большого файла.
     */
    String BIG_FILE_NAME = "bigSrvFile.txt";

    /**
     * Имя хеша большого файла.
     */
    String DIGEST_FILE_NAME = "digestSrvFile.txt";

    /**
     * Help сервера.
     */
    String HELP_SERVER = "HELP\n" +
            "Server\n" +
            GEN_FILE + "                   generate file\n\n" +
            ComLine.protocol + "           protocol             (def: GostTLS)\n" +
            ComLine.PORT + "               port                 (def: 443)\n" +
            ComLine.auth + "               auth. of client      (def: false)\n" +
            ComLine.keyStoreType + "       keyStoreType         (def: \"HDImageStore\")\n" +
            ComLine.storeprovider + "      storeprovider        (def: JCP)\n" +
            ComLine.trustStoreType + "     trustStoreType       (def: \"CertStore\")\n" +
            ComLine.trustStorePath + "     trustStorePath       (def: no def)\n" +
            ComLine.trustStorePassword + " trustStorePassword   (def: no def)\n" +
            ComLine.keyStoreAlias + "      keyStoreAlias        (def: null)\n" +
            ComLine.keyStorePassword + "   keyStorePassword     (def: null)\n" +
            ComLine.servDir + "            serverWorkDir        (def: current)\n" +
            SOCKET_TO + "                  socketTO (in ms)     (def: 3000000)\n" +
            CIPHER_SUITES + "              cipherSuites         (def: default)\n" +
            SESSION_TO + "                 sessionTO (in sec)   (def: default)\n" +
            CACHE_SIZE + "                 cacheSize            (def: default)\n" +
            CACHE_LIMIT + "                cacheLimit           (def: default)\n" +
            ComLine.help + "               call help\n" +
            "\n parameters with (def: no def) must be defined necessarily\n";

    /**
     * Help клиента.
     */
    String HELP_CLIENT = "HELP\n" +
            "Client\n" +
            ComLine.protocol + "           protocol             (def: GostTLS)\n" +
            ComLine.PORT + "               port                 (def: 443)\n" +
            ComLine.SERVER + "             server name          (def: \"localhost\")\n" +
            ComLine.auth + "               auth. of client      (def: false)\n" +
            ComLine.keyStoreType + "       keyStoreType         (def: \"HDImageStore\")\n" +
            ComLine.storeprovider + "      storeprovider        (def: JCP)\n" +
            ComLine.trustStoreType + "     trustStoreType       (def: \"CertStore\")\n" +
            ComLine.trustStorePath + "     trustStorePath       (def: no def)\n" +
            ComLine.trustStorePassword + " trustStorePassword   (def: no def)\n" +
            ComLine.keyStoreAlias + "      keyStoreAlias        (def: null)\n" +
            ComLine.keyStorePassword + "   keyStorePassword     (def: null)\n" +
            ComLine.fileget + "            name of getting file (def: index.html)\n" +
            ComLine.fileout + "            path to output file  (def: no output)\n" +
            SOCKET_TO + "                  socketTO (in ms)     (def: 3000000)\n" +
            CIPHER_SUITES + "              cipherSuites         (def: default)\n" +
            THREAD_COUNT + "               threadCount          (def: 10)\n" +
            THREAD_TO + "                  threadTO (in ms)     (def: 3000000)\n\n" +
            REQUEST_COUNT + "              requestCount         (def: 10)\n" +
            GET_BIG_FILE  + "              get big file         (def: false)\n" +
            LOG_ENABLED   + "              enable console log   (def: false)\n" +
            ComLine.help + "               call help\n" +
            "\n parameters with (def: no def) must be defined necessarily\n";

    /**
     * Алгоритм хеширования файла.
     */
    String DIGEST_NAME = JCP.GOST_DIGEST_NAME;

    /**
     * Длина хеша в байтах.
     */
    int DIGEST_SIZE = 32;

    /**
     * Размер буфера для чтения данных.
     */
    int BUFFER_SIZE = 1024 * 1024;

    /**
     * Число блоков размером BUFFER_SIZE в
     * большом файле.
     */
    int BIG_FILE_FACTOR = 100;

    /**
     * Логгер JCP.
     */
    Logger JCP_LOG = Logger.getLogger(JCPLogger.LOGGER_NAME);

    /**
     * Логгер TLS.
     */
    Logger TLS_LOG = Logger.getLogger(SSLLogger.LOGGER_NAME);

}
