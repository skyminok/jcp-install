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

import java.util.*;

/**
 * Класс для хранения наиболее общих свойств и параметров TLS-примеров.
 *
 * 30/04/2013
 *
 */
public abstract class AbstractTLSExample {

    public static final String DEFAULT_PROVIDER = "ru.CryptoPro.defaultSSLProv";

    /**
     * Максимальное время выполнения одного потока, sec.
     */
    public static final int THREAD_TIMEOUT = 10 * 60;

    /**
     * Список аргументов и их описание для вывода в справке.
     */
    protected final Map<String, String> argumentMap = new LinkedHashMap<String, String>();

    /**
     * Провайдер для хеширования и подписи.
     */
    public static final String defaultProvider =
        System.getProperty(DEFAULT_PROVIDER);

    /**
     * Ширина поля в справке в символах.
     */
    public static final int ARGUMENT_MAX_WIDTH = 20;

    public static final String PARAM_SERVER = "-server";
    public static final String PARAM_NAME = "-name";
    public static final String PARAM_LISTEN = "-listen";
    public static final String PARAM_SERVER_TIMEOUT = "-st";

    public static final String PARAM_CLIENT = "-client";
    public static final String PROTOCOL = "-protocol";
    public static final String PARAM_HOST = "-host";
    public static final String PARAM_PORT = "-port";
    public static final String PARAM_GET = "-get";
    public static final String PARAM_CLIENT_TIMEOUT = "-ct";
    public static final String PARAM_T = "-t";
    public static final String PARAM_N = "-n";
    public static final String PARAM_FILE_SOURCE = "-source";
    public static final String PARAM_FILE_STORE = "-store";
    public static final String PARAM_SAVE = "-save";
    public static final String PARAM_TRUST_ALL = "-trust_all";

    public static final String PARAM_AUTH = "-auth";
    public static final String PARAM_ALLOW = "-allow";

    public static final String PARAM_EXTERNAL = "-external";
    public static final String PARAM_APACHE_HTTP_CLIENT4 = "-apache4";
    public static final String PARAM_HTTP_1_1 = "-http_1_1";
    public static final String PARAM_TRACE = "-trace";

    /**
     * Конструктор класса. Необходим для корректного заполнения
     * списка с описанием аргументов примера.
     *
     */
    protected AbstractTLSExample() {

        // Наиболее общие настройки. Индивидуальные дописаны в самих
        // классах-потомках.

        argumentMap.put(PARAM_CLIENT, "");
        argumentMap.put(PARAM_HOST, "remote server to connect (def: \"127.0.0.1\")");
        argumentMap.put(PARAM_PORT, "remote server port to connect (def: 443)");
        argumentMap.put(PARAM_GET,  "remote server source to download (def: \"default.htm\")");

        putAdditionalClientParams();
        putAdditionalServerParams();

        argumentMap.put(ComLine.protocol,           "protocol (def: \"" + ComLine.GOST_TLS + "\")");
        argumentMap.put(ComLine.keyStoreType,       "type of key store (def: \"HDImageStore\")");
        argumentMap.put(ComLine.keyStoreAlias,      "alias of key (def: null)");
        argumentMap.put(ComLine.trustStoreType,     "type of trusted store for server and client (def: \"CertStore\")");
        argumentMap.put(ComLine.trustStorePath,     "pass to open trust store (def: no def)");
        argumentMap.put(ComLine.trustStorePassword, "password to open trusted store (def: null)");
        argumentMap.put(ComLine.keyStorePassword,   "password to open key store (def: null)");
        argumentMap.put(ComLine.help,               "call help");

    }

    /**
     * Дополнительная функция для добавления специфических
     * клиентских аргументов в общий список.
     */
    public abstract void putAdditionalClientParams();

    /**
     * Дополнительная функция для добавления специфических
     * серверных аргументов в общий список.
     */
    public abstract void putAdditionalServerParams();

    /**
     * Выравнивание аргументов и описания по некоторой ширине.
     *
     * @param argument Аргумент.
     * @param message Описание аргумента.
     * @param maxLeftWidth Максимальная ширина аргумента.
     * @param addTab True, если следует добавить знак табуляции перед
     * аргументом.
     * @return строку с выровненными аргументом и описанием.
     */
    public static String align(String argument, String message,
        int maxLeftWidth, boolean addTab) {

        int fillLen = maxLeftWidth - argument.length();
        String intend = "";

        if (fillLen > 0) {
            char[] buffer = new char[fillLen];
            Arrays.fill(buffer, ' ');
            intend = String.copyValueOf(buffer);
        } // if

        return (addTab ? "\t" : "") + argument + intend + message + "\n";
    }

    /**
     * Справка об аргументах в виде строки.
     *
     * @param map Список аргументов для вывода на экран.
     * @param excluded Список аргументов, к которым не надо применять
     * табуляцию при выравнивании.
     * @return справка.
     */
    public static String help(Map map, List excluded) {

        Set<Map.Entry<String, String>> entries = map.entrySet();
        Iterator<Map.Entry<String, String>> iterator = entries.iterator();

        String result = "HELP\n";

        while (iterator.hasNext()) {

            Map.Entry<String, String> entry = iterator.next();
            final String key = entry.getKey();
            final String value = entry.getValue();

            boolean subArgument = excluded != null && !excluded.contains(key);

            if (subArgument) {
                result += align(key, value, ARGUMENT_MAX_WIDTH, subArgument);
            } // if
            else {

                if (value != null) {
                    result += align(key, value, ARGUMENT_MAX_WIDTH, false);
                } // if
                else {
                    result += entry.getKey() + "\n";
                } // else

            } // else

        } // while

        result += "\n parameters with (def: no def) must be defined necessarily\n";

        return result;
    }

    /**
     * Вывод параметров примера, конфигурации.
     *
     * @param args Список параметров.
     * @param config Конфигурация.
     */
    public static void showSettings(Properties args, ClientConfiguration config) {

        System.out.println("---- Properties -----");

        if (args != null) {
            args.list(System.out);
        }

        System.out.println("---------------------\n");

        System.out.println("--- Configuration ---");

        if (config != null) {
            config.list(System.out);
        }

        System.out.println("---------------------\n");
    }

}
