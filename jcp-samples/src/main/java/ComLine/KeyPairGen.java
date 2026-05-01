/**
 * $RCSfile$ version $Revision$ created 20.06.2007 17:15:15 by kunina last
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
import ru.CryptoPro.JCP.KeyStore.CPKeyContainer;
import ru.CryptoPro.JCP.Util.DefaultProviders;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Генерирование ключевой пары в соответствие с алгоритмом ГОСТ Р 34.10-2001 и
 * соответствующего ему самоподписанного сертификата. Запись их на носитель.
 * Генерирование запроса на сертификат и запись его в файл.
 *
 * @kunina Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class KeyPairGen {

    /**
     * logger
     */
    private static Logger log = Logger.getLogger("LOGGER");

    /**
     * forbidden.
     */
    private KeyPairGen() {

    }

    /**
     * KeyPairGen -alias name_of_key [-alg GOST3410] [-storetype HDImageStore]
     * [-storepath null] [-storepass null] [-keypass password] [-isServer true]
     * -dname CN=autor,OU=Security,O=CryptoPro,C=RU -reqCertpath C:/*.* -encoding
     * der
     * <br>
     * <DL> <DT><b> -alias </b>  <DD>уникальное имя записываемого ключа</DD> <DT><b>
     * -alg </b> <DD>алгоритм для генерирования<DD>(по умолчанию GOST3410)</DD>
     * <DT><b> -storetype </b> <DD>имя ключевого носителя HDImageStore (жесткий
     * диск), FloppyStore (дискета), OCFStore (карточки) <DD>(по умолчанию
     * HDImageStore)</DD> <DT><b>-storepath </b>  <DD>путь к хранилищу доверенных
     * сертификатов <DD>(по умолчанию null)</DD> <DT><b> -storepass </b>  <DD>пароль
     * на хранилище доверенных сертификатов <DD>(по умолчанию null)</DD> <DT><b>
     * -keypass </b> <DD>пароль на записываемый ключ <DD>(по умолчанию null)</DD>
     * <DT><b>-isServer</b> <DD>если ключ серверный, то значение true <DD>(по
     * умолчанию false)</DD> <DT><b> -dname</b>  <DD>имя субъекта для генерирования
     * самоподписанного сертификата</DD> <DT><b> -encoding</b>  <DD>кодировка
     * (DER/BASE64) <DD>(по умолчанию DER)</DD> <DT><b> -reqCertpath </b> <DD>путь
     * для записи запроса</DD></DT> </DL>
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {

        if (ComLine.getFunc(ComLine.help, args))
            log.info(ComLine.KeyPairGenHelpHD);
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
        if (ComLine.getFunc(ComLine.help, args))
            log.info(ComLine.KeyPairGenHelpHD);
        else
            try {
                //----------------------------------------------------------------------------//
                //Заполнение таблицы свойств значениями параметров командной строки
                //или значениями по умолчанию при отсутствии первых
                final Properties ArgList = new Properties();
                //уникальное имя записываемого ключа
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
                //пароль на записываемый ключ
                ArgList.setProperty(ComLine.keypass,
                        ComLine.getValue(ComLine.keypass, args, "null"));
                //имя субъекта для генерирования самодписанного сертификата
                ArgList.setProperty(ComLine.dname,
                        ComLine.getValue(ComLine.dname, args, null));
                //алгоритм для генерирования ключевой пары, по умолчанию ГОСТ Р 34.10-2012 (256)
                ArgList.setProperty(ComLine.keyAlgorithm,
                        ComLine.getValue(ComLine.keyAlgorithm, args, JCP.GOST_EL_2012_256_NAME));
                //путь для записи GostCertificateRequest EncodedSelfCert
                ArgList.setProperty(ComLine.reqCertpath,
                        ComLine.getValue(ComLine.reqCertpath, args, null));
                //серверный ли ключ
                ArgList.setProperty(ComLine.isServer,
                        ComLine.getValue(ComLine.isServer, args, "false"));
                //в какой кодировке сохранять запрос
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

                //Проверка правильности имени субъекта для сертификата(длины параметра С)
                final String nf;
                nf = ArgList.getProperty(ComLine.dname);
                final String[] nfs = nf.split(",");
                for (int i = 0; i < nfs.length; i++) {
                    final String[] ss = nfs[i].split("=");
                    if ("C".equals(ss[0]) && ss[1].length() != 2)
                        throw new Exception("Incorrect name of the certificate");
                }
//============================================================================//
                //Генерирование ключевой пары в соответствие с алгоритмом ГОСТ Р 34.10-2001
                //и соответствующего ему самоподписанного сертификата.
                //Запись их на носитель. Запись запроса на сертификат (DER) в файл
                keyGen(
                        ArgList.getProperty(ComLine.ALIAS),
                        ArgList.getProperty(ComLine.keyAlgorithm),
                        ArgList.getProperty(ComLine.storetype),
                        ArgList.getProperty(ComLine.keypass),
                        ArgList.getProperty(ComLine.storeprovider),
                        ArgList.getProperty(ComLine.dname),
                        ArgList.getProperty(ComLine.storepass),
                        ArgList.getProperty(ComLine.storepath),
                        ArgList.getProperty(ComLine.reqCertpath),
                        ArgList.getProperty(ComLine.isServer),
                        ArgList.getProperty(ComLine.encoding));

            } catch (NullPointerException e) {
                log.info(ComLine.KeyPairGenHelpHD);
            } catch (IllegalArgumentException e) {
                log.info("\n" +
                        e.toString() + "\n" + ComLine.KeyPairGenHelpHD);
            } catch (ArrayIndexOutOfBoundsException e) {
                log.info(ComLine.KeyPairGenHelpHD);
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

    /**
     * Генерирование ключевой пары и запроса на сертификат.
     *
     * @param alias уникальное имя записываемого ключа
     * @param keyAlgorithm алгоритм ГОСТ Р 34.10-2001 (генерирование ключ.
     * пары)
     * @param keystoreName имя ключевого носителя
     * @param keypass пароль на записываемый ключ
     * @param name имя субъекта для генерирования самодписанного сертификата
     * @param keystorePass пароль на хранилище доверенных сертификатов
     * @param keystorePath путь к хранилищу доверенных сертификатов
     * @param reqpath путь к файлу для записи запроса
     * @param server true - ключ аутентификации сервера, false - ключ аутентификации
     * клиента
     * @param encoding der - запись запроса в der-кодировке, base64 - запись запроса
     * в base64-кодировке
     * @throws Exception ...
     */
private static void keyGen(String alias, String keyAlgorithm,
                               String keystoreName, String keypass,
                               String keyStoreProvider, String name,
                               String keystorePass, String keystorePath,
                               String reqpath, String server, String
                               encoding)
            throws Exception {
        //генерирование ключевой пары
    CPKeyContainer.validateCreationAlias(alias);

    String genProvider = ComLine.getKeyGenProvider(keyAlgorithm, keyStoreProvider);
    final KeyPairGenerator kg =
                KeyPairGenerator.getInstance(keyAlgorithm, genProvider);
        final KeyPair pair = kg.generateKeyPair();
        //log.info("Generation of key pair is completed");

        //генерирование самоподписанного сертификата
        final GostCertificateRequest req;
        String signAlgorithm = ComLine.getSignAlgorithm(keyAlgorithm);
        req = new GostCertificateRequest(keyStoreProvider);
        boolean isServer = false;
        if ("true".equalsIgnoreCase(server)) isServer = true;
        req.init(keyAlgorithm, isServer);
        final byte[] encodedCert = req.getEncodedSelfCert(pair, name, signAlgorithm);

        //генерирование самоподписанного сертификата
        final CertificateFactory cf = CertificateFactory.getInstance("X509");
        final Certificate[] certs;
        certs = new Certificate[1];
        certs[0] =
                cf.generateCertificate(new ByteArrayInputStream(encodedCert));
        //log.info("Generation of certificate is completed"); /**/

        //запись в хранилище ключевой пары с самоподписанным сертификатом
        final KeyStore ks = KeyStore.getInstance(keystoreName, keyStoreProvider);
        char[] KeyStorePass = null;
        if (!"null".equalsIgnoreCase(keystorePass)) {
            KeyStorePass = keystorePass.toCharArray();
        }
        InputStream is = null;
        if (!"null".equalsIgnoreCase(keystorePath)) {
            is = new FileInputStream(keystorePath);
        }
        ks.load(is, KeyStorePass);/**/
        if (is != null) is.close();
        final PrivateKey key;
        key = pair.getPrivate();
        char[] Keypass = null;
        if (!"null".equalsIgnoreCase(keypass)) {
            Keypass = keypass.toCharArray();
        }
        ks.setKeyEntry(alias, key, Keypass, certs);
        OutputStream os = null;
        if (!"null".equalsIgnoreCase(keystorePath)) {
            os = new FileOutputStream(keystorePath);
        }
        ks.store(os, KeyStorePass);
        if (os != null) os.close();
        log
                .info("Recording of a private key named \"" + alias + "\" to " +
                        keystoreName + " is completed. Request: " + reqpath);

//----------------------------------------------------------------------------//
        //Подписанный запрос (DER кодировка)
        //Определение параметров открытого ключа субъекта
        final PublicKey publicKey = pair.getPublic();
//    boolean isServer = false;
//    if ("true".equalsIgnoreCase(server)) isServer = true;
//    req.init(keypairAlgorithm, isServer);
        req.setPublicKeyInfo(publicKey);
        //Определение имени субъекта
        req.setSubjectInfo(name);
        //Кодирование и подпись запроса
        final PrivateKey privateKey = pair.getPrivate();
        req.encodeAndSign(privateKey, signAlgorithm);

        //Запись в файл
        boolean toBASE64 = false;
        if ("base64".equalsIgnoreCase(encoding)) toBASE64 = true;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(reqpath);
            final PrintStream stream = new PrintStream(fos);
            if (toBASE64)
                req.printToBASE64(stream);
            else
                req.printToDER(stream);
        } finally {
            if (fos != null) fos.close();
        }
//----------------------------------------------------------------------------//
        //Отправка запроса в центр, получение сертификата и его запись

    /*String httpAddress = "http://www.cryptopro.ru/certsrv/";

    byte[] encCert = req.getEncodedCert(httpAddress);
    Certificate aliascert = cf.generateCertificate(new ByteArrayInputStream(encCert));
    //в хранилище
    ks.load(is, KeyStorePass);
    ks.setCertificateEntry(alias,aliascert);
    ks.store(os, KeyStorePass);

    //в файл
    ks.load(is, KeyStorePass);
    Certificate Cert = ks.getCertificate(alias);
    byte[] encoCert = Cert.getEncoded();
    ks.store(os, KeyStorePass);
    FileOutputStream fost = null;
    try {
        fost = new FileOutputStream("D:\\Job\\test\\myData\\prKey.cer");
        fost.write(encoCert);
    } finally {
        if (fost!=null) fost.close();
    } /**/

    }


}
