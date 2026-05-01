/**
 * $RCSfile$
 * version $Revision$
 * created 05.07.2007 10:24:37 by kunina
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

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;

import ru.CryptoPro.JCPRequest.GostCertificateRequest;
import userSamples.Certificates;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 * ключ (сервер).
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class servkey {
/**
 *
 */
private servkey() {
}

public static void main(String[] args) throws Exception {
    JCPInit.initProviders(false);
    final KeyPairGenerator kg = KeyPairGenerator.getInstance(JCP.GOST_EL_DH_NAME);
    final KeyPair pair = kg.generateKeyPair();
    // закрытый ключ обмена
    final PrivateKey privKey = pair.getPrivate();
    // соответствующий ему открытый ключ
    final PublicKey pubKey = pair.getPublic();

    final String keyAlg = JCP.GOST_EL_DH_NAME;
    final boolean isServer = true;
    final String httpAddress = Certificates.HTTP_ADDRESS;
    final String certName = "CN=serv, O=CryptoPro, C=RU";
    final String keystorePass = "null";
    final String keystorePath = "null";
    final String keypass = "pass";
    final String alias = "serKey";
    final String keystoreName = JCP.HD_STORE_NAME;

    // создание запроса на сертификат аутентификации сервера
    final GostCertificateRequest req = new GostCertificateRequest();
    req.init(keyAlg, isServer);
    req.setPublicKeyInfo(pubKey);
    //Определение имени субъекта
    req.setSubjectInfo(certName);
    req.encodeAndSign(privKey);

    // отправка запроса центру сертификации и получение от центра
    // сертификата в DER-кодировке
    byte[] encoded = req.getEncodedCert(httpAddress);

    // генерирование X509-сертификата из закодированного представления сертификата
    CertificateFactory cf = CertificateFactory.getInstance("X509");
    Certificate cert =
            cf.generateCertificate(new ByteArrayInputStream(encoded));

    // забираем корневой сертификат УЦ с того же адреса
    final Certificate certRoot = cf.generateCertificate(
            new ByteArrayInputStream(
                    GostCertificateRequest.getEncodedRootCert(httpAddress)));

    final Certificate[] certs = new Certificate[1];
    certs[0] = cert;

    final KeyStore ks = KeyStore.getInstance(keystoreName);
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
    char[] Keypass = null;
    if (!"null".equalsIgnoreCase(keypass)) {
        Keypass = keypass.toCharArray();
    }
    ks.setKeyEntry(alias, privKey, Keypass, certs);
    ks.setCertificateEntry("CPTrootCert", certRoot);
    OutputStream os = null;
    if (!"null".equalsIgnoreCase(keystorePath)) {
        os = new FileOutputStream(keystorePath);
    }
    ks.store(os, KeyStorePass);
    if (os != null) os.close();
    System.out.println("Recording of a private key named \"" + alias + "\" to " + keystoreName + " is completed.");

}
}
