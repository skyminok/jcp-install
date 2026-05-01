/**
 * $RCSfile$ version $Revision$ created 28.06.2007 14:49:57 by kunina last
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

import ru.CryptoPro.JCP.Util.DirList;
import ru.CryptoPro.JCP.Util.DefaultProvider;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Построение цепочки сертификатов
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class Certs {

/**
 * logger
 */
private static Logger log = Logger.getLogger("LOGGER");

/**
 * forbidden
 */
private Certs() {
}

/**
 * Certs -alias name_of_key [-storetype HDImageStore] [-storepath null]
 * [-storepass null] [-keypass password] -certs C:/my.cer,C:/*.cer,...,C:/root.cer
 * <p/>
 * <DL> <DT><b> -alias </b>  <DD>уникальное имя ключа</DD> <DT><b> -keypass </b>
 * <DD>пароль на ключ <DD>(по умолчанию null)</DD> <DT><b> -storetype </b>
 * <DD>имя ключевого носителя HDImageStore (жесткий диск), FloppyStore
 * (дискета), OCFStore или J6CFStore (карточки), RutokenStore (Рутокен) <DD>(по умолчанию
 * HDImageStore)</DD> <DT><b>-storepath </b> <DD>путь к хранилищу доверенных
 * сертификатов <DD>(по умолчанию null)</DD> <DT><b> -storepass </b> <DD>пароль
 * на хранилище доверенных сертификатов <DD>(по умолчанию null)</DD> <DT><b>
 * -сerts</b> <DD>пути к сертификатам</DD></DT> </DL>
 *
 * @param args аргументы командной строки
 */
public static void main(String[] args) {
    if (ComLine.getFunc(ComLine.help, args)) log.info(ComLine.CertsHelpHD);
    else
        try {
            //Заполнение таблицы свойств значениями параметров командной строки
            //или значениями по умолчанию при отсутствии первых
            final Properties ArgList = new Properties();
            //уникальное имя ключа
            ArgList.setProperty(ComLine.ALIAS,
                    ComLine.getValue(ComLine.ALIAS, args, null));
            //пароль на записываемый ключ
            ArgList.setProperty(ComLine.keypass,
                    ComLine.getValue(ComLine.keypass, args, "null"));
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
            //провайдер ключевого носителя и подписи
            ArgList.setProperty(ComLine.storeprovider,
                    ComLine.getValue(ComLine.storeprovider, args,
                            JCP.PROVIDER_NAME));
            //пути к сертификатам
            ArgList.setProperty(ComLine.certs,
                    ComLine.getValue(ComLine.certs, args, null));

            //Проверка типа хранилища.
            final String ks;
            ks = ArgList.getProperty(ComLine.storetype);
            final String provider = ArgList.getProperty(ComLine.storeprovider);

            String resultingKeyStoreType = ComLine.verifyKeyStoreType(ks,provider);
            if (!ks.equalsIgnoreCase(resultingKeyStoreType))
            {
                ArgList.setProperty(ComLine.storetype, resultingKeyStoreType);
                log.info("Incorrect key store type: " + ks +
                        ". Value by default is appropriated: " + resultingKeyStoreType);
            }

            // инициализация провайдеров
            JCPInit.initProviders(provider.equalsIgnoreCase(
                    DefaultProvider.JCSP_PROVIDER_NAME));

            //Построение цепочки сертификатов и ее запись к ключу
            Certs(
                    ArgList.getProperty(ComLine.ALIAS),
                    ArgList.getProperty(ComLine.keypass),
                    ArgList.getProperty(ComLine.storetype),
                    ArgList.getProperty(ComLine.storepass),
                    ArgList.getProperty(ComLine.storepath),
                    ArgList.getProperty(ComLine.storeprovider),
                    ArgList.getProperty(ComLine.certs));

        } catch (NullPointerException e) {
            //System.out.println(e.toString());
            log.info(ComLine.CertsHelpHD);
        } catch (ArrayIndexOutOfBoundsException ae) {
            log
                    .info("\nWrong input in command line (" + ComLine.certs +
                            ")\n\n" + ComLine.CertsHelpHD);
        } catch (Exception e1) {
            final String sss = "java.lang.Exception:";
            log.info("\n" +
                    e1.toString()
                            .substring(sss.length(), e1.toString().length()) +
                    "\n" + ComLine.CertsHelpHD);
        }

}

/**
 * @param alias имя ключа
 * @param keypass пароль на ключ
 * @param keystoreName тип ключевого носителя
 * @param keystorePass пароль на хранилище доверенных сертификатов
 * @param keystorePath путь к хранилищу доверенных сертификатов
 * @param certs пути к сертификатам
 * @throws Exception ...
 */
private static void Certs(String alias,
                          String keypass,
                          String keystoreName,
                          String keystorePass,
                          String keystorePath,
                          String keyStoreProvider,
                          String certs) throws Exception {

    //Чтение сертификатов
    final String[] certnames = certs.split(",");
    final CertificateFactory cf = CertificateFactory.getInstance("X509");
    final Certificate[] Certificates = new Certificate[certnames.length];
    for (int i = 0; i < certnames.length; i++) {
        try (FileInputStream fIn = new FileInputStream(certnames[i])) {
            byte[] certBody = DirList.convertInputStreamToByteArray(fIn);
            certBody = DirList.intellectDecode(certBody);
            Certificates[i] = cf.generateCertificate(new ByteArrayInputStream(certBody));
        }
    }
    //запись в хранилище ключевой пары и сертификатов
    final KeyStore ks = KeyStore.getInstance(keystoreName, keyStoreProvider);
    char[] KeyStorePass = null;
    if (!"null".equalsIgnoreCase(keystorePass)) {
        KeyStorePass = keystorePass.toCharArray();
    }
    InputStream is = null;
    if (!"null".equalsIgnoreCase(keystorePath)) {
        is = new FileInputStream(keystorePath);
    }
    ks.load(is, KeyStorePass);
    if (is != null) is.close();
    final PrivateKey key;
    char[] Keypass = null;
    if (!"null".equalsIgnoreCase(keypass)) {
        Keypass = keypass.toCharArray();
    }
    key = (PrivateKey) ks.getKey(alias, Keypass);
    ks.setKeyEntry(alias, key, Keypass, Certificates);
    OutputStream os = null;
    if (!"null".equalsIgnoreCase(keystorePath)) {
        os = new FileOutputStream(keystorePath);
    }
    ks.store(os, KeyStorePass);
    if (os != null) os.close();
    log.info(
            "Recording of certificates to " + keystoreName +
                    " is completed.");
}
}
