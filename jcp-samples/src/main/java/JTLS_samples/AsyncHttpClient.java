/**
 * $RCSfileAsyncHttpClient.java,v $ version $Revision$ created 22.07.2020 18:58
 * by afevma last modified $Date$ by $Author$ (C) ООО Крипто-Про 2004-2020.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен для целей обучения.
 * Может быть скопирован или модифицирован при условии сохранения абзацев с
 * указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован для защиты информации.
 * Компания Крипто-Про не несет никакой ответственности за функционирование
 * этого кода.
 */
package JTLS_samples;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.reprov.RevCheck;
import ru.CryptoPro.ssl.Provider;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.Security;
import java.util.concurrent.Future;

/**
 * Пример подключения с использованием SSLEngine.
 * Используется:
 *
 * org.apache.httpcomponents:httpclient:4.5.8
 * org.apache.httpcomponents:httpasyncclient:4.1.4
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class AsyncHttpClient {

    /**
     * Создание подключения.
     *
     * @param url Адрес подключения.
     * @param ctx Защищенный контекст.
     * @throws Exception
     */
    public static void connect(URL url, SSLContext ctx) throws Exception {

        HttpHost target = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        HttpGet httpGet = new HttpGet(url.getPath());

        CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom()
            .setSSLHostnameVerifier(new AllowAllHostnameVerifier())
            .setSSLContext(ctx).build();

        try {

            httpClient.start();

            Future<HttpResponse> future = httpClient.execute(target, httpGet, null);
            HttpResponse response = future.get();

            try {

                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {

                    System.out.println(response.getStatusLine());
                    System.out.println(EntityUtils.toString(response.getEntity()));

                } // if
                else {
                    throw new Exception("Invalid status code: " + statusCode);
                } // else

            } finally {
                EntityUtils.consume(response.getEntity());
            }

        } finally {
            httpClient.close();
        }

    }

    /**
     * Создание защищенного контекста для подключения
     * без клиентской аутентификации.
     *
     * @param trustStorePath Путь к хранилищу доверенных сертификатов.
     * @param trustStoreType Тип хранилища доверенных сертификатов.
     * @param trustStorePassword Пароль хранилища доверенных сертификатов.
     * @return защищенный контекст.
     * @throws Exception
     */
    public static SSLContext createContext(String trustStorePath,
        String trustStoreType, char[] trustStorePassword) throws Exception {

        KeyStore trustStore = KeyStore.getInstance(trustStoreType);
        trustStore.load(new FileInputStream(trustStorePath), trustStorePassword);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("GostX509");
        tmf.init(trustStore);

        final SSLContext ctx = SSLContext.getInstance("GostTLS");
        ctx.init(null, tmf.getTrustManagers(), null);

        return ctx;

    }

    /**
     * Пример.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // CRL для проверки серверной цепочки будут
        // загружены из CRL DP.

        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        Security.addProvider(new JCP());
        Security.addProvider(new CryptoProvider());
        Security.addProvider(new RevCheck());
        Security.addProvider(new Provider());

        // Создание защищенного контекста для подключения
        // без клиентской аутентификации.

        SSLContext ctx = createContext(
            "C:/Projects/trust.store",
            JCP.CERT_STORE_NAME,
            "11111111".toCharArray()
        );

        // Подключение без клиентской аутентификации.

        connect(new URL("https://build-jcp:8443/index.html"), ctx);

    }

}
