package tls_proxy;

public interface ConfigParameters {

    /**
     * Файл с конфигурацией.
     */
    String CONFIG_NAME = "config.xml";

    /**
     * Главный блок.
     */
    String CONFIG = "Config";

    /**
     * Блок адресов.
     */
    String ADDRESSES = "Addresses";

    /**
     * Блок адреса.
     */
    String ADDRESS = "Address";

    /**
     * Блок ListenPort.
     */
    String ADDRESS_LISTEN_PORT = "ListenPort";

    /**
     * Блок Host.
     */
    String ADDRESS_HOST = "Host";

    /**
     * Блок Port.
     */
    String ADDRESS_PORT = "Port";

    /**
     * Блок Page.
     */
    String ADDRESS_PAGE = "Page";

    /**
     * Блок ClientAuthEnabled.
     */
    String ADDRESS_CLIENT_AUTH_ENABLED = "ClientAuthEnabled";

    /**
     * Блок KeyType.
     */
    String ADDRESS_KEY_TYPE = "KeyType";

    /**
     * Блок KeyAlias.
     */
    String ADDRESS_KEY_ALIAS = "KeyAlias";

    /**
     * Блок KeyPassword.
     */
    String ADDRESS_KEY_PASSWORD = "KeyPassword";

    /**
     * Блок CertStore.
     */
    String CERT_STORE = "CertStore";

    /**
     * Имя провайдера для работы с доверенным хранилищем корневых сертификатов.
     */
    String CERT_STORE_PROVIDER = "provider";

    /**
     * Тип хранилища доверенных корневых сертификатов.
     */
    String CERT_STORE_TYPE = "type";

    /**
     * Путь к хранилищу доверенных корневых сертификатов.
     */
    String CERT_STORE_PATH = "path";

    /**
     * Пароль к хранилищу доверенных корневых сертификатов.
     */
    String CERT_STORE_PASSWORD = "password";

    /**
     * Блок Parameters.
     */
    String PARAMETERS = "Parameters";

    /**
     * Период неактивного соединения, после которого его следует закрыть.
     */
    String PARAMETERS_INACTIVITY_TIMEOUT = "inactiveTimeout";

    /**
     * Таймаут проверки неактивных соединений.
     */
    String PARAMETERS_CHECK_INACTIVITY_TIMEOUT = "checkInactiveTimeout";

    /**
     * Период ожидания подключения.
     */
    String PARAMETERS_SERVER_SO_TIMEOUT = "serverSoTimeout";

    /**
     * Провайдер для работы с ключами.
     */
    String PARAMETERS_KEY_STORE_PROVIDER = "provider";

    /**
     * TLS протокол.
     */
    String PARAMETERS_TLS_PROTOCOL = "protocol";

    /**
     * TLS cipher suites.
     */
    String PARAMETERS_TLS_CIPHERS = "ciphers";

    /**
     * Разделитель для разбора cipher suites.
     */
    String TLS_CIPHER_SEPARATOR = ";";

}
