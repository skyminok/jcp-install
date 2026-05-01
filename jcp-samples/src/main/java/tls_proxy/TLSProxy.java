/**
 * $RCSfileTLSProxy.java,v $
 * version $Revision: 36379 $
 * created 15.08.2016 17:13 by afevma
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

import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.util.Arrays;

/**
 * Класс TLSProxy предназначен для запуска прокси.
 *
 * @author Copyright 2004-2016 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class TLSProxy implements ConfigParameters {

    /**
     * Пример конфигурации.
     */
    private static final String PROXY_CONFIG_EXAMPLE =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    + "\n<" + CONFIG + ">"
    + "\n<!-- Parameter 'provider' is a key store provider. -->"
    + "\n<" + PARAMETERS + " " + PARAMETERS_INACTIVITY_TIMEOUT + "=\"60\" " + PARAMETERS_CHECK_INACTIVITY_TIMEOUT + "=\"30\" " + PARAMETERS_SERVER_SO_TIMEOUT + "=\"600\" " + PARAMETERS_KEY_STORE_PROVIDER + "=\"JCP\" " + PARAMETERS_TLS_PROTOCOL + "=\"GostTLSv1.2\" " + PARAMETERS_TLS_CIPHERS + "=\"TLS_CIPHER_2012" + TLS_CIPHER_SEPARATOR + "\"/>"
    + "\n<!-- Parameter 'path' is a path to the trust store with type 'CertStore' and password 'password'. There can be absolute path to the store file or file name. If file name set "
    + "path to the config.xml is used. A parameter 'provider' is a trust store provider.-->"
    + "\n<" + CERT_STORE + " " + CERT_STORE_PATH + "=\"c:\\software\\Keys\\tomcat7\\test_ca.store\" " + CERT_STORE_PASSWORD + "=\"1\" " + CERT_STORE_TYPE + "=\"CertStore\" " + CERT_STORE_PROVIDER + "=\"JCP\" />"
    + "\n<!-- List of the hosts -->"
    + "\n<" + ADDRESSES + ">"
    + "\n<" + ADDRESS + ">"
    + "\n<!-- Listening local port. -->"
    + "\n<" + ADDRESS_LISTEN_PORT + ">9000</" + ADDRESS_LISTEN_PORT + ">"
    + "\n<!-- Host parameters: host and port. -->"
    + "\n<" + ADDRESS_HOST + ">cpca.cryptopro.ru</" + ADDRESS_HOST + ">"
    + "\n<" + ADDRESS_PORT + ">443</" + ADDRESS_PORT + ">"
    + "\n<!-- True if client authentication is required. -->"
    + "\n<" + ADDRESS_CLIENT_AUTH_ENABLED + ">false</" + ADDRESS_CLIENT_AUTH_ENABLED + ">"
    + "\n</" + ADDRESS + ">"
    + "\n<" + ADDRESS + ">"
    + "\n<" + ADDRESS_LISTEN_PORT + ">9001</" + ADDRESS_LISTEN_PORT + ">"
    + "\n<" + ADDRESS_HOST + ">cryptopro.ru</" + ADDRESS_HOST + ">"
    + "\n<" + ADDRESS_PORT + ">4444</" + ADDRESS_PORT + ">"
    + "\n<!-- If client authentication is required then KeyType and KeyPassword must be set, and keyAlias is also available. -->"
    + "\n<" + ADDRESS_CLIENT_AUTH_ENABLED + ">true</" + ADDRESS_CLIENT_AUTH_ENABLED + ">"
    + "\n<" + ADDRESS_KEY_TYPE + ">HDImageStore</" + ADDRESS_KEY_TYPE + ">"
    + "\n<" + ADDRESS_KEY_ALIAS + ">client_tls</" + ADDRESS_KEY_ALIAS + ">"
    + "\n<" + ADDRESS_KEY_PASSWORD + ">Pass1234</" + ADDRESS_KEY_PASSWORD + ">"
    + "\n</" + ADDRESS + ">"
    + "\n</" + ADDRESSES + ">"
    + "\n</" + CONFIG + ">";

    /**
     * Запуск приложения.
     *
     * @param args Параметры приложения.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        if (args.length == 0) {
            System.out.println(
                  "Usage:"
                + "\ntls_proxy -help to get an example of configuration"
                + "\ntls_proxy <listen-address-port> <path to config.xml> to start application"
                + "\ntls_proxy <listen-address-port> to start application"
                + "\nExamples:"
                + "\njava -jar tls_proxy.jar 9000"
                + "\njava -jar tls_proxy.jar 9001 C:/config.xml"
            );
            return;
        } // if

        if (Arrays.asList(args).contains("-help")) {
            System.out.println("Example of configuration file 'config.xml':\n");
            System.out.println(PROXY_CONFIG_EXAMPLE);
            return;
        } // if

        int listenPort = Integer.parseInt(args[0]);
        MainLogger.info("*** Listen address port: " + listenPort);

        String configFile = null;
        if (args.length > 1) {
            configFile = args[1];
        } // if

        ConfigReader.init(configFile);
        String provider = ConfigReader.getInstance().getProvider();

        JCPInit.initProviders(provider.equalsIgnoreCase(DefaultProvider.JCSP_PROVIDER_NAME));
        ConnectionManager connectionManager = new ConnectionManager(listenPort);

        connectionManager.start();
        connectionManager.join();

    }

}
