package app;

import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.stream.Stream;

public class JCP {

    public static void main(String[] args) throws Exception {
        Helper.initJcp();

        Stream.of(Security.getProviders())
                .forEach(System.out::println);

        Security.getProvider("JCP").getServices().forEach(System.out::println);

        KeyStore ks = KeyStore.getInstance("HDImageStore", "JCP");
        ks.load(null, null);
        Enumeration<String> aliases = ks.aliases();

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            Certificate cert = ks.getCertificate(alias);
            if (cert == null) {
                continue;
            }
            if (!(cert instanceof X509Certificate)) {
                continue;
            }
            X509Certificate curCert = (X509Certificate) cert;
            System.out.println(alias + "=" + curCert.getSubjectX500Principal());
        }

    }
}
