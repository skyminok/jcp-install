/**
 * $RCSfile$
 * version $Revision$
 * created 16.02.2009 13:18:24 by kunina
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2009.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован 
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package Crypt_samples;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * создание контейнеров для примеров Encrypt.java и Decrypt.java
 *
 * @author Copyright 2004-2009 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class GenKeys {
/**
 * имя отправителя (контейнер, сертификат)
 */
public static final String SENDER = "Sender";
/**
 * имя получателя (контейнер, сертификат)
 */
public static final String RESPONDER = "Responder";
/**
 * рабочая директория
 */
public static String W_PATH = "C:\\TESTS";
/**
 * расширение файла сертификата
 */
public static final String CERT_EXT = ".cer";

/**
 * @param args *
 * @throws Exception /
 */
public static void main(String[] args) throws Exception {
    JCPInit.initProviders(false);
    main_(args);
}

public static void main_(String[] args) throws Exception {
    //создание контейнеров для примеров Encrypt и Decrypt
    createContainer(SENDER, JCP.GOST_EL_DH_NAME, JCP.GOST_EL_SIGN_NAME);
    createContainer(RESPONDER, JCP.GOST_EL_DH_NAME, JCP.GOST_EL_SIGN_NAME);
    //экспорт сертификатов
    expCert(SENDER, W_PATH);
    expCert(RESPONDER, W_PATH);
}

/**
 * @param name имя
 * @param pathh рабочая директория
 * @throws KeyStoreException /
 * @throws NoSuchAlgorithmException /
 * @throws IOException /
 * @throws CertificateException /
 */
public static void expCert(String name, String pathh) throws KeyStoreException,
        NoSuchAlgorithmException, IOException, CertificateException {
    final KeyStore ks = KeyStore.getInstance(JCP.HD_STORE_NAME);
    ks.load(null, null);
    final Certificate cert = ks.getCertificate(name);
    Array.writeFile(pathh + File.separator + name + CERT_EXT,
            cert.getEncoded());
}

/**
 * @param name имя контейнера
 * @throws NoSuchAlgorithmException /
 * @throws IOException /
 * @throws SignatureException /
 * @throws InvalidKeyException /
 * @throws CertificateException /
 * @throws KeyStoreException /
 */
public static void createContainer(String name, String keyAlgName, String signAlgName)
        throws NoSuchAlgorithmException, IOException, SignatureException,
        InvalidKeyException, CertificateException, KeyStoreException, NoSuchProviderException {
    final KeyPairGenerator kg = KeyPairGenerator.getInstance(keyAlgName);
    final KeyPair keyPair = kg.generateKeyPair();
    //генерирование самоподписанного сертификата(клиент)
    final GostCertificateRequest req = new GostCertificateRequest();
    req.init(keyAlgName, false);
    final byte[] encodedCert = req.getEncodedSelfCert(keyPair, "CN=" + name, signAlgName);

    //генерирование самоподписанного сертификата
    final CertificateFactory cf = CertificateFactory.getInstance("X509");
    final Certificate[] certs;
    certs = new Certificate[1];
    certs[0] = cf.generateCertificate(new ByteArrayInputStream(encodedCert));

    //запись в хранилище ключевой пары с самоподписанным сертификатом
    final KeyStore ks = KeyStore.getInstance(JCP.HD_STORE_NAME);
    ks.load(null, null);
    ks.setKeyEntry(name, keyPair.getPrivate(), null, certs);
}
}
