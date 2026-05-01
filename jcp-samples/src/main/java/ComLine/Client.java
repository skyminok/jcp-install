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
import JTLS_samples.connection.SSLConnector;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.Util.JCPInit;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * Пример запуска клиента из командной строки (см. JTLS_samples.Client).
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class Client {

    public static final String DEFAULT_PROVIDER = "ru.CryptoPro.defaultSSLProv";

    /**
     * logger
     */
    private static Logger log = Logger.getLogger("LOGGER");

    /**/
    private Client() {
    }

    /**
     * Client [-port port] [-server serverName] [-keyStoreType HDImageStore]
     * [-trustStoreType HDImageStore] -trustStorePath C:/*.* -trustStorePassword
     * trust_pass -keyStorePassword key_pass [-fileget gettingFileName] [-fileout
     * outputFilePath]
     * <br>
     * </DD> <DL> <DT><b> -port </b>  <DD>порт сервера <DD>(по умолчанию 443)</DD>
     * <DT><b> -server </b> <DD>имя сервера <DD>(по умолчанию localhost)<br> <DT><b>
     * -keyStoreType </b> <DD>тип ключевого носителя HDImageStore (жесткий диск),
     * FloppyStore (дискета), OCFStore или J6CFStore (карточки), RutokenStore (Рутокен)
     * <DD>(по умолчанию HDImageStore)</DD> <DT><b> -trustStoreType </b> <DD>тип носителя для
     * хранилища доверенных сертификатов HDImageStore (жесткий диск), FloppyStore
     * (дискета) <DD>(по умолчанию HDImageStore)</DD> <DT><b> -trustStorePath </b>
     * <DD>путь к хранилищу доверенных сертификатов</DD> <DT><b> -trustStorePassword
     * </b> <DD>пароль на хранилище доверенных сертификатов</DD> <DT><b>
     * -keyStorePassword </b>  <DD>пароль на ключ</DD> <DT><b> -fileget</b>  <DD>имя
     * ресурса <DD>(по умолчанию index.html)</DD> <DT><b> -fileout </b> <DD>путь к
     * файлу вывода <DD>(по умолчанию out.html)<br></DT> </DL>
     *
     * @param args аргументы командной строки
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {

        if (ComLine.getFunc(ComLine.help, args)) log.info(ComLine.HELP_CLIENT);
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
        if (ComLine.getFunc(ComLine.help, args)) log.info(ComLine.HELP_CLIENT);
        else
            try {

                //Заполнение таблицы свойств значениями параметров командной строки
                //или значениями по умолчанию при отсутствии первых
                final Properties ArgList = new Properties();
                //порт
                ArgList.setProperty(ComLine.PORT, ComLine.getValue(ComLine.PORT, args, "443"));
                //хост
                ArgList.setProperty(ComLine.SERVER, ComLine.getValue(ComLine.SERVER, args, "localhost"));
                //protocol
                ArgList.setProperty(ComLine.protocol, ComLine.getValue(ComLine.protocol, args, ComLine.GOST_TLS_12));
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
                //Get file (filename)
                ArgList.setProperty(ComLine.fileget, ComLine.getValue(ComLine.fileget, args, "index.html"));
                //file for output (fileout path)
                ArgList.setProperty(ComLine.fileout, ComLine.getValue(ComLine.fileout, args, "out.html"));
                //провайдер ключевого носителя и подписи
                ArgList.setProperty(ComLine.storeprovider, ComLine.getValue(ComLine.storeprovider, args, JCP.PROVIDER_NAME));

                ArgList.setProperty(ComLine.separateThread, ComLine.getBooleanValue(ComLine.separateThread, args, "false"));
                boolean isSeparateThread = Boolean.parseBoolean(ArgList.getProperty(ComLine.separateThread));

                ArgList.setProperty(ComLine.testClose, ComLine.getBooleanValue(ComLine.testClose, args, "false"));
                boolean isTestClose = Boolean.parseBoolean(ArgList.getProperty(ComLine.testClose));

                ArgList.setProperty(ComLine.separateHandshake, ComLine.getBooleanValue(ComLine.separateHandshake, args, "false"));
                boolean isSeparateHandshake = Boolean.parseBoolean(ArgList.getProperty(ComLine.separateHandshake));

                //Проверка типа хранилища.
                String provider = System.getProperty(DEFAULT_PROVIDER, JCP.PROVIDER_NAME);
                final String ks;
                ks = ArgList.getProperty(ComLine.keyStoreType);

                String resultingKeyStoreType = ComLine.verifyKeyStoreTypeJavaTLS(ks,provider);
                if (!ks.equalsIgnoreCase(resultingKeyStoreType))
                {
                    ArgList.setProperty(ComLine.keyStoreType, resultingKeyStoreType);
                    log.info("Incorrect key store type: " + ks +
                            ". Value by default is appropriated: " + resultingKeyStoreType);
                }

                /*
                if (!"null".equalsIgnoreCase(ArgList.getProperty(ComLine.keyStorePassword))) {
                    final String keyStpass = ArgList.getProperty(ComLine.keyStorePassword);
                    System.setProperty("javax.net.ssl.keyStorePassword", keyStpass);
                }

                System.setProperty("javax.net.ssl.keyStoreType", ArgList.getProperty(ComLine.keyStoreType));
                System.setProperty("javax.net.ssl.trustStoreType", ArgList.getProperty(ComLine.trustStoreType));
                System.setProperty("javax.net.ssl.trustStore", ArgList.getProperty(ComLine.trustStorePath));
                System.setProperty("javax.net.ssl.trustStorePassword", ArgList.getProperty(ComLine.trustStorePassword));
                */

                //System.setProperty("javax.net.ssl.supportGVO", "true");

                //=============================================================================

                final String protocol = ArgList.getProperty(ComLine.protocol);
                final int sslPort = Integer.decode(ArgList.getProperty(ComLine.PORT));
                final String sslHost = ArgList.getProperty(ComLine.SERVER);

                // Конфигурация подключения.

                String trustStorePasswordString = ArgList.getProperty(ComLine.trustStorePassword);
                char[] trustStorePassword = trustStorePasswordString != null ? trustStorePasswordString.toCharArray() : null;

                String keyStoreAliasString = ArgList.getProperty(ComLine.keyStoreAlias);
                keyStoreAliasString = (!keyStoreAliasString.equalsIgnoreCase("null")) ? keyStoreAliasString : null;

                String keyStorePasswordString = ArgList.getProperty(ComLine.keyStorePassword);
                char[] keyStorePassword = (!keyStorePasswordString.equalsIgnoreCase("null")) ? keyStorePasswordString.toCharArray() : null;

                boolean clientAuth = keyStorePassword != null;

                SSLConfiguration sslConfig = new SSLConfiguration(
                    ArgList.getProperty(ComLine.trustStoreType),
                    ArgList.getProperty(ComLine.trustStorePath),
                    trustStorePassword,
                    clientAuth,
                    ArgList.getProperty(ComLine.keyStoreType),
                    keyStoreAliasString,
                    keyStorePassword
                );

                // Контекст подключения.

                SSLConnector clientSslConn = new SSLConnector(sslConfig);
                clientSslConn.prepare(false);
                SSLContext clientSslContext = clientSslConn.create(protocol);

                // Соединение и получение файла.
                if (!isSeparateThread) {
                    final JTLS_samples.Client client = new JTLS_samples.Client(sslHost, sslPort);
                    client.setTimeout(3000000);
                    if (client.get(clientSslContext, ArgList.getProperty(ComLine.fileget), ArgList.getProperty(ComLine.fileout), null) != 0) {
                        throw new IOException("Couldn't get data.");
                    } // if
                }
                else{
                    final JTLS_samples.SeparateClient client = new JTLS_samples.SeparateClient(sslHost, sslPort);
                    client.setTimeout(3000000);
                    if (client.get(clientSslContext, ArgList.getProperty(ComLine.fileget), null, isSeparateHandshake, isTestClose) != 0) {
                        throw new IOException("Couldn't get data.");
                    } // if
                }

            } catch (NullPointerException e) {
                log.info(ComLine.HELP_CLIENT);
            } catch (ArrayIndexOutOfBoundsException e) {
                log.info(ComLine.HELP_CLIENT);
            }
    }
}
