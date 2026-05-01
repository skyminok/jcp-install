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

import ru.CryptoPro.JCP.KeyStore.StoreInputStream;

import ru.CryptoPro.ssl.Provider;

import javax.net.ssl.*;

import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * Класс SSL-коннектора для выработки SSL-контекста в
 * TLS примерах и тестах.
 */
public class SSLConnector {

    /**
     * Настройки подключения.
     */
    private SSLConfiguration sslConfiguration = null;

    /**
     * Хранилища.
     */
    private KeyStore keyStore = null, trustStore = null;

    /**
     * Фабрика для получения менеджера ключей.
     */
    private KeyManagerFactory kmf = null;

    /**
     * Фабрика для получения менеджера доверенных
     * сертификатов.
     */
    private TrustManagerFactory tmf = null;

    /**
     * Создание настроек для ssl-подключения с настройками по умолчанию.
     *
     * @param clientAuth True, если нужна аутентификация клиента.
     * @throws Exception
     */
    public SSLConnector(boolean clientAuth) throws Exception {
        sslConfiguration = new SSLConfiguration(clientAuth);
    }

    /**
     * Создание настроек для ssl-подключения с двухсторонней аутентификацией.
     *
     * @param configuration Настройки подключения.
     */
    public SSLConnector(SSLConfiguration configuration) {
        sslConfiguration = configuration;
    }

    /**
     * Выполнение загрузки контейнеров, открытие
     * хранилища доверенных сертификатов.
     *
     * @param isServer True, если создается для сервера.
     * @throws Exception
     */
    public void prepare(boolean isServer) throws Exception {
        prepare(isServer, false);
    }

    /**
     * Выполнение загрузки контейнеров, открытие
     * хранилища доверенных сертификатов.
     *
     * @param isServer True, если создается для сервера.
     * @param noAlias True, если требуется создать без алиаса.
     * @throws Exception
     */
    public void prepare(boolean isServer, boolean noAlias) throws Exception {

        if (isServer || sslConfiguration.needClientAuth()) {

            kmf = KeyManagerFactory.getInstance(Provider.KEYMANGER_ALG, "JTLS");
            keyStore = KeyStore.getInstance(sslConfiguration.getKeyStoreType(), sslConfiguration.getKeyStoreProvider());

            if (sslConfiguration.getKeyAlias() != null && !noAlias) {
                keyStore.load(new StoreInputStream(sslConfiguration.getKeyAlias()), null);
            } // if
            else {
                keyStore.load(null, null);
            } // else

            if (sslConfiguration.getKeyManagerFactoryParametersCreatorCallback() == null) {
                kmf.init(keyStore, sslConfiguration.getKeyStorePassword());
            } // if
            else {
                ManagerFactoryParameters managerFactoryParameters = sslConfiguration.getKeyManagerFactoryParametersCreatorCallback().create(keyStore, sslConfiguration.getKeyStorePassword());
                kmf.init(managerFactoryParameters);
            } // else

        } // if

        trustStore = KeyStore.getInstance(sslConfiguration.getTrustStoreType());
        try (FileInputStream is = new FileInputStream(sslConfiguration.getTrustStore())) {
            trustStore.load(is, sslConfiguration.getTrustStorePassword());
        }

        tmf = TrustManagerFactory.getInstance(Provider.TRUSTMANGER_ALG, "JTLS");

        if (sslConfiguration.getTrustManagerFactoryParametersCreatorCallback() == null) {
            tmf.init(trustStore);
        } // if
        else {
            ManagerFactoryParameters managerFactoryParameters = sslConfiguration.getTrustManagerFactoryParametersCreatorCallback().create(trustStore);
            tmf.init(managerFactoryParameters);
        } // else

    }

    /**
     * Выполнение загрузки контейнеров, открытие
     * хранилища доверенных сертификатов без указания алиаса.
     *
     * @param isServer True, если создается для сервера.
     * @throws Exception
     */
    public void prepareNoAlias(boolean isServer) throws Exception {
        prepare(isServer, true);
    }

    /*
    * Создание SSL контекста.
    *
    * @param protocol Протокол.
    * @return готовый SSL контекст.
    * @throws Exception.
    */
    public SSLContext create(String protocol) throws Exception {

        SSLContext sslCtx = SSLContext.getInstance(protocol, "JTLS");
        if (sslConfiguration.isTrustAll()) {
            sslCtx.init(
                kmf != null ? kmf.getKeyManagers() : null,
                // new TrustManager[] {sslConfiguration.getTrustManager()}, // JCP-1904
                new TrustManager[] {SSLConfiguration.TRUST_MANAGER_ALL},
                null
            );
        } // if
        else {
            sslCtx.init(kmf != null ? kmf.getKeyManagers() : null, tmf.getTrustManagers(), null);
        } // else

        return sslCtx;
        
    }

    /**
     * Получение хранилища контейнеров.
     *
     * @return хранилище контейнеров.
     */
    public KeyStore getKeyStore() {
        return keyStore;
    }

    /**
     * Получение хранилища сертификатов.
     *
     * @return хранилище сертификатов.
     */
    public KeyStore getTrustStore() {
        return trustStore;
    }

    /**
     * Получение конфигурации.
     *
     * @return конфигурация.
     */
    public SSLConfiguration getSslConfiguration() {
        return sslConfiguration;
    }

}
