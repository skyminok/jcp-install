/**
 * $RCSfile$ version $Revision$ created 26.06.2007 14:59:43 by kunina last
 * modified $Date$ by $Author$ (C) ООО Крипто-Про 2004-2007.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен для целей обучения.
 * Может быть скопирован или модифицирован при условии сохранения абзацев с
 * указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован для защиты информации.
 * Компания Крипто-Про не несет никакой ответственности за функционирование
 * этого кода.
 */
package ComLine;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.tools.Array;

import ru.CryptoPro.JCP.tools.Decoder;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Получение сертификата из запроса, представленного в DER-кодировке и запись
 * его в хранилище и в файл
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class getCert {

    /**
     * logger
     */
    private static Logger log = Logger.getLogger("LOGGER");

    /**
     * forbidden
     */
    private getCert() {
    }

    /**
     * getCert -alias name_of_key [-storetype HDImageStore] [-storepath null]
     * [-storepass null] -http http://www.cryptopro.ru/certsrv/ -certpath C:/*.cer
     * -reqCertpath C:/*.*
     * <br>
     * <DL> <DT><b> -alias </b>  <DD>уникальное имя ключа</DD> <DT><b> -storetype
     * </b> <DD>имя ключевого носителя HDImageStore (жесткий диск), FloppyStore
     * (дискета), OCFStore или J6CFStore (карточки), RutokenStore (Рутокен)<DD>(по умолчанию
     * HDImageStore)</DD> <DT><b>-storepath </b> <DD>путь к хранилищу доверенных
     * сертификатов <DD>(по умолчанию null)</DD> <DT><b> -storepass </b> <DD>пароль
     * на хранилище доверенных сертификатов <DD>(по умолчанию null)</DD> <DT><b>
     * -http </b> <DD>путь к центру сертификации</DD> <DT><b> -reqCertpath</b>
     * <DD>путь к файлу с запросом</DD> <DT><b> -encoding</b>  <DD>кодировка запроса
     * (DER/BASE64)<DD>(по умочанию DER)</DD> <DT><b> -сertpath</b> <DD>путь к файлу
     * для записи сертификата</DD></DT> </DL>
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {

        if (ComLine.getFunc(ComLine.help, args)) log.info(ComLine.CertsHelpHD);
        else {

            //Заполнение таблицы свойств значениями параметров командной строки
            //или значениями по умолчанию при отсутствии первых
            final Properties ArgList = new Properties();

            //провайдер ключевого носителя и подписи
            ArgList.setProperty(ComLine.storeprovider,
                    ComLine.getValue(ComLine.storeprovider, args,
                            JCP.PROVIDER_NAME));

            // инициализация провайдеров
            final String provider =
                    ArgList.getProperty(ComLine.storeprovider);

            JCPInit.initProviders(provider.equalsIgnoreCase(
                    DefaultProvider.JCSP_PROVIDER_NAME));

            main_(args);

        }

    }

    public static void main_(String[] args) {
        if (ComLine.getFunc(ComLine.help, args)) log.info(ComLine.CertsHelpHD);
        else
            try {
                //Заполнение таблицы свойств значениями параметров командной строки
                //или значениями по умолчанию при отсутствии первых
                final Properties ArgList = new Properties();
                //уникальное имя ключа
                ArgList.setProperty(ComLine.ALIAS,
                        ComLine.getValue(ComLine.ALIAS, args, null));
                //тип ключевого носителя
                ArgList.setProperty(ComLine.storetype,
                        ComLine.getValue(ComLine.storetype, args,
                                ComLine.HDImageStore));
                //путь к хранилищу доверенных сертификатов
                ArgList.setProperty(ComLine.storepath,
                        ComLine.getValue(ComLine.storepath, args, "null"));
                //пароль на хранилище доверенных сертификатов
                ArgList.setProperty(ComLine.storepass,
                        ComLine.getValue(ComLine.storepass, args, "null"));
                //путь к центру сертификации
                ArgList.setProperty(ComLine.http,
                        ComLine.getValue(ComLine.http, args, null));
                //путь к запросу на сертификат
                ArgList.setProperty(ComLine.reqCertpath,
                        ComLine.getValue(ComLine.reqCertpath, args, null));
                //путь к файлу для записи сертификата
                ArgList.setProperty(ComLine.certpath,
                        ComLine.getValue(ComLine.certpath, args, null));
                //кодировка запроса
                ArgList.setProperty(ComLine.encoding,
                        ComLine.getValue(ComLine.encoding, args, "der"));
                //провайдер ключевого носителя и подписи
                ArgList.setProperty(ComLine.storeprovider,
                        ComLine.getValue(ComLine.storeprovider, args,
                                JCP.PROVIDER_NAME));

                //Проверка типа хранилища.
                final String ks;
                ks = ArgList.getProperty(ComLine.storetype);
                final String sp;
                sp = ArgList.getProperty(ComLine.storeprovider);

                String resultingKeyStoreType = ComLine.verifyKeyStoreType(ks,sp);
                if (!ks.equalsIgnoreCase(resultingKeyStoreType))
                {
                    ArgList.setProperty(ComLine.storetype, resultingKeyStoreType);
                    log.info("Incorrect key store type: " + ks +
                            ". Value by default is appropriated: " + resultingKeyStoreType);
                }

                //Запрос сертификата и его запись
                getCert(
                        ArgList.getProperty(ComLine.ALIAS),
                        ArgList.getProperty(ComLine.reqCertpath),
                        ArgList.getProperty(ComLine.storetype),
                        ArgList.getProperty(ComLine.storepass),
                        ArgList.getProperty(ComLine.storepath),
                        ArgList.getProperty(ComLine.storeprovider),
                        ArgList.getProperty(ComLine.http),
                        ArgList.getProperty(ComLine.certpath),
                        ArgList.getProperty(ComLine.encoding));

            } catch (NullPointerException e) {
                //System.out.println(e.toString());
                log.info(ComLine.GetCertHelpHD);
            } catch (Exception e1) {
                final String sss = "java.lang.Exception:";
                log.info("\n" +
                        e1.toString()
                                .substring(sss.length(), e1.toString().length()) +
                        "\n" + ComLine.GetCertHelpHD);
            }

    }

    /**
     * @param alias имя ключа
     * @param reqpath путь к файлу с запросом
     * @param keystoreName тип ключевого носителя
     * @param keystorePass пароль на хранилище доверенных сертификатов
     * @param keystorePath путь к хранилищу доверенных сертификатов
     * @param httpAddr путь к центру сертификации
     * @param cerpath путь к файлу для записи сертификата
     * @param encoding кодировка запроса
     * @throws Exception ...
     */
    private static void getCert(String alias,
                                String reqpath,
                                String keystoreName,
                                String keystorePass,
                                String keystorePath,
                                String keyStoreProvider,
                                String httpAddr,
                                String cerpath,
                                String encoding) throws Exception {

        //чтение запроса из файла
        InputStream is = null;
        FileOutputStream fost = null;
        final byte[] encodedCert;
        try {

            //запись в хранилище
            final KeyStore ks = KeyStore.getInstance(keystoreName, keyStoreProvider);
            char[] KeyStorePass = null;
            if (!"null".equalsIgnoreCase(keystorePass)) {
                KeyStorePass = keystorePass.toCharArray();
            }
            InputStream Is = null;
            if (!"null".equalsIgnoreCase(keystorePath)) {
                Is = new FileInputStream(keystorePath);
            }
            ks.load(Is, KeyStorePass);
            if (Is != null) Is.close();
            if (!"null".equalsIgnoreCase(httpAddr)) {
                is = new FileInputStream(reqpath);
                if ("base64".equals(encoding)) {
                    //Получение сертификата из запроса, представленного в BASE64 кодировке
                    encodedCert = GostCertificateRequest.getEncodedCertFromBASE64(httpAddr, is);
                } else {
                    //Получение сертификата из запроса, представленного в DER кодировке
                    encodedCert = GostCertificateRequest.getEncodedCertFromDER(httpAddr, is);
                }
            }
            else {
                byte[] readRequest = Array.readFile(reqpath);
                byte[] encodedRequest;
                if ("base64".equals(encoding)) {
                    String reqB64s = new String(readRequest);
                    reqB64s = reqB64s.replaceAll(GostCertificateRequest.BEGIN_STRING, "");
                    reqB64s = reqB64s.replaceAll(GostCertificateRequest.END_STRING, "");
                    reqB64s = reqB64s.replaceAll("\r\n", "");
                    encodedRequest = new Decoder().decodeBuffer(new ByteArrayInputStream(reqB64s.getBytes(StandardCharsets.UTF_8)));
                } else {
                    encodedRequest = readRequest;
                }
                JCPProtectionParameter parameter = new JCPProtectionParameter(keystorePass.toCharArray(), true, true);
                JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) ks.getEntry(alias, parameter);
                GostCertificateRequest gs = new GostCertificateRequest(keyStoreProvider);
                gs.decodeRequest(encodedRequest);
                encodedCert = gs.getEncodedSelfCert(new KeyPair(null, entry.getPrivateKey()), "CN=" + alias);
            }
            //запись в файл
            fost = new FileOutputStream(cerpath);
            fost.write(encodedCert);

            final CertificateFactory cf = CertificateFactory.getInstance("X509");
            final Certificate cert;
            cert = cf.generateCertificate(new ByteArrayInputStream(encodedCert));
            ks.setCertificateEntry(alias, cert);
            OutputStream os = null;
            if (!"null".equalsIgnoreCase(keystorePath)) {
                os = new FileOutputStream(keystorePath);
            }
            ks.store(os, KeyStorePass);
            if (os != null) os.close();
            log.info(
                    "Recording of a certificate to " + cerpath + " and to " +
                            keystoreName + " is completed.");
        } finally {
            if (is != null) is.close();
            if (fost != null) fost.close();
        }
    }
}
