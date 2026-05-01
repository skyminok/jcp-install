/**
 * Программный код, содержащийся в этом файле, предназначен для целей обучения.
 * Может быть скопирован или модифицирован при условии сохранения абзацев с
 * указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован для защиты информации.
 * Компания Крипто-Про не несет никакой ответственности за функционирование
 * этого кода.
 */
package ComLine;

import JTLS_samples.connection.SSLConfiguration;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Пример запуска сервера из командной строки (см. JTLS_samples.Server).
 * <br>
 * При запросе "shutdown" прекращение работы сервера...
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class Server {

    public static final String DEFAULT_PROVIDER = "ru.CryptoPro.defaultSSLProv";

    /**
     * logger
     */
    private static Logger log = Logger.getLogger("LOGGER");

    /**/
    private Server() {
    }

    /**
     * Server [-port port] [-auth true] [-keyStoreType HDImageStore]
     * [-trustStoreType HDImageStore] -trustStorePath C:/*.* -trustStorePassword
     * trust_pass -keyStorePassword key_pass
     * <br>
     * </DD> <DL> <DT><b> -port </b>  <DD>порт сервера <DD>(по умолчанию 443)</DD>
     * <DT><b> -auth </b> <DD>нужна ли аутентификация клиента <DD>(по умолчанию
     * false)<br> <DT><b> -keyStoreType </b> <DD>тип ключевого носителя HDImageStore
     * (жесткий диск), FloppyStore (дискета), OCFStore или J6CFStore (карточки), RutokenStore (Рутокен)
     * <DD>(по умолчанию HDImageStore)</DD> <DT><b> -trustStoreType </b> <DD>тип
     * носителя для хранилища доверенных сертификатов HDImageStore (жесткий диск),
     * FloppyStore (дискета) <DD>(по умолчанию HDImageStore)</DD> <DT><b>
     * -trustStorePath </b> <DD>путь к хранилищу доверенных сертификатов</DD>
     * <DT><b> -trustStorePassword </b> <DD>пароль на хранилище доверенных
     * сертификатов</DD> <DT><b> -keyStorePassword </b>  <DD>пароль на ключ</DD>
     * <DT><b> -servDir </b>  <DD>рабочая директория сервера <DD>(по умолчанию
     * текущая)</DD></DT> </DL>
     *
     * @param args аргументы командной строки
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (ComLine.getFunc(ComLine.help, args)) log.info(ComLine.HELP_SERV);
        else {

            //Заполнение таблицы свойств значениями параметров командной строки
            //или значениями по умолчанию при отсутствии первых
            final Properties ArgList = new Properties();

            //провайдер ключевого носителя и подписи
            ArgList.setProperty(ComLine.storeprovider, ComLine.getValue(ComLine.storeprovider, args, JCP.PROVIDER_NAME));

            // инициализация провайдеров
            final String provider = ArgList.getProperty(ComLine.storeprovider);
            JCPInit.initProviders(provider.equalsIgnoreCase(DefaultProvider.JCSP_PROVIDER_NAME));

            main_(args);

        }

    }

    public static void main_(String[] args) throws Exception {
        createServer(args);
    }

    public static JTLS_samples.Server createServer(String[] args) throws Exception {
        if (ComLine.getFunc(ComLine.help, args)) {
            log.info(ComLine.HELP_SERV);
            return null;
        }
        else {

            final JTLS_samples.Server server = new JTLS_samples.Server();

            try {
                //Заполнение таблицы свойств значениями параметров командной строки
                //или значениями по умолчанию при отсутствии первых
                final Properties ArgList = new Properties();
                //порт
                ArgList.setProperty(ComLine.PORT, ComLine.getValue(ComLine.PORT, args, "443"));
                //protocol
                ArgList.setProperty(ComLine.protocol, ComLine.getValue(ComLine.protocol, args, ComLine.GOST_TLS));
                //keyStoreType
                ArgList.setProperty(ComLine.keyStoreType, ComLine.getValue(ComLine.keyStoreType, args, ComLine.HDImageStore));
                //trustStoreType
                ArgList.setProperty(ComLine.trustStoreType, ComLine.getValue(ComLine.trustStoreType, args, ComLine.CertStore));
                //trustStorePath
                ArgList.setProperty(ComLine.trustStorePath, ComLine.getValue(ComLine.trustStorePath, args, null));
                // keyStoreAlias
                ArgList.setProperty(ComLine.keyStoreAlias, ComLine.getValue(ComLine.keyStoreAlias, args, "null"));
                //keyStorePassword
                ArgList.setProperty(ComLine.keyStorePassword, ComLine.getValue(ComLine.keyStorePassword, args, "null"));
                //trustStorePassword
                ArgList.setProperty(ComLine.trustStorePassword, ComLine.getValue(ComLine.trustStorePassword, args, null));
                //authentication of client
                ArgList.setProperty(ComLine.auth, ComLine.getValue(ComLine.auth, args, "false"));
                //server working dir
                ArgList.setProperty(ComLine.servDir, ComLine.getValue(ComLine.servDir, args, new File(".").getCanonicalPath()));
                //провайдер ключевого носителя и подписи
                ArgList.setProperty(ComLine.storeprovider, ComLine.getValue(ComLine.storeprovider, args, JCP.PROVIDER_NAME));

                // инициализация провайдеров
                final String provider = ArgList.getProperty(ComLine.storeprovider);

                //Проверка типа хранилища.
                final String ks;
                ks = ArgList.getProperty(ComLine.keyStoreType);

                String resultingKeyStoreType = ComLine.verifyKeyStoreTypeJavaTLS(ks, provider);
                if (!ks.equalsIgnoreCase(resultingKeyStoreType)) {
                    ArgList.setProperty(ComLine.keyStoreType, resultingKeyStoreType);
                    log.info("Incorrect key store type: " + ks +
                            ". Value by default is appropriated: " + resultingKeyStoreType);
                }

                /*
                if (!"null".equalsIgnoreCase(ArgList.getProperty(ComLine.keyStoreAlias))) {
                    final String keyStAlias = ArgList.getProperty(ComLine.keyStoreAlias);
                    System.setProperty("javax.net.ssl.keyStoreAlias", keyStAlias);
                }

                if (!"null".equalsIgnoreCase(ArgList.getProperty(ComLine.keyStorePassword))) {
                    final String keyStpass = ArgList.getProperty(ComLine.keyStorePassword);
                    System.setProperty("javax.net.ssl.keyStorePassword", keyStpass);
                }
                */

                //Папка сервера.
                if (!new File(ArgList.getProperty(ComLine.servDir)).isDirectory())
                    ArgList.setProperty(ComLine.servDir, new File(".").getCanonicalPath());

                /*
                System.setProperty("javax.net.ssl.keyStoreType",ArgList.getProperty(ComLine.keyStoreType));
                System.setProperty("javax.net.ssl.trustStoreType",ArgList.getProperty(ComLine.trustStoreType));
                System.setProperty("javax.net.ssl.trustStore",ArgList.getProperty(ComLine.trustStorePath));
                System.setProperty("javax.net.ssl.trustStorePassword",ArgList.getProperty(ComLine.trustStorePassword));
                System.setProperty("javax.net.ssl.supportGVO", "true");
                */

                // Конфигурация подключения.

                String trustStorePasswordString = ArgList.getProperty(ComLine.trustStorePassword);
                char[] trustStorePassword = trustStorePasswordString != null ? trustStorePasswordString.toCharArray() : null;

                String keyStoreAliasString = ArgList.getProperty(ComLine.keyStoreAlias);
                keyStoreAliasString = (!keyStoreAliasString.equalsIgnoreCase("null")) ? keyStoreAliasString : null;
                boolean noAlias = (keyStoreAliasString == null);

                String keyStorePasswordString = ArgList.getProperty(ComLine.keyStorePassword);
                char[] keyStorePassword = (!keyStorePasswordString.equalsIgnoreCase("null")) ? keyStorePasswordString.toCharArray() : null;

                final String protocol = ArgList.getProperty(ComLine.protocol);
                final int sslPort = Integer.decode(ArgList.getProperty(ComLine.PORT));

                boolean clientAuth = false;

                if (ArgList.getProperty(ComLine.auth).equalsIgnoreCase("true"))
                    clientAuth = true;

                SSLConfiguration sslConfig = new SSLConfiguration(
                    ArgList.getProperty(ComLine.trustStoreType),
                    ArgList.getProperty(ComLine.trustStorePath),
                    trustStorePassword,
                    clientAuth,
                    ArgList.getProperty(ComLine.keyStoreType),
                    keyStoreAliasString,
                    keyStorePassword
                );

                // сервер с аутентификацией auth
                server.create(sslConfig, sslPort, ArgList.getProperty(ComLine.servDir), protocol, noAlias);
                server.setTimeout(3000000);
                server.start();

                if (!server.isAlive()) throw new IOException();
                return server;

            } catch (NullPointerException e) {
                log.info(ComLine.HELP_SERV);
            } catch (ArrayIndexOutOfBoundsException e) {
                log.info(ComLine.HELP_SERV);
            }
        }

        return null;
    }

}
