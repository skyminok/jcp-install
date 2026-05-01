/**
 * $RCSfile$
 * version $Revision$
 * created 07.07.2008 15:20:54 by kunina
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2008.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован 
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;

/**
 * Отпечаток сертификата.
 *
 * @author Copyright 2004-2008 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class CertFingerPrint {
 /**/
private CertFingerPrint() {
}

/**
 * Пример получения отпечатка сертификата (сначала он генерируется)
 *
 * @param args /
 * @throws Exception /
 */
public static void main(String[] args) throws Exception {
    JCPInit.initProviders(false);
    //генерирование сертификата для примера
    final KeyPairGenerator kg =
            KeyPairGenerator.getInstance(Constants.SIGN_KEY_PAIR_ALG_2001);
    final GostCertificateRequest gr = new GostCertificateRequest();
    final CertificateFactory cf =
            CertificateFactory.getInstance(Constants.CF_ALG);
    final KeyPair kp = kg.generateKeyPair();
    final String name = "CN=cert";
    final byte[] enc = gr.getEncodedSelfCert(kp, name);
    final Certificate cert =
            cf.generateCertificate(new ByteArrayInputStream(enc));
    //получение отпечатка сертификата
    final String fp = fingerPrintCert(cert, "SHA1");
    System.out.println(fp);
}

/**
 * Получение отпечатка сертификата
 *
 * @param cert сертификат
 * @param alg алгоритм (SHA1)
 * @return hex-string
 * @throws CertificateEncodingException /
 * @throws NoSuchAlgorithmException /
 */
private static String fingerPrintCert(Certificate cert, String alg)
        throws CertificateEncodingException, NoSuchAlgorithmException {
    final byte[] encCertInfo = cert.getEncoded();
    final MessageDigest md = MessageDigest.getInstance(alg);
    final byte[] digest = md.digest(encCertInfo);
    return Constants.toHexString(digest);
}
}
