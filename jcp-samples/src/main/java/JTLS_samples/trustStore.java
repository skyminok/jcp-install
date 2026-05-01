/**
 * $RCSfile$
 * version $Revision$
 * created 09.07.2007 12:30:08 by kunina
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2007.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован 
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JTLS_samples;

import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.logging.Logger;

/**
 * trust store.
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class trustStore {
/**
 *
 */
private trustStore() {
}

/**
 * Функции записи сертификата из файла в хранилище и удаления сертификата из
 * хранилища.
 *
 * @param args -
 * @throws Exception e
 */
public static void main(String[] args) throws Exception {

    final String keystoreName = "HDImageStore";
    final String keystorePass = "1";
    final String keystorePath = "C:\\trust.store";
    final String certPath = "C:\\Cert.cer";
    final String alias = "Cert";

    JCPInit.initProviders(false);

    //Запись сертификата в хранилище
    addCert(certPath, keystoreName, keystorePass, keystorePath, alias);
    //Удаление сертификата из хранилища
    delCert(keystoreName, keystorePass, keystorePath, alias);

}

/**
 * Запись сертификата в хранилище
 *
 * @param certPath Путь к файлу сертификата
 * @param keystoreName тип хранилища
 * @param keystorePass пароль на хранилище
 * @param keystorePath путь к хранилищу
 * @param alias имя
 * @throws Exception е
 */
public static void addCert(String certPath, String keystoreName,
                           String keystorePass,
                           String keystorePath, String alias) throws Exception {
    final CertificateFactory cf = CertificateFactory.getInstance("X509");
    final Certificate rootCert;
    try (FileInputStream is = new FileInputStream(certPath)) {
        rootCert = cf.generateCertificate(new BufferedInputStream(is));
    }
    final KeyStore ks = KeyStore.getInstance(keystoreName);
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
    ks.setCertificateEntry(alias, rootCert);

    OutputStream os = null;
    if (!"null".equalsIgnoreCase(keystorePath)) {
        os = new FileOutputStream(keystorePath);
    }
    ks.store(os, KeyStorePass);
    if (os != null) os.close();
    Logger.getLogger("LOGGER").info(
            "Recording of a Certificate named \"" + alias + "\" to " +
                    keystoreName + " is completed.");

}

/**
 * Удаление сертификата из хранилища
 *
 * @param keystoreName тип хранилища
 * @param keystorePass пароль на хранилище
 * @param keystorePath путь к хранилищу
 * @param alias имя
 * @throws Exception е
 */
public static void delCert(String keystoreName,
                           String keystorePass,
                           String keystorePath, String alias) throws Exception {

    final KeyStore ks = KeyStore.getInstance(keystoreName);
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
    if (ks.isCertificateEntry(alias)) ks.deleteEntry(alias);

    OutputStream os = null;
    if (!"null".equalsIgnoreCase(keystorePath)) {
        os = new FileOutputStream(keystorePath);
    }
    ks.store(os, KeyStorePass);
    if (os != null) os.close();
    Logger.getLogger("LOGGER").info(
            "Deleting of a Certificate named \"" + alias + "\" to " +
                    keystoreName + " is completed.");

}
}
