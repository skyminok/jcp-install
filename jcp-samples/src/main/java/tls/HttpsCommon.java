package tls;

import JTLS_samples.connection.SSLConfiguration;
import ru.CryptoPro.JCP.KeyStore.StoreInputStream;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.*;
import java.util.HashMap;
import java.util.Map;

public class HttpsCommon {

    public static final int PKIX_DEFAULT = 0;
    public static final int PKIX_NO_REVOCATION_CHECK = 1; // 1, если нужно отключить только проверку статуса сертификата другой стороны
    public static final int PKIX_TRUST_ALL = 2; // 2, если вообще не нужно строить и проверять цепочку противоположной стороны

    public static final String PARAM_prov_names = "prov_names"; // список имен провайдеров для добавления в java.security, порядок важен
    public static final String PARAM_prov_classes = "prov_classes"; // список классов провайдеров для их регистрации
    public static final String PARAM_prov_method = "prov_method"; // способ регистрации провайдеров
    public static final String PARAM_trust_man_prov = "trust_man_prov"; // провайдер TrustManagerFactory
    public static final String PARAM_trust_man_alg = "trust_man_alg"; // алгоритм TrustManagerFactory
    public static final String PARAM_truststore_prov = "truststore_prov"; // провайдер хранилища доверенных корневых сертификатов (KeyStore)
    public static final String PARAM_truststore_type = "truststore_type"; // тип хранилища доверенных корневых сертификатов (KeyStore)
    public static final String PARAM_truststore = "truststore"; // путь к хранилищу доверенных корневых сертификатов (KeyStore)
    public static final String PARAM_truststore_pass = "truststore_pass"; // пароль к хранилищу доверенных корневых сертификатов (KeyStore)
    public static final String PARAM_key_man_prov = "key_man_prov"; // провайдер KeyManagerFactory
    public static final String PARAM_key_man_alg = "key_man_alg"; // алгоритм KeyManagerFactory
    public static final String PARAM_keystore_prov = "keystore_prov"; // провайдер ключевого контейнера (KeyStore)
    public static final String PARAM_keystore_type = "keystore_type"; // тип ключевого контейнера (KeyStore)
    public static final String PARAM_keystore_alias = "keystore_alias"; // имя ключевого контейнера (можно не задавать) (KeyStore)
    public static final String PARAM_keystore_pass = "keystore_pass"; // пароль к ключевому контейнеру (можно не задавать) (KeyStore)
    public static final String PARAM_client_auth = "client_auth"; // true, если на сервере включена клиентская аутентификация
    public static final String PARAM_no_check = "no_check"; // {@link #PKIX_DEFAULT}, {@link #PKIX_NO_REVOCATION_CHECK}, {@link #PKIX_TRUST_ALL}
    public static final String PARAM_tls_provider = "tls_provider"; // провайдер SSLContext
    public static final String PARAM_tls_protocol = "tls_protocol"; // алгоритм SSLContext
    public static final String PARAM_tls_cipher_suite = "tls_cipher_suite"; // выбранная сюита SSLContext (можно не задавать)

    protected static final String PARAM_MARK = "--"; // признак параметра
    protected static final String PARAM_KEY_VALUE_SEPARATOR = "="; // разделитель ключ=значение
    protected static final String PARAM_VALUES_SEPARATOR = ","; // разделитель для {@link #PARAM_prov_names} и {@link #PARAM_prov_classes}
    protected static final String HTTP_SEPARATOR = "\r\n\r\n"; // разделитель http-заголовков

    protected static final Map<String, String> parameters = new HashMap<>();

    static {
        clearParameters();
    }

    protected static void clearParameters() {
        parameters.put(PARAM_prov_names, "");
        parameters.put(PARAM_prov_classes, "");
        parameters.put(PARAM_prov_method, "");
        parameters.put(PARAM_trust_man_prov, "");
        parameters.put(PARAM_trust_man_alg, "");
        parameters.put(PARAM_truststore_prov, "");
        parameters.put(PARAM_truststore_type, "");
        parameters.put(PARAM_truststore, "");
        parameters.put(PARAM_truststore_pass, "");
        parameters.put(PARAM_key_man_prov, "");
        parameters.put(PARAM_key_man_alg, "");
        parameters.put(PARAM_keystore_prov, "");
        parameters.put(PARAM_keystore_type, "");
        parameters.put(PARAM_keystore_alias, "");
        parameters.put(PARAM_keystore_pass, "");
        parameters.put(PARAM_client_auth, "false");
        parameters.put(PARAM_no_check, String.valueOf(PKIX_DEFAULT));
        parameters.put(PARAM_tls_provider, "");
        parameters.put(PARAM_tls_protocol, "");
        parameters.put(PARAM_tls_cipher_suite, "");
    }

    protected static String strOrNull(String key) {
        String value = parameters.get(key);
        return value != null && !value.isEmpty() ? value : null;
    }

    protected static String str(String key) {
        String value = parameters.get(key);
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Argument " + key + " must have value.");
        }
        return value;
    }

    protected static boolean bool(String key) {
        return Boolean.parseBoolean(strOrNull(key));
    }

    protected static int number(String key) {
        return Integer.parseInt(str(key));
    }

    protected static void registerProviders() {
        String providerNames = strOrNull(PARAM_prov_names);
        String providerClasses = strOrNull(PARAM_prov_classes);
        String providerMethod = strOrNull(PARAM_prov_method);
        if (providerNames != null) {
            if (providerClasses == null) {
                throw new RuntimeException("Provider classes must be set for " + providerNames);
            }
            String[] providers = providerNames.split(PARAM_VALUES_SEPARATOR);
            String[] classes = providerClasses.split(PARAM_VALUES_SEPARATOR);
            if (providers.length != classes.length) {
                throw new RuntimeException("Invalid count of provider classes: " + providerClasses);
            }
            for (int i = 0; i < providers.length; i++) {
                String providerName = providers[i];
                String providerClazz = classes[i];
                try {
                    Security.removeProvider(providerName);
                } catch (Throwable t) {}
                Provider providerObject;
                try {
                    Class providerClass = Class.forName(providerClazz, false, HttpsCommon.class.getClassLoader());
                    providerObject = (Provider) providerClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Provider " + providerName + " not instantiated.", e);
                }
                // Для тестов с BC TLS важно, чтобы была вставка, а не добавление, так как BC будет обращаться
                // к провайдерам без указания их имен, поэтому Java CSP должен быть первым провайдером (до Sun или BC).
                if (providerMethod == null || providerMethod.equalsIgnoreCase("insert")) {
                    int position = Security.insertProviderAt(providerObject, i + 1);
                    if (position != i + 1) {
                        throw new RuntimeException("Invalid provider position #" + position + " of " + providerName);
                    }
                }
                // В случае Java 8 нет тестов с BC TLS, провайдеры уже установлены, поэтому используется
                // добавление в конец.
                else if (providerMethod.equalsIgnoreCase("add")) {
                    Security.addProvider(providerObject);
                }
                else {
                    throw new RuntimeException("Unknown method "+ providerMethod + " of provider registering.");
                }
            }
        }
    }

    protected static KeyStore getTrustStore() throws Exception {
        KeyStore keyStore = strOrNull(PARAM_truststore_prov) != null
            ? KeyStore.getInstance(str(PARAM_truststore_type), str(PARAM_truststore_prov))
            : KeyStore.getInstance(str(PARAM_truststore_type));
        char[] password = (strOrNull(PARAM_truststore_pass) != null)
            ? str(PARAM_truststore_pass).toCharArray()
            : null;
        try (FileInputStream is = new FileInputStream(str(PARAM_truststore))) {
            keyStore.load(is, password);
        }
        return keyStore;
    }

    protected static KeyStore getKeyStore() throws Exception {
        KeyStore keyStore = strOrNull(PARAM_keystore_prov) != null
            ? KeyStore.getInstance(str(PARAM_keystore_type), str(PARAM_keystore_prov))
            : KeyStore.getInstance(str(PARAM_keystore_type));
        if (strOrNull(PARAM_keystore_alias) != null) {
            keyStore.load(new StoreInputStream(str(PARAM_keystore_alias)), null);
        }
        else {
            keyStore.load(null, null);
        }
        return keyStore;
    }

    protected static SSLContext createContext(boolean isServer) throws Exception {
        TrustManager[] trustManagers = null;
        if (number(PARAM_no_check) == PKIX_TRUST_ALL) {
            trustManagers = new TrustManager[] {SSLConfiguration.TRUST_MANAGER_ALL};
        }
        else {
            TrustManagerFactory trustManagerFactory = strOrNull(PARAM_trust_man_prov) != null
                ? TrustManagerFactory.getInstance(str(PARAM_trust_man_alg), str(PARAM_trust_man_prov))
                : TrustManagerFactory.getInstance(str(PARAM_trust_man_alg));
            KeyStore trustStore = getTrustStore();
            PKIXParameters pkixParams = new PKIXBuilderParameters(trustStore, new X509CertSelector());
            boolean revocationDisabled = number(PARAM_no_check) > PKIX_DEFAULT;
            pkixParams.setRevocationEnabled(!revocationDisabled);
            trustManagerFactory.init(new CertPathTrustManagerParameters(pkixParams));
            trustManagers = trustManagerFactory.getTrustManagers();
        }
        KeyManagerFactory keyManagerFactory = null;
        if (bool(PARAM_client_auth) || isServer) {
            keyManagerFactory = strOrNull(PARAM_key_man_prov) != null
                ? KeyManagerFactory.getInstance(str(PARAM_key_man_alg), str(PARAM_key_man_prov))
                : KeyManagerFactory.getInstance(str(PARAM_key_man_alg));
            char[] password = (strOrNull(PARAM_keystore_pass) != null)
                ? str(PARAM_keystore_pass).toCharArray()
                : null;
            KeyStore keyStore = getKeyStore();
            keyManagerFactory.init(keyStore, password);
        }
        SSLContext context = strOrNull(PARAM_tls_provider) != null
            ? SSLContext.getInstance(str(PARAM_tls_protocol), str(PARAM_tls_provider))
            : SSLContext.getInstance(str(PARAM_tls_protocol));
        if (keyManagerFactory != null) {
            context.init(keyManagerFactory.getKeyManagers(), trustManagers, null);
        }
        else {
            context.init(null, trustManagers, null);
        }
        return context;
    }

    protected static void parseArguments(String[] args) {
        if (args.length == 0) {
            throw new RuntimeException("No arguments found.");
        }
        for (String arg : args) {
            if (!arg.startsWith(PARAM_MARK)) {
                throw new RuntimeException("Invalid argument " + arg);
            }
            arg = arg.substring(2);
            String parameter;
            String value;
            int separatorPos = arg.indexOf(PARAM_KEY_VALUE_SEPARATOR);
            if (separatorPos == -1) {
                parameter = arg;
                value = "true";
            }
            else {
                parameter = arg.substring(0, separatorPos);
                value = arg.substring(separatorPos + PARAM_KEY_VALUE_SEPARATOR.length());
            }
            if (parameters.get(parameter) == null) {
                throw new RuntimeException("Unknown argument " + parameter);
            }
            parameters.put(parameter, value);
        }
    }

    protected static void setSSLParameters(SSLSocket sslSocket) {
        if (strOrNull(PARAM_tls_protocol) != null) {
            sslSocket.setEnabledProtocols(new String[]{str(PARAM_tls_protocol)});
        }
        if (strOrNull(PARAM_tls_cipher_suite) != null) {
            sslSocket.setEnabledCipherSuites(new String[]{str(PARAM_tls_cipher_suite)});
        }
    }

    protected static void setSSLParameters(SSLServerSocket sslSocket) {
        if (strOrNull(PARAM_tls_protocol) != null) {
            sslSocket.setEnabledProtocols(new String[]{str(PARAM_tls_protocol)});
        }
        if (strOrNull(PARAM_tls_cipher_suite) != null) {
            sslSocket.setEnabledCipherSuites(new String[]{str(PARAM_tls_cipher_suite)});
        }
    }

    protected static void fromTo(InputStream inputStream, OutputStream outputStream) throws Exception {
        final int bufferSize = 4 * 1024 * 1024;
        byte[] buffer = new byte[bufferSize];
        int read;
        while ((read = inputStream.read(buffer, 0, bufferSize)) >= 0) {
            if (outputStream != null) {
                outputStream.write(buffer, 0, read);
            }
        }
    }

    static class ConnectionParameters {
        int responseCode;
        String cipherSuite;
    }

    /**
     * Менеджер сертификатов для случаев, когда проверка сертификата сервера отключена.
     *
     */
    public static final TrustManager TRUST_MANAGER_ALL = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}
        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {}
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };
}
