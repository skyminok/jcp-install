/**
 * $RCSfileCRLValidateCert.java,v $
 * version $Revision: 36379 $
 * created 21.02.2018 10:02 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * (C) ООО Крипто-Про 2004-2018.
 * <br>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <br>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.File;
import java.io.FileInputStream;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Пример построения и проверки цепочки сертификатов.
 * Построение выполняется путем загрузки необходимых
 * сертификатов из сети согласно сведениям AIA, при
 * этом задан сертификат проверки и корневой сертификат.
 * Проверка выполняется с помощью CRL DP в сертификатах.
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class CRLValidateCert {

    /**
     * разделитель
     */
    public static final String SEPAR = File.separator;
    /**
     * рабочая директория
     */
    public static String TEST_PATH = System.getProperty("user.dir") + SEPAR + "temp";

    /**
     * Путь к сертификатам.
     */
    private static final String PATH = TEST_PATH+ SEPAR ;

    /**
     * Выполнение примера.
     *
     * @param args Параметры.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        System.setProperty("com.sun.security.enableCRLDP", "true"); // для проверки по CRL DP
        System.setProperty("com.sun.security.enableAIAcaIssuers", "true"); // для загрузки сертификатов по AIA из сети
        System.setProperty("ru.CryptoPro.reprov.enableAIAcaIssuers", "true"); // для загрузки сертификатов по AIA из сети

        JCPInit.initProviders(false);
        final CertificateFactory cf = CertificateFactory.getInstance("X509");
        final Certificate user;

        try (FileInputStream is = new FileInputStream(PATH + "user.cer")) {
            user = cf.generateCertificate(is);
        }

        final Certificate root;
        try (FileInputStream is = new FileInputStream(PATH + "root.cer")) {
            root = cf.generateCertificate(is);
        }

        final Certificate[] certs = new Certificate[2];
        certs[0] = user;
        certs[1] = root;

        final Set<TrustAnchor> trust = new HashSet<TrustAnchor>(1);
        trust.add(new TrustAnchor((X509Certificate) root, null));

        final List cert = new ArrayList(0);
        for (int i = 0; i < certs.length; i++)
            cert.add(certs[i]);

        final PKIXBuilderParameters cpp = new PKIXBuilderParameters(trust, null);
        cpp.setSigProvider(null);

        final CollectionCertStoreParameters par =
            new CollectionCertStoreParameters(cert);

        final CertStore store = CertStore.getInstance("Collection", par);
        cpp.addCertStore(store);

        final X509CertSelector selector = new X509CertSelector();
        selector.setCertificate((X509Certificate) user);

        cpp.setTargetCertConstraints(selector);
        cpp.setRevocationEnabled(false);

        // Построение цепочки.

        final PKIXCertPathBuilderResult res =
            (PKIXCertPathBuilderResult) CertPathBuilder.
                getInstance("CPPKIX", "RevCheck").build(cpp);

        final CertPath cp = res.getCertPath();

        System.out.println("%%% SIZE: " + cp.getCertificates().size());
        System.out.println("%%% PATH:\n" + cp);
        System.out.println("OK-1");

        // Проверка цепочки.

        final CertPathValidator cpv = CertPathValidator.getInstance("CPPKIX", "RevCheck");
        cpp.setRevocationEnabled(true);

        cpv.validate(cp, cpp);
        System.out.println("OK-2");

    }

}
