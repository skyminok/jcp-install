/**
 * $RCSfile$
 * version $Revision$
 * created 06.10.2008 17:31:06 by kunina
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

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Только для java 1.5 и выше.
 * <br>
 * http://java.sun.com/javase/6/docs/technotes/guides/security/certpath/CertPathProgGuide.html
 *
 * @author Copyright 2004-2008 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class OCSPValidateCert {

public static void main(String[] args) throws Exception {
    JCPInit.initProviders(false);
    final KeyPairGenerator kg =
            KeyPairGenerator.getInstance(JCP.GOST_EL_DEGREE_NAME);
    final KeyPair pair = kg.generateKeyPair();

    final GostCertificateRequest req = new GostCertificateRequest();
    req.init(JCP.GOST_EL_DEGREE_NAME, false);
    req.setPublicKeyInfo(pair.getPublic());
    req.setSubjectInfo("CN=TEST_OCSP");
    req.encodeAndSign(pair.getPrivate());

    final byte[] encodedCert =
            req.getEncodedCert(Certificates.HTTP_ADDRESS);
    final byte[] encodedRootCert = GostCertificateRequest
            .getEncodedRootCert(Certificates.HTTP_ADDRESS);
    final CertificateFactory cf = CertificateFactory.getInstance("X509");
    final Certificate crt =
            cf.generateCertificate(new ByteArrayInputStream(encodedCert));
    final Certificate tr =
            cf.generateCertificate(new ByteArrayInputStream(encodedRootCert));

    //Настройки ocsp
    Security.setProperty("ocsp.enable", "true");
    //тестовый УЦ КриптоПРО
    //корневой сертификат
    Security.setProperty("ocsp.responderCertSubjectName",
            "CN=Test Center CRYPTO-PRO,O=CRYPTO-PRO,C=RU,EMAILADDRESS=info@cryptopro.ru");
    //доступ к сведениям центра сертификации
    Security.setProperty("ocsp.responderURL",
            "http://www.cryptopro.ru/ocspnc/ocsp.srf");

    //Сертификаты (в данном случае корневой и пользователя, выданный УЦ)
    final Certificate[] certs = new Certificate[2];
    certs[0] = crt;
    certs[1] = tr;  //root

    final Set trust = new HashSet(0);
    trust.add(new TrustAnchor((X509Certificate) tr, null));

    final List cert = new ArrayList(0);
    for (int i = 0; i < certs.length; i++)
        cert.add(certs[i]);

    //Параметры
    final PKIXBuilderParameters cpp = new PKIXBuilderParameters(trust, null);
    cpp.setSigProvider(null);
    final CollectionCertStoreParameters par =
            new CollectionCertStoreParameters(cert);
    final CertStore store = CertStore.getInstance("Collection", par);
    cpp.addCertStore(store);
    final X509CertSelector selector = new X509CertSelector();
    selector.setCertificate((X509Certificate) crt);
    cpp.setTargetCertConstraints(selector);

    //Сертификаты (CertPath)
    //1)просто из списка сертификатов (в правильном порядке)
    //final CertificateFactory cf = CertificateFactory.getInstance("X509");
    //final CertPath cp = cf.generateCertPath(cert);

    //2) построение цепочки
    //а) с проверкой crl
    //cpp.setRevocationEnabled(true);
    //для использования расширения сертификата CRL Distribution Points
    //установить System.setProperty("com.sun.security.enableCRLDP", "true");
    //или System.setProperty("com.ibm.security.enableCRLDP", "true");

    //б) без проверки crl
    cpp.setRevocationEnabled(false);
    final PKIXCertPathBuilderResult res =
            (PKIXCertPathBuilderResult) CertPathBuilder.
                    getInstance("CPPKIX", "RevCheck").build(cpp);
    final CertPath cp = res.getCertPath();

    System.out.println(cp);

    //Проверка
    final CertPathValidator cpv = CertPathValidator.getInstance("CPPKIX", "RevCheck");
    cpp.setRevocationEnabled(true);
    cpv.validate(cp, cpp);

    System.out.println("OK");
}
}
