package tomcat;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.*;
import java.util.*;

/**
 * Класс TrustManager для tomcat, использующий CRL с диска.
 *
 */
public class Tomcat8TrustManager extends X509ExtendedTrustManager implements X509TrustManager {

    private static class ConfigProperty {
        private final String name;
        private final String value;
        private ConfigProperty(String name) {
            this.name = name;
            this.value = System.getProperty(name);
        }
    }

    /**
     * set JAVA_OPTS="-Dtomcat8-trust-manager-config=trust-manager.xml"
     *
     * <?xml version="1.0" encoding="utf-8" ?>
     * <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
     * <properties>
     *     <entry key="tls-provider">JTLS</entry>
     *     <entry key="trust-store-algorithm">GostX509</entry>
     *     <entry key="trust-store-provider">JCP</entry>
     *     <entry key="trust-store-type">CertStore</entry>
     *     <entry key="trust-store-file">store</entry>
     *     <entry key="trust-store-password">1</entry>
     *     <entry key="crl-list-file">crl.pem</entry>
     * </properties>
     */

    private static final String configOption = "tomcat8-trust-manager-config";

    /**
     * set "JAVA_OPTS=%JAVA_OPTS% -Dtls-provider=JTLS"
     * set "JAVA_OPTS=%JAVA_OPTS% -Dtrust-store-algorithm=GostX509"
     * set "JAVA_OPTS=%JAVA_OPTS% -Dtrust-store-provider=JCP"
     * set "JAVA_OPTS=%JAVA_OPTS% -Dtrust-store-type=CertStore"
     * set "JAVA_OPTS=%JAVA_OPTS% -Dtrust-store-file=store"
     * set "JAVA_OPTS=%JAVA_OPTS% -Dtrust-store-password=1"
     * set "JAVA_OPTS=%JAVA_OPTS% -Dcrl-list-file=crl.pem"
     */

    private static final String tlsProvider = "tls-provider";
    private static final String trustStoreAlgorithm = "trust-store-algorithm";
    private static final String trustStoreProvider = "trust-store-provider";
    private static final String trustStoreType = "trust-store-type";
    private static final String trustStorePath = "trust-store-file";
    private static final String trustStorePassword = "trust-store-password";
    private static final String crlListPath = "crl-list-file";

    private final X509ExtendedTrustManager delegate;

    private static final List<ConfigProperty> configProperties = Arrays.asList(
        new ConfigProperty(tlsProvider),
        new ConfigProperty(trustStoreAlgorithm),
        new ConfigProperty(trustStoreProvider),
        new ConfigProperty(trustStoreType),
        new ConfigProperty(trustStorePath),
        new ConfigProperty(trustStorePassword),
        new ConfigProperty(crlListPath)
    );

    public Tomcat8TrustManager() {
        try {
            String config = System.getProperty(configOption);
            Properties properties;
            if (config != null) {
                properties = initConfig(config);
            }
            else {
                properties = new Properties();
                for (ConfigProperty configProperty : configProperties) {
                    if (configProperty.value != null) {
                        properties.setProperty(configProperty.name, configProperty.value);
                    }
                }
            }
            TrustManagerFactory tmf = initTrustManagerFactory(properties);
            delegate = (X509ExtendedTrustManager) tmf.getTrustManagers()[0];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Properties initConfig(String config) throws Exception {
        System.out.println("Config is " + config);
        Properties properties = new Properties();
        try (FileInputStream is = new FileInputStream(config)) {
            properties.loadFromXML(is);
        }
        properties.list(System.out);
        return properties;
    }

    private TrustManagerFactory initTrustManagerFactory(Properties properties) throws Exception {
        String tlsProviderValue = properties.getProperty(tlsProvider);
        String trustStoreAlgorithmValue = properties.getProperty(trustStoreAlgorithm);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(trustStoreAlgorithmValue, tlsProviderValue);
        tmf.init(new CertPathTrustManagerParameters(initParameters(properties, initTrustStore(properties))));
        return tmf;
    }

    private KeyStore initTrustStore(Properties properties) throws Exception {
        String trustStoreTypeValue = properties.getProperty(trustStoreType);
        String trustStoreProviderValue = properties.getProperty(trustStoreProvider);
        String trustStorePathValue = properties.getProperty(trustStorePath);
        String trustStorePasswordValue = properties.getProperty(trustStorePassword);
        KeyStore trustStore = KeyStore.getInstance(trustStoreTypeValue, trustStoreProviderValue);
        try (FileInputStream is = new FileInputStream(trustStorePathValue)) {
            trustStore.load(is, trustStorePasswordValue.toCharArray());
        }
        return trustStore;
    }

    private PKIXParameters initParameters(Properties properties, KeyStore trustStore) throws Exception {
        String crlListPathValue = properties.getProperty(crlListPath);
        PKIXParameters pkixParams = new PKIXBuilderParameters(trustStore, new X509CertSelector());
        pkixParams.setRevocationEnabled(true);
        List<CertStore> certStores = new ArrayList<>(1);
        try (FileInputStream is = new FileInputStream(crlListPathValue)) {
            Collection<? extends CRL> cRLs = CertificateFactory.getInstance("X.509").generateCRLs(is);
            certStores.add(CertStore.getInstance("Collection", new CollectionCertStoreParameters(cRLs)));
        }
        pkixParams.setCertStores(certStores);
        return pkixParams;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        delegate.checkClientTrusted(x509Certificates, s, socket);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
        delegate.checkServerTrusted(x509Certificates, s, socket);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
        delegate.checkClientTrusted(x509Certificates, s, sslEngine);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
        delegate.checkServerTrusted(x509Certificates, s, sslEngine);
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        delegate.checkServerTrusted(x509Certificates, s);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        delegate.checkServerTrusted(x509Certificates, s);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return delegate.getAcceptedIssuers();
    }

}
