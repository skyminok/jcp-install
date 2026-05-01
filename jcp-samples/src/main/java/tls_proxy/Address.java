/**
 * $RCSfileAddress.java,v $
 * version $Revision: 36379 $
 * created 15.08.2016 16:53 by afevma
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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.Arrays;

/**
 * Класс Address предназначен для декодирования
 * адреса хоста и его параметров в конфиге.
 *
 * @author Copyright 2004-2016 Crypto-Pro. All rights reserved.
 * @version 2.5
 * @see ConfigReader
 */
public class Address implements ConfigParameters {

    /**
     * Оформление ссылки.
     */
    private static final String URL_LINK = "<sup><small><font color='#cccccc'>%d</font></small></sup><a href='%s'>%s</a>";

    /**
     * Флаг состояния подключения к адресу.
     */
    private int connectionStatus;

    /**
     * Слушаемый порт {@link #ADDRESS_LISTEN_PORT}.
     */
    private final int listenPort;

    /**
     * Хост {@link #ADDRESS_HOST}.
     */
    private String host;

    /**
     * Порт {@link #ADDRESS_PORT}.
     */
    private int port;

    /**
     * Страница {@link #ADDRESS_PAGE}.
     */
    private String page;

    /**
     * Флаг клиентской аутентификации {@link #ADDRESS_CLIENT_AUTH_ENABLED}.
     */
    private boolean clientAuthEnabled;

    /**
     * Тип ключа {@link #ADDRESS_KEY_TYPE}.
     */
    private String keyType;

    /**
     * Тип ключа {@link #ADDRESS_KEY_ALIAS}.
     */
    private String keyAlias;

    /**
     * Пароль к ключу {@link #ADDRESS_KEY_PASSWORD}.
     */
    private String keyPassword;

    /**
     * Конструктор.
     *
     * @param connectionStatus Флаг состояния подключения к адресу.
     * @param listenPort Слушаемый порт.
     * @param host Хост.
     * @param port Порт.
     * @param page Страница.
     * @param clientAuthEnabled Флаг клиентской аутентификации.
     * @param keyType Тип ключа.
     * @param keyAlias Алиас ключа.
     * @param keyPassword Пароль к ключу.
     */
    public Address(int connectionStatus, int listenPort, String host,
        int port, String page, boolean clientAuthEnabled, String keyType,
        String keyAlias, String keyPassword) {
        this.connectionStatus = connectionStatus;
        this.listenPort = listenPort;
        this.host = host;
        this.port = port;
        this.page = page;
        this.clientAuthEnabled = clientAuthEnabled;
        this.keyType = keyType;
        this.keyAlias = keyAlias;
        this.keyPassword = keyPassword;
    }

    /**
     * Конструктор.
     *
     * @param connectionStatus Флаг состояния подключения к адресу.
     * @param listenPort Слушаемый порт.
     * @param url Адрес подключения.
     * @param clientAuthEnabled Флаг клиентской аутентификации.
     * @param keyType Тип ключа.
     * @param keyAlias Алиас ключа.
     * @param keyPassword Пароль к ключу.
     */
    public Address(int connectionStatus, int listenPort, URL url,
        boolean clientAuthEnabled, String keyType, String keyAlias,
        String keyPassword) {
        this(
            connectionStatus,
            listenPort,
            url.getHost(),
            (url.getPort() <= 0) ? url.getDefaultPort() : url.getPort(),
            url.getFile(),
            clientAuthEnabled,
            keyType,
            keyAlias,
            keyPassword
        );
    }

    /**
     * Копирование редактируемых полей.
     *
     * @param src Источник данных.
     * @param dst Обновляемый объект.
     */
    public static void copyEditableFields(Address src, Address dst) {
        dst.setHost(src.getHost());
        dst.setPort(src.getPort());
        dst.setPage(src.getPage());
        dst.setKeyType(src.getKeyType());
        dst.setKeyAlias(src.getKeyAlias());
        dst.setKeyPassword(src.getKeyPassword());
        dst.setClientAuthEnabled(src.isClientAuthEnabled());
    }

    /**
     * Управление флагом состояния подключения к адресу.
     *
     * @param status Статус.
     */
    public void setConnectionStatus(int status) {
        connectionStatus = status;
    }

    /**
     * Проверка статуса подключения к адресу.
     *
     * @return статус.
     */
    public int getConnectionStatus() {
        return connectionStatus;
    }

    /**
     * Получение слушаемого порта.
     *
     * @return слушаемый порт.
     */
    public int getListenPort() {
        return listenPort;
    }

    /**
     * Получение адреса подключения в исходном виде.
     *
     * @return адрес подключения.
     */
    public String getUrlString() {
        final String format = "https://%s:%d%s";
        return String.format(format, host, port, (page != null ? page : ""));
    }

    /**
     * Получение адреса подключения с оформлением.
     *
     * @return адрес подключения.
     */
    public String getDesignedUrlString() {
        String proxy = getProxyUrlString();
        String address = getUrlString();
        return String.format(URL_LINK, listenPort, proxy, address);
    }

    /**
     * Получение адреса подключения к прокси.
     *
     * @return адрес подключения к прокси.
     */
    public String getProxyUrlString() {
        final String format = "http://localhost:%d%s";
        return String.format(format, listenPort, (page != null ? page : ""));
    }

    /**
     * Задание хоста.
     *
     * @param host Хост.
     */
    public void setHost(String host) {
        this.host = host;
    }
    /**
     * Получение хоста.
     *
     * @return Хост.
     */
    public String getHost() {
        return host;
    }

    /**
     * Задание порта.
     *
     * @param port Порт.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Получение страницы.
     *
     * @return порт.
     */
    public String getPage() {
        return page;
    }

    /**
     * Задание страница.
     *
     * @param page Страница.
     */
    public void setPage(String page) {
        this.page = page;
    }

    /**
     * Получение порта.
     *
     * @return порт.
     */
    public int getPort() {
        return port;
    }

    /**
     * Задание флага клиентской аутентификации.
     *
     * @param clientAuthEnabled Флаг клиентской аутентификации.
     */
    public void setClientAuthEnabled(boolean clientAuthEnabled) {
        this.clientAuthEnabled = clientAuthEnabled;
    }

    /**
     * Проверка флага клиентской аутентификации.
     *
     * @return флаг клиентской аутентификации.
     */
    public boolean isClientAuthEnabled() {
        return clientAuthEnabled;
    }

    /**
     * Задание типа ключа.
     *
     * @param keyType Тип ключа.
     */
    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    /**
     * Получение типа ключа.
     *
     * @return тип ключа.
     */
    public String getKeyType() {
        return keyType;
    }

    /**
     * Задание алиаса ключа.
     *
     * @param keyAlias Алиас ключа.
     */
    public void setKeyAlias(String keyAlias) {
        this.keyAlias = keyAlias;
    }

    /**
     * Получение алиаса ключа.
     *
     * @return алиас ключа.
     */
    public String getKeyAlias() {
        return keyAlias;
    }

    /**
     * Задание пароля к ключу.
     *
     * @param keyPassword Пароль к ключу.
     */
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * Получение пароля к ключу.
     *
     * @return пароль к ключу.
     */
    public String getKeyPassword() {
        return keyPassword;
    }

    /**
     * Загрузка структуры адреса.
     *
     * @param nodeList Адрес с параметрами.
     * @return объект адреса или null.
     */
    public static Address load(NodeList nodeList) {
        int listenPort = 0;
        String host = "";
        int port = 0;
        String page = "";
        boolean clientAuthEnabled = false;
        String keyType = "";
        String keyAlias = "";
        String keyPassword = "";
        for (int j = 0; j < nodeList.getLength(); j++) {
            Node currentNode = nodeList.item(j);
            if (currentNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            } // if
            String currentNodeName = currentNode.getNodeName();
            String currentNodeContent = currentNode.getTextContent();
            if (currentNodeName.equalsIgnoreCase(ADDRESS_LISTEN_PORT)) {
                listenPort = Integer.parseInt(currentNodeContent);
            } // else
            else if (currentNodeName.equalsIgnoreCase(ADDRESS_HOST)) {
                host = currentNodeContent;
            } // else
            else if (currentNodeName.equalsIgnoreCase(ADDRESS_PORT)) {
                port = Integer.parseInt(currentNodeContent);
            } // else
            else if (currentNodeName.equalsIgnoreCase(ADDRESS_PAGE)) {
                page = currentNodeContent;
            } // else
            else if (currentNodeName.equalsIgnoreCase(ADDRESS_CLIENT_AUTH_ENABLED)) {
                clientAuthEnabled = Boolean.parseBoolean(currentNodeContent);
            } // else
            else if (currentNodeName.equalsIgnoreCase(ADDRESS_KEY_TYPE)) {
                keyType = currentNodeContent;
            } // else
            else if (currentNodeName.equalsIgnoreCase(ADDRESS_KEY_ALIAS)) {
                keyAlias = currentNodeContent;
            } // else
            else if (currentNodeName.equalsIgnoreCase(ADDRESS_KEY_PASSWORD)) {
                keyPassword = currentNodeContent;
            } // else
        } // for
        if (listenPort == 0 || host == null) {
            MainLogger.warning("Warning! Listen port or/and host undefined.");
            return null;
        } // if
        MainLogger.info("Imported address is:" +
            "\n\t " + ADDRESS_HOST + ": " + host +
            "\n\t " + ADDRESS_PORT + ": " + port +
            "\n\t " + ADDRESS_PAGE + ": " + page +
            "\n\t " + ADDRESS_CLIENT_AUTH_ENABLED + ": " + clientAuthEnabled +
            "\n\t " + ADDRESS_KEY_TYPE + ": " + keyType +
            "\n\t " + ADDRESS_KEY_ALIAS + ": " + keyAlias +
            "\n\t " + ADDRESS_KEY_PASSWORD + ": ***"
        );
        return new Address(
            TLSProxyConstants.CONNECTION_STOPPED,
            listenPort,
            host,
            port,
            page,
            clientAuthEnabled,
            keyType,
            keyAlias,
            keyPassword
        );
    }

    @Override
    public int hashCode() {
        int localPortHashCode = listenPort;
        int hostHashCode = host.hashCode();
        int portHashCode = port;
        int pageHashCode = page != null ? page.hashCode() : 0;
        int clientAuthHashCode = Boolean.valueOf(clientAuthEnabled).hashCode();
        int keyTypeHashCode     = keyType     != null ? keyType.hashCode()     : 0;
        int keyAliasHashCode    = keyAlias    != null ? keyAlias.hashCode()    : 0;
        int keyPasswordHashCode = keyPassword != null ? keyPassword.hashCode() : 0;
        return Arrays.hashCode(new Object[] {
            localPortHashCode,
            hostHashCode,
            portHashCode,
            pageHashCode,
            clientAuthHashCode,
            keyTypeHashCode,
            keyAliasHashCode,
            keyPasswordHashCode
        });
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        } // if
        if (!(object instanceof Address)) {
            return false;
        } // if
        Address address = (Address) object;
        if (listenPort != address.listenPort) {
            return false;
        } // if
        if (!host.equalsIgnoreCase(address.host)) {
            return false;
        } // if
        if (port != address.port) {
            return false;
        } // if
        if (!compareString(page, address.page)) {
            return false;
        } // if
        if (clientAuthEnabled != address.clientAuthEnabled) {
            return false;
        } // if
        if (!compareString(keyType, address.keyType)) {
            return false;
        } // if
        if (!compareString(keyAlias, address.keyAlias)) {
            return false;
        } // if
        if (!compareString(keyPassword, address.keyPassword)) {
            return false;
        } // if
        return true;
    }

    /**
     * Сравнение некоторых строковых параметров адресов.
     *
     * @param a Строка.
     * @param b Строка.
     * @return результат сравнения.
     */
    private static boolean compareString(String a, String b) {
        if (a != null && b != null) {
            if (a.equals(b)) {
                return true;
            } // if
        } // if
        else {
            if (a == null && b == null) {
                return true;
            } // if
            if (a == null && b.isEmpty()) {
                return true;
            } // if
            if (b == null && a.isEmpty()) {
                return true;
            } // if
        } // else
        return false;
    }

}
