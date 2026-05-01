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

import ru.CryptoPro.JCP.JCP;
import util.ResolveProvider;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Класс SSL-конфигурации для TLS-примеров.
 */
public class SSLConfiguration {

    /**
     * Тип контейнера Java CSP по умолчанию.
     */
    // TODO: добавить проверку if windows then registry else hdimage.
    private static final String JCSP_DEFAULT_STORE_TYPE = ResolveProvider.ALTERNATIVE_HD_IMAGE;

    /**
     * Пароль для всех по умолчанию.
     */
    public static final char[] DEFAULT_PASSWORD = "1".toCharArray();

    /**
     * Относительный путь к корневому доверенному хранилищу локального Тестового УЦ.
     */
    public static final String LOCAL_CA_REL_PATH = "local_ca";

    /**
     * Путь к хранилищу доверенного корневого сертификата локального УЦ (test-ca).
     */
    public static final String LOCAl_TEST_CA_STORE = System.getProperty("user.dir") +
        File.separator + "data" + File.separator + "KEYS" + File.separator +
        LOCAL_CA_REL_PATH + File.separator + "truststore.store";

    /**
     * Переменная, сообщающая о задействованном Java CSP.
     */
    protected boolean JCSPEnabledForTls = ResolveProvider.JCSPEnabledForTls;
    /**
     * Тип доверенного хранилища.
     */
    protected String trustStoreType = null;

    /**
     * Путь к доверенному хранилищу.
     */
    protected String trustStore = null;

    /**
     * Пароль для доступа к доверенному хранилищу.
     */
    protected char[] trustStorePassword = null;

    /**
     * True, если требуется двусторонняя аутентификация.
     */
    protected boolean clientAuth = false;

    /**
     * Провайдер.
     */
    protected String keyStoreProvider = null;

    /**
     * Тип ключевого контейнера.
     */
    protected String keyStoreType = null;

    /**
     * Алиас ключа. Используется Java CSP + Java TLS.
     */
    protected String keyAlias = null;

    /**
     * Пароль для доступа к ключевому контейнеру.
     */
    protected char[] keyStorePassword = null;

    /**
     * True, если проверка другой стороны отключена.
     */
    protected boolean trustAll;

    /**
     * Менеджер сертификатов.
     */
    private TrustManager trustManager = null;

    /**
     * Менеджер сертификатов для случаев, когда проверка сертификата сервера отключена.
     *
     */
    public static final TrustManager TRUST_MANAGER_ALL = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}
        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

    /**
     * Набор cipher suites.
     */
    private String[] cipherSuites = null;

    /**
     * Способы указания application protocol клиентом или сервером.
     */
    public enum ApplicationProtocolStrategy { None, ByParameters, ByServerSelector, ByServerSelectorWithAbort };

    /**
     * Способ указания application protocol.
     */
    private ApplicationProtocolStrategy applicationProtocolStrategy = ApplicationProtocolStrategy.None;

    /**
     * Набор application protocol.
     */
    private String[] applicationProtocols = null;

    /**
     * Интерфейс для реализации проверки объекта типа T.
     *
     * @param <T> Тип проверяемого объекта.
     */
    public interface Checker<T> {
        void check(T t) throws IOException;
    }

    /**
     * Интерфейс для проверки сокета соединения.
     *
     */
    public interface ConnectionSSLSocketChecker extends Checker<SSLSocket> {}

    /**
     * Список проверок.
     */
    private Checker<?>[] checkers = null;

    /**
     * Интерфейс для реализации callback функции, создающей ManagerFactoryParameters для KeyManager.
     * Альтернативный вариант передачи параметров для KeyManagerFactory.
     *
     */
    public interface KeyManagerFactoryParametersCreator {
        ManagerFactoryParameters create(KeyStore keyStore, char[] password) throws Exception;
    }

    /**
     * Callback для создания ManagerFactoryParameters для KeyManager.
     */
    private KeyManagerFactoryParametersCreator keyManagerFactoryParametersCreatorCallback = null;

    /**
     * Интерфейс для реализации callback функции, создающей ManagerFactoryParameters для TrustManager.
     * Альтернативный вариант передачи параметров для TrustManagerFactory.
     *
     */
    public interface TrustManagerFactoryParametersCreator {
        ManagerFactoryParameters create(KeyStore trustStore) throws Exception;
    }

    /**
     * Callback для создания ManagerFactoryParameters для TrustManager.
     */
    private TrustManagerFactoryParametersCreator trustManagerFactoryParametersCreatorCallback = null;

    /**
     * Создание конфигурации по умолчанию для локального или внешнего УЦ.
     *
     * @param JCSPEnabled true, если задействован Java CSP.
     * @param clientAuth True, если используется клиентская аутентификация.
     * @throws Exception
     */
    public SSLConfiguration(boolean JCSPEnabled, boolean clientAuth) throws Exception {
        this(JCSPEnabled, JCP.CERT_STORE_NAME, LOCAl_TEST_CA_STORE, DEFAULT_PASSWORD, clientAuth, JCP.HD_STORE_NAME, null, DEFAULT_PASSWORD);
    }

    /**
     * Создание конфигурации по умолчанию для локального или внешнего УЦ.
     *
     * @param clientAuth True, если используется клиентская аутентификация.
     * @throws Exception
     */
    public SSLConfiguration(boolean clientAuth) throws Exception {
        this(ResolveProvider.JCSPEnabledForTls, clientAuth);
    }

    /**
     * Создание конфигурации согласно пользовательским настройкам.
     *
     * @param config Пользовательские настройки.
     * @throws Exception
     */
    public SSLConfiguration(ClientConfiguration config) throws Exception {
        this(ResolveProvider.JCSPEnabledForTls, config);
    }

    /**
     * Создание конфигурации согласно пользовательским настройкам.
     *
     * @param JCSPEnabled true, если задействован Java CSP.
     * @param config Пользовательские настройки.
     * @throws Exception
     */
    public SSLConfiguration(boolean JCSPEnabled, ClientConfiguration config) throws Exception {
        this(
            JCSPEnabled,
            config.getTrustStoreType(),
            config.getTrustStore(),
            config.getTrustStorePassword() != null
                ? config.getTrustStorePassword().toCharArray()
                : null,
                config.getUseClientAuth(),
            config.getKeyStoreType(),
            config.getKeyStoreAlias() != null
                ? config.getKeyStoreAlias()
                : null,
            config.getKeyStorePassword() != null
                ? config.getKeyStorePassword().toCharArray()
                : null
        );
    }

    /**
     * Создание конфигурации.
     *
     * @param JCSPEnabled true, если задействован Java CSP.
     * @param trustStoreType Тип доверенного хранилища.
     * @param trustStore Путь к доверенному хранилищу.
     * @param trustStorePassword Пароль для доступа к доверенному хранилищу.
     * @param clientAuth True, если используется клиентская аутентификация.
     * @param keyStoreType Тип ключевого контейнера.
     * @param keyStoreAlias Алиас ключа (для Java CSP). Может быть null.
     * @param keyStorePassword Пароль для доступа к ключевому контейнеру.
     * @throws Exception
     */
    public SSLConfiguration(boolean JCSPEnabled, String trustStoreType,
        String trustStore, char[] trustStorePassword, boolean clientAuth,
        String keyStoreType, String keyStoreAlias, char[] keyStorePassword)
        throws Exception {
        JCSPEnabledForTls = JCSPEnabled;
        this.keyStoreProvider = JCSPEnabledForTls ? ResolveProvider.ALTERNATIVE_PROVIDER : JCP.PROVIDER_NAME;
        this.keyStoreType = JCSPEnabledForTls
            ? ((keyStoreType == null) ? JCSP_DEFAULT_STORE_TYPE : keyStoreType)
            : ((keyStoreType == null) ? JCP.HD_STORE_NAME       : keyStoreType);
        this.keyAlias = keyStoreAlias;
        this.keyStorePassword = keyStorePassword;
        this.clientAuth = clientAuth;
        this.trustStoreType = trustStoreType == null ? JCP.CERT_STORE_NAME : trustStoreType;
        if (trustStore == null || trustStore.length() == 0) {
            throw new Exception("Trust store is null or empty.");
        } // if
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * Создание конфигурации.
     *
     * @param trustStoreType Тип доверенного хранилища.
     * @param trustStore Путь к доверенному хранилищу.
     * @param trustStorePassword Пароль для доступа к доверенному хранилищу.
     * @param clientAuth True, если используется клиентская аутентификация.
     * @param keyStoreType Тип ключевого контейнера.
     * @param keyStoreAlias Алиас ключа (для Java CSP). Может быть null.
     * @param keyStorePassword Пароль для доступа к ключевому контейнеру.
     * @throws Exception
     */
    public SSLConfiguration(String trustStoreType,
        String trustStore, char[] trustStorePassword, boolean clientAuth,
        String keyStoreType, String keyStoreAlias, char[] keyStorePassword,
        String keyStoreProvider) throws Exception {
        this.keyStoreProvider = keyStoreProvider;
        JCSPEnabledForTls = !keyStoreProvider.equalsIgnoreCase(JCP.PROVIDER_NAME);
        this.keyStoreType = JCSPEnabledForTls
            ? ((keyStoreType == null) ? JCSP_DEFAULT_STORE_TYPE : keyStoreType)
            : ((keyStoreType == null) ? JCP.HD_STORE_NAME       : keyStoreType);
        this.keyAlias = keyStoreAlias;
        this.keyStorePassword = keyStorePassword;
        this.clientAuth = clientAuth;
        this.trustStoreType = trustStoreType == null ? JCP.CERT_STORE_NAME : trustStoreType;
        if (trustStore == null || trustStore.length() == 0) {
            throw new Exception("Trust store is null or empty.");
        } // if
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
    }
    /**
     * Создание конфигурации.
     *
     * @param trustStoreType Тип доверенного хранилища.
     * @param trustStore Путь к доверенному хранилищу.
     * @param trustStorePassword Пароль для доступа к доверенному хранилищу.
     * @param clientAuth True, если используется клиентская аутентификация.
     * @param keyStoreType Тип ключевого контейнера.
     * @param keyStoreAlias Алиас ключа (для Java CSP). Может быть null.
     * @param keyStorePassword Пароль для доступа к ключевому контейнеру.
     * @throws Exception
     */
    public SSLConfiguration(String trustStoreType, String trustStore,
         char[] trustStorePassword, boolean clientAuth, String keyStoreType,
         String keyStoreAlias, char[] keyStorePassword) throws Exception {
        this(ResolveProvider.JCSPEnabledForTls, trustStoreType, trustStore,
            trustStorePassword, clientAuth, keyStoreType, keyStoreAlias,
                keyStorePassword);
    }

    /**
     * Если true, то доверие другой стороне без проверок сертификатов.
     *
     * @param trustAll True, если полное доверие другой стороне.
     */
    public void setTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
    }

    /**
     * Если true, то доверие другой стороне без проверок сертификатов.
     *
     * @return true, если полное доверие другой стороне.
     */
    public boolean isTrustAll() {
        return trustAll;
    }

    /**
     * Позволяет узнать, используется ли провайдер Java CSP.
     *
     * @return true, если используется.
     */
    public boolean isJCSPEnabledForTls() {
        return JCSPEnabledForTls;
    }

    /**
     * Позволяет узнать тип доверенного хранилища сертификатов.
     *
     * @return тип хранилища.
     */
    public String getTrustStoreType() {
        return trustStoreType;
    }

    /**
     * Позволяет узнать путь к доверенному хранилищу сертификатов.
     *
     * @return путь к хранилищу.
     */
    public String getTrustStore() {
        return trustStore;
    }

    /**
     * Позволяет узнать пароль к доверенному хранилищу сертификатов.
     *
     * @return пароль к хранилищу.
     */
    public char[] getTrustStorePassword() {
        return trustStorePassword;
    }

    /**
     * Позволяет узнать пароль к доверенному хранилищу сертификатов в виде строки.
     *
     * @return пароль к хранилищу.
     */
    public String getTrustStorePasswordString() {
        return trustStorePassword != null ? String.copyValueOf(trustStorePassword) : "";
    }

    /**
     * Позволяет узнать, требуется ли аутентификация клиента.
     *
     * @return True, если требуется.
     */
    public boolean needClientAuth() {
        return clientAuth;
    }

    /**
     * Позволяет узнать имя провайдера ключевого контейнера.
     *
     * @return тип имя провайдера.
     */
    public String getKeyStoreProvider() {
        return keyStoreProvider;
    }

    /**
     * Позволяет узнать тип ключевого контейнера.
     *
     * @return тип контейнера.
     */
    public String getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * Позволяет получить алиас ключа.
     *
     * @return алиас ключа или null.
     */
    public String getKeyAlias() {
        return keyAlias;
    }

    /**
     * Позволяет узнать пароль к ключевому контейнеру.
     *
     * @return пароль к контейнеру.
     */
    public char[] getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * Позволяет узнать пароль к ключевому контейнеру в виде строки.
     *
     * @return пароль к контейнеру.
     */
    public String getKeyStorePasswordString() {
        return keyStorePassword != null ? String.copyValueOf(keyStorePassword) : "";
    }

    /**
     * Задание менеджера сертификатов.
     *
     * @param trustManager Менеджер сертификатов.
     */
    public void setTrustManager(TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    /**
     * Получение менеджера сертификатов.
     *
     * @return менеджер сертификатов.
     */
    public TrustManager getTrustManager() {
        if (trustManager == null) {
            return TRUST_MANAGER_ALL;
        } // if
        return trustManager;
    }

    /**
     * Задание списка cipher suite.
     *
     * @param cipherSuites Список cipher suite.
     */
    public void setCipherSuites(String[] cipherSuites) {
        if (cipherSuites != null) {
            this.cipherSuites = new String[cipherSuites.length];
            System.arraycopy(cipherSuites, 0, this.cipherSuites, 0, cipherSuites.length);
        } // if
    }

    /**
     * Получение списка cipher suite.
     *
     * @return список cipher suite или null.
     */
    public String[] getCipherSuites() {
        return cipherSuites;
    }

    /**
     * Задание способа указания application protocol.
     *
     * @param applicationProtocolStrategy Способ указания application protocol.
     */
    public void setApplicationProtocolStrategy(ApplicationProtocolStrategy applicationProtocolStrategy) {
        this.applicationProtocolStrategy = applicationProtocolStrategy;
    }

    /**
     * Получение способа указания application protocol.
     *
     * @return способ указания application protocol.
     */
    public ApplicationProtocolStrategy getApplicationProtocolStrategy() {
        return applicationProtocolStrategy;
    }

    /**
     * Задание списка application protocol.
     *
     * @param applicationProtocols Список application protocol.
     */
    public void setApplicationProtocols(String[] applicationProtocols) {
        if (applicationProtocols != null) {
            this.applicationProtocols = new String[applicationProtocols.length];
            System.arraycopy(applicationProtocols, 0, this.applicationProtocols, 0, applicationProtocols.length);
        } // if
    }

    /**
     * Получение списка application protocol.
     *
     * @return список application protocol или null.
     */
    public String[] getApplicationProtocols() {
        return applicationProtocols;
    }

    /**
     * Задание списка проверок.
     *
     * @param checkers Список проверок.
     */
    public void setCheckers(Checker<?>[] checkers) {
        if (checkers != null) {
            this.checkers = new Checker[checkers.length];
            System.arraycopy(checkers, 0, this.checkers, 0, checkers.length);
        } // if
    }

    /**
     * Получение списка проверок.
     *
     * @return список проверок.
     */
    public Checker<?>[] getCheckers() {
        return checkers;
    }

    /**
     * Функция позволяет задать свойства для KeyManager с помощью callback для создания ManagerFactoryParameters.
     *
     * @param callback callback.
     */
    public void setKeyManagerFactoryParametersCreatorCallback(KeyManagerFactoryParametersCreator callback) {
        keyManagerFactoryParametersCreatorCallback = callback;
    }

    /**
     * Функция возвращает callback для создания ManagerFactoryParameters для KeyManager.
     *
     * @return callback.
     */
    public KeyManagerFactoryParametersCreator getKeyManagerFactoryParametersCreatorCallback() {
        return keyManagerFactoryParametersCreatorCallback;
    }

    /**
     * Функция позволяет задать свойства для TrustManager с помощью callback для создания ManagerFactoryParameters.
     *
     * @param callback callback.
     */
    public void setTrustManagerFactoryParametersCreatorCallback(TrustManagerFactoryParametersCreator callback) {
        trustManagerFactoryParametersCreatorCallback = callback;
    }

    /**
     * Функция возвращает callback для создания ManagerFactoryParameters для TrustManager.
     *
     * @return callback.
     */
    public TrustManagerFactoryParametersCreator getTrustManagerFactoryParametersCreatorCallback() {
        return trustManagerFactoryParametersCreatorCallback;
    }

    /**
     * Сравнение конфигураций.
     *
     * @param configuration Сравниваемая конфигурация.
     * @return True, если конфигурации идентичные.
     */
    public boolean equals(SSLConfiguration configuration) {
        boolean equalKeyAliases =
            (!(keyAlias != null && configuration.getKeyAlias() != null)) ||
            keyAlias.equalsIgnoreCase(configuration.getKeyAlias());
        return JCSPEnabledForTls == configuration.isJCSPEnabledForTls()
            && trustStoreType.equalsIgnoreCase(configuration.trustStoreType)
            && trustStore.equalsIgnoreCase(configuration.trustStore)
            && Arrays.equals(trustStorePassword, configuration.trustStorePassword)
            && keyStoreType.equalsIgnoreCase(configuration.keyStoreType)
            && equalKeyAliases
            && Arrays.equals(keyStorePassword, configuration.keyStorePassword)
            && (clientAuth || clientAuth == configuration.clientAuth);
    }

}
