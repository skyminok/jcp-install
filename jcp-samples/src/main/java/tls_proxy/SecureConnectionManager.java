/**
 * $RCSfileSecureConnectionManager.java,v $
 * version $Revision: 36379 $
 * created 16.08.2016 10:43 by afevma
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

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.StoreInputStream;

import ru.CryptoPro.ssl.Provider;
import javax.net.ssl.*;

import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * Класс SecureConnectionManager предназначен
 * для создания защищенного сокета по адресу
 * и другим параметрами.
 *
 * @author Copyright 2004-2016 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class SecureConnectionManager implements ConfigParameters {

    /**
     * Адрес подключения.
     */
    private final Address address;

    /**
     * Фабрика защищенных сокетов.
     */
    private final SSLSocketFactory sslSocketFactory;

    /**
     * Конструктор.
     * Выполняет создание защищенного контекста и инициализацию фабрики сокетов.
     *
     * @param address Адрес подключения.
     */
    public SecureConnectionManager(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address is null.");
        } // if
        this.address = address;
        try {
            this.sslSocketFactory = createSSLContext().getSocketFactory();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Создание защищенного контекста.
     *
     * @return защищенный контекст.
     * @throws Exception
     */
    private SSLContext createSSLContext() throws Exception {

        KeyManagerFactory kmf = null;
        ConfigReader configReader = ConfigReader.getInstance();

        String certStoreProvider = configReader.getCertStoreProvider();
        String certStoreType = configReader.getCertStoreType();
        String certStorePath = configReader.getCertStorePath();
        String certStorePassword = configReader.getCertStorePassword();

        String provider = configReader.getProvider();
        String protocol = configReader.getProtocol();

        if (certStoreType == null || certStoreType.isEmpty()) {
            certStoreType = JCP.CERT_STORE_NAME;
        } // if

        MainLogger.info("Trust store parameters for " + address.getHost() + ":"
            + "\n\t" + CERT_STORE_PROVIDER + ": " + certStoreProvider
            + "\n\t" + CERT_STORE_TYPE + ": " + certStoreType
            + "\n\t" + CERT_STORE_PATH + ": " + certStorePath
            + "\n\t" + ADDRESS_CLIENT_AUTH_ENABLED + ": " + address.isClientAuthEnabled()
            + "\n\t...");

        KeyStore trustStore;

        if (certStoreProvider != null && !certStoreProvider.isEmpty()) {
            trustStore = KeyStore.getInstance(certStoreType, certStoreProvider);
        } // if
        else {
            trustStore = KeyStore.getInstance(certStoreType);
        } // else

        try (FileInputStream is = new FileInputStream(certStorePath)) {
            trustStore.load(is, certStorePassword.toCharArray());
        }

        if (address.isClientAuthEnabled()) {

            String keyType = address.getKeyType();
            String keyAlias = address.getKeyAlias();
            String keyPassword = address.getKeyPassword();

            MainLogger.info("Key store parameters for " + address.getHost() + ":"
                + "\n\t" + PARAMETERS_KEY_STORE_PROVIDER + ": " + provider
                + "\n\t" + ADDRESS_KEY_TYPE + ": " + keyType
                + "\n\t" + ADDRESS_KEY_ALIAS + ": " + keyAlias
                + "\n\t...");

            KeyStore keyStore;

            if (provider != null && !provider.isEmpty()) {
                keyStore = KeyStore.getInstance(keyType, provider);
            } // if
            else {
                keyStore = KeyStore.getInstance(keyType);
            } // else

            if (keyAlias != null && !keyAlias.isEmpty()) {
                keyStore.load(new StoreInputStream(keyAlias), null);
            } // if
            else {
                keyStore.load(null, null);
            } // else

            kmf = KeyManagerFactory.getInstance(Provider.KEYMANGER_ALG, Provider.PROVIDER_NAME);
            kmf.init(keyStore, keyPassword.toCharArray());

        } // if

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(Provider.TRUSTMANGER_ALG, Provider.PROVIDER_NAME);
        tmf.init(trustStore);

        MainLogger.info("Creating secure context with TLS protocol '" + protocol + "'...");

        SSLContext sslCtx = SSLContext.getInstance(protocol, Provider.PROVIDER_NAME);
        sslCtx.init(kmf != null ? kmf.getKeyManagers() : null, tmf.getTrustManagers(), null);

        MainLogger.info("Secure context created.");
        return sslCtx;

    }

    /**
     * Создание SSL сокета.
     *
     * @return SSL сокет.
     */
    public SSLSocket getSSLSocket() throws Exception {
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(address.getHost(), address.getPort());
        String[] ciphers = ConfigReader.getInstance().getCiphers();
        if (ciphers != null) {
            sslSocket.setEnabledCipherSuites(ciphers);
        } // if
        return sslSocket;
    }

}
