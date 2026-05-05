package app;

import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.stream.Stream;

public class JCSP {

    public static void main(String[] args) throws Exception {
        Helper.initJcsp();

        Stream.of(Security.getProviders())
                .forEach(System.out::println);

        KeyStore ks = KeyStore.getInstance("REGISTRY", "JCSP");
        ks.load(null, null);
        Enumeration<String> aliases = ks.aliases();

        while (aliases.hasMoreElements()) {
            Certificate cert = ks.getCertificate(aliases.nextElement());
            if (cert == null) {
                continue;
            }
            if (!(cert instanceof X509Certificate)) {
                continue;
            }
            X509Certificate curCert = (X509Certificate) cert;
            System.out.println(curCert.getSubjectX500Principal());
        }

    }
}
