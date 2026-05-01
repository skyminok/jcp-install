/**
 * $RCSfile$
 * version $Revision$
 * created 03.07.2007 10:07:20 by kunina
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2007.
 * <p/>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p/>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JTLS_samples;

import okhttp3.*;

import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.reprov.RevCheck;

import ru.CryptoPro.ssl.Provider;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.ssl.util.TLSContext;
import ru.CryptoPro.ssl.Provider;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.Collections;

/**
 * Пример односторонней аутентификации TLS клиента.
 * В примере используется класс OkHttpClient.
 *
 * Для работы примера нужны библиотеки:
 * kotlin-stdlib-1.3.72.jar
 * okhttp-4.8.0.jar
 * okio-2.7.0.jar
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class OkHTTPClientSample {

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Security.addProvider(new JCP());
        Security.addProvider(new CryptoProvider());
        Security.addProvider(new RevCheck());
        Security.addProvider(new Provider());

        String trustStorePath = "C:/Projects/trust-1.store";
        String trustStorePassword = "1";
        String urlPath = "https://testca.cryptopro.ru/certsrv/certcarc.asp";

        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        TrustManager[] trustManagers = new TrustManager[1];

        // Загрузка хранилища доверенных сертификатов. Инициализация
        // на основе его TrustManagerFactory. Создание и инициализация
        // контекста SSLContext.

        SSLContext ctx = TLSContext.initClientSSL(
            Provider.PROVIDER_NAME,
            "TLSv1.2",
            JCP.PROVIDER_NAME,
            JCP.CERT_STORE_NAME,
            trustStorePath,
            trustStorePassword,
            trustManagers
        );

        SSLSocketFactory factory = ctx.getSocketFactory();
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
        connect(urlPath, factory, trustManager);

    }

    /**
     * Функция устанавливает подключение по заданному адресу.
     *
     * @param urlPath Адрес для подключения.
     * @param factory Фабрика сокетов.
     * @param trustManager Менеджер хранилища доверенных
     * сертификатов.
     * @throws Exception
     */
    public static void connect(String urlPath, SSLSocketFactory
        factory, X509TrustManager trustManager) throws Exception {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        /*
        // Класс фабрики переобпределяет стандартную фабрику,
        // чтобы задать SSLParameters.
        class MySSLSocketFactory extends SSLSocketFactory {

            private final SSLSocketFactory delegate;

            public MySSLSocketFactory(SSLSocketFactory delegate) {
                this.delegate = delegate;
            }

            //
            // Настройка end-point-identification-algorithm.
            //
            // @param socket Сокет.
            // @return сокет.
            //
            protected Socket prepare(Socket socket) {
                if (socket instanceof SSLSocket) {
                    SSLParameters socketParams = ((SSLSocket)socket).getSSLParameters();
                    socketParams.setEndpointIdentificationAlgorithm("https");
                    ((SSLSocket)socket).setSSLParameters(socketParams);
                }
                return socket;
            }

            @Override
            public String[] getDefaultCipherSuites() {
                return delegate.getDefaultCipherSuites();
            }

            @Override
            public String[] getSupportedCipherSuites() {
                return delegate.getSupportedCipherSuites();
            }

            @Override
            public Socket createSocket(Socket socket, String s,
                int i, boolean b) throws IOException {
                return prepare(delegate.createSocket(socket, s, i, b));
            }

            @Override
            public Socket createSocket(String s, int i) throws
                IOException, UnknownHostException {
                return prepare(delegate.createSocket(s, i));
            }

            @Override
            public Socket createSocket(String s, int i, InetAddress
                inetAddress, int i1) throws IOException, UnknownHostException {
                return prepare(delegate.createSocket(s, i, inetAddress, i1));
            }

            @Override
            public Socket createSocket(InetAddress inetAddress,
                int i) throws IOException {
                return prepare(delegate.createSocket(inetAddress, i));
            }

            @Override
            public Socket createSocket(InetAddress inetAddress, int i,
                InetAddress inetAddress1, int i1) throws IOException {
                return prepare(delegate.createSocket(inetAddress, i, inetAddress1, i1));
            }

        }

        // Установка нужного SSLSocketFactory.
        builder.sslSocketFactory(new MySSLSocketFactory(factory), trustManager);
        */

        builder.sslSocketFactory(factory, trustManager);

        // Задание необходимых параметров (сюиты, протокол).
        ConnectionSpec spec = new ConnectionSpec.Builder(
            ConnectionSpec.MODERN_TLS)
            .tlsVersions("TLSv1.2", "TLSv1.1", "TLSv1")
            .cipherSuites(
                    "TLS_CIPHER_2012",
                    "TLS_CIPHER_2001")
            .build();

        builder.connectionSpecs(Collections.singletonList(spec));
        OkHttpClient client = builder.build();

        // Создание запроса к нужному адресу.
        Request request = new Request.Builder()
            .url(urlPath)
            .build();

        // Обращение к серверу.
        Response response = client.newCall(request).execute();

        // Вывод полученного ответа.
        ClientSample.printStream(response.body().byteStream());

    }

}
