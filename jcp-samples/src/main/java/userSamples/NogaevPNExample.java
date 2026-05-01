/**
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.*;
import java.util.*;

/**
 * http://cryptopro.ru/forum2/Default.aspx?g=posts&t=4905
 */
public class NogaevPNExample {

    public static void main(String[] args) throws Exception {

        Security.addProvider(new BouncyCastleProvider());
        JCPInit.initProviders(false);
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        // уникальное имя корневого сертификата
        final String aliasRootCert = "rootCert";
        // уникальное имя промежуточного сертификата
        final String aliasInterCert = "intermediateCert";
        // уникальное имя сертификата открытого ключа
        final String aliasEndCert = "endCert";

        //инициализация хранилища доверенных сертификатов и ключевого носителя
        final KeyStore keyStore = KeyStore.getInstance("HDImageStore");

        // загрузка содержимого хранилища (предполагается, что хранилище,
        // проинициализированное именем STORE_TYPE существует) и содержимого
        // ключевого носителя
        try (FileInputStream is = new FileInputStream("C:/TESTS/User/NogaevPN/certstore1")) {
            keyStore.load(is, "pass123".toCharArray());
        }

        // чтение корневого сертификата из хранилища доверенных сертификатов
        // (предполагается, что такой сертификат существует в хранилище)
        final Certificate certRoot = keyStore.getCertificate(aliasRootCert);

        // чтение промежуточного сертификата из хранилища доверенных сертификатов
        // (предполагается, что такой сертификат существует в хранилище)
        final Certificate certInter = keyStore.getCertificate(aliasInterCert);

        // чтение конечного сертификата (сертификата открытого ключа) с носителя
        // (предполагается, что сертификат такой сертификат существует на носителе)
        final Certificate certEnd = keyStore.getCertificate(aliasEndCert);

        //Построение цепочки из прочитанных сертификатов, начиная с корневого сертификата
        //(с именем aliasRootCert) и заканчивая сертификатом открытого ключа (c именем aliasEndCert)

        // определение списка сертификатов, из которых
        // осуществляется построение цепочки
        final List<Certificate> certs = new ArrayList<Certificate>(3);
        //certs.add(certRoot);
        certs.add(certInter);
        certs.add(certEnd);

        // определение корневого сертификата (с которого начинается построение
        // цепочки)
        final TrustAnchor anchor =
                new TrustAnchor((X509Certificate) certRoot, null);

        // определение параметров специального хранилища
        // сертификатов, в которое записываются все используемые
        // в построении цепочки сертификаты
        final CollectionCertStoreParameters par =
                new CollectionCertStoreParameters(certs);

        // создание специального хранилища сертификатов на основе
        // параметров, определенных списком сертификатов
        final CertStore store = CertStore.getInstance("Collection", par);

        // инициализация объекта построения цепочки сертификатов
        final CertPathBuilder cpb = CertPathBuilder.getInstance("PKIX");
        //или для совместимости с КриптоПро УЦ
        //CertPathBuilder cpb = CertPathBuilder.getInstance("CPPKIX", "RevCheck");

        // инициализация параметров построения цепочки сертификатов
        PKIXBuilderParameters params = new PKIXBuilderParameters(
                Collections.singleton(anchor), new X509CertSelector());

        params.setSigProvider(JCP.PROVIDER_NAME);
        params.setRevocationEnabled(false);

        // добавление к параметрам сертификатов, из которых
        // будет строиться цепочка
        params.addCertStore(store);

        // инициализация объекта выборки сертификата, которым
        // заканчивается построение цепочки
        final X509CertSelector selector = new X509CertSelector();

        // определение сертификата, которым
        // заканчивается построение цепочки
        selector.setCertificate((X509Certificate) certEnd);

        params.setTargetCertConstraints(selector);

        // построение цепочки сертификатов
        final PKIXCertPathBuilderResult res =
                (PKIXCertPathBuilderResult) cpb.build(params);

        params = new PKIXBuilderParameters(
                Collections.singleton(anchor), new X509CertSelector());

        // Проверка построенной цепочки сертификатов
        params.addCertStore(store);
        params.setSigProvider("JCP");
        params.setRevocationEnabled(true);
        //Security.setProperty("ocsp.enable", "true");
        params.setTargetCertConstraints(selector);

        // инициализация объекта проверки цепочки сертификатов
        final CertPathValidator validator = CertPathValidator.getInstance("PKIX");
        //или для совместимости с КриптоПро УЦ
        //CertPathValidator validator = CertPathValidator.getInstance("CPPKIX", "RevCheck");

        // проверка цепочки сертификатов
        final CertPathValidatorResult val_res =
                validator.validate(res.getCertPath(), params);

        // вывод результата проверки в строком виде
        System.out.println(val_res.toString());

    }
}
