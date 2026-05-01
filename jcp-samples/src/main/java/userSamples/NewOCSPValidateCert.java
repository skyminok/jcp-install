/**
 * $RCSfileNewOCSPValidateCert.java,v $ version $Revision$ created 06.11.2020
 * 18:27 by afevma last modified $Date$ by $Author$ (C) ООО Крипто-Про 2004-2020.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен для целей обучения.
 * Может быть скопирован или модифицирован при условии сохранения абзацев с
 * указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован для защиты информации.
 * Компания Крипто-Про не несет никакой ответственности за функционирование
 * этого кода.
 */
package userSamples;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;

import ru.CryptoPro.JCP.tools.AlgorithmTools;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Пример построения цепочки сертификатов и проверки
 * их статусов с использованием OCSP службы.
 *
 * Java 10+
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class NewOCSPValidateCert {

    // Адрес тестового УЦ.
    private static final String CA_ADDRESS = Certificates.HTTP_ADDRESS;

    // Адрес OCSP службы.
    private static final String OCSP_ADDRESS = "http://testca.cryptopro.ru/ocsp/ocsp.srf";

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Добавление провайдеров.

        JCPInit.initProviders(false); // JCP - провайдер по умолчанию.

        // Генерация ключей на алгоритме ГОСТ 2012 (256).

        final KeyPairGenerator kg = KeyPairGenerator
            .getInstance(JCP.GOST_EL_2012_256_NAME);

        final KeyPair pair = kg.generateKeyPair();

        // Создание запроса и обращение в тестовый УЦ
        // {@link #CA_ADDRESS} за сертификатом.

        final GostCertificateRequest req = new GostCertificateRequest();
        req.init(JCP.GOST_EL_2012_256_NAME, false);

        req.setPublicKeyInfo(pair.getPublic());
        req.setSubjectInfo("CN=TEST_OCSP"); // имя тестового сертификата

        String signAlg = AlgorithmTools.getSignatureAlgorithmByPrivateKey(pair.getPrivate());
        req.encodeAndSign(pair.getPrivate(), signAlg);

        // Цепочка из двух сертификатов.

        final byte[] encodedCert = req.getEncodedCert(CA_ADDRESS); // выпуск тестового сертификата

        final byte[] encodedRootCert = GostCertificateRequest
            .getEncodedRootCert(CA_ADDRESS); // получение корневого сертификата

        final CertificateFactory factory = CertificateFactory
            .getInstance("X509");

        final Certificate client = factory.generateCertificate(
            new ByteArrayInputStream(encodedCert));

        final Certificate root = factory.generateCertificate(
            new ByteArrayInputStream(encodedRootCert));

        // Выстраивание тестовой цепочки. В данном примере
        // она известна заранее, на самом деле, часто это
        // не так.

        final Certificate[] certs = new Certificate[2];
        certs[0] = client;
        certs[1] = root;

        final Set<TrustAnchor> trust = new HashSet<TrustAnchor>(0);
        trust.add(new TrustAnchor((X509Certificate) root, null));

        final List<Certificate> cert = new ArrayList<Certificate>(0);
        cert.addAll(Arrays.asList(certs));

        // Построение цепочки сертификатов.

        final PKIXBuilderParameters cpp = new PKIXBuilderParameters(trust, null);
        cpp.setSigProvider(null);

        final CollectionCertStoreParameters par =
            new CollectionCertStoreParameters(cert);

        final CertStore store = CertStore.getInstance("Collection", par);
        cpp.addCertStore(store);

        final X509CertSelector selector = new X509CertSelector();

        selector.setCertificate((X509Certificate) client);
        cpp.setTargetCertConstraints(selector);
        cpp.setRevocationEnabled(false); // проверка статуса сертификата отключена при построении

        CertPathBuilder builder = CertPathBuilder
            .getInstance("CPPKIX", "RevCheck");

        final PKIXCertPathBuilderResult res =
            (PKIXCertPathBuilderResult)builder.build(cpp);

        final CertPath cp = res.getCertPath(); // цепочка сертификатов
        System.out.println(cp);

        final CertPathValidator DEFAULT = CertPathValidator
            .getInstance("PKIX"); // данный алгоритм имеет реализацию OCSP revocation checker

        CertPathChecker cpc = DEFAULT.getRevocationChecker();
        PKIXRevocationChecker prc = (PKIXRevocationChecker)cpc;

        prc.init(false);

        // Адрес OCSP службы согласно адресу УЦ. Его можно
        // опустить, если в сертификате есть AIA с адресом
        // службы.
        //
        // URI uri = new URI(OCSP_ADDRESS);
        // prc.setOcspResponder(uri);

        // Параметры проверки цепочки.
        //
        // Set<PKIXRevocationChecker.Option> options = EnumSet.
        //    of(PKIXRevocationChecker.Option.NO_FALLBACK);
        //
        // prc.setOptions(options);

        final CertPathValidator validator = CertPathValidator
            .getInstance("CPPKIX", "RevCheck"); // провайдер проверки, не имеет OCSP revocation checker

        // prc.setOcspResponderCert(cert);
        cpp.addCertPathChecker(prc); // задаем провайдеру проверки OCSP revocation checker

        validator.validate(cp, cpp);
        System.out.println("OK");

    }


}
