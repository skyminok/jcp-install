/**
 * $RCSfileDSSConnection.java,v $ version $Revision: 36379 $ created 28.09.2018
 * 16:44 by afevma last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012)
 * $ by $Author: afevma $ (C) ООО Крипто-Про 2004-2018.
 * <p/>
 * Программный код, содержащийся в этом файле, предназначен для целей обучения.
 * Может быть скопирован или модифицирован при условии сохранения абзацев с
 * указанием авторства и прав.
 * <p/>
 * Данный код не может быть непосредственно использован для защиты информации.
 * Компания Крипто-Про не несет никакой ответственности за функционирование
 * этого кода.
 */
package JTLS_samples;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.ssl.Provider;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;

/**
 * Пример подключения к серверу DSS с использованием
 * клиентского контейнера.
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class DSSConnection {

    /**
     * Адрес подключения. d1831dea-985f-4df1-a54b-2497eeace2f2
     */
    private static final String URL =
            //"https://stenddss.cryptopro.ru:4430/opensmeidp/ums/user/d1831dea-985f-4df1-a54b-2497eeace2f2";
        "https://stenddss.cryptopro.ru:4430/opensmeidp/ums/";

    /**
     * Хранилище доверенных корневых сертификатов.
     * Содержит корневой сертификат УЦ сервера.
     */
    private static final String TRUST_STORE = "trust.store";

    /**
     * Пароль хранилища доверенных корневых
     * сертификатов.
     */
    private static final char[] TRUST_STORE_PASSWORD = "password".toCharArray();

    /**
     * Пароль ключевого контейнера.
     */
    private static final char[] KEY_STORE_PASSWORD = "1111".toCharArray();

    /**
     * Запуск примера.
     *
     * @param args Параметры командной строки.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Включаем enableCRLDP для проверки цепочки
        // сертификатов сервера онлайн (по CRL DP из
        // сертификата).

        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        // Загрузка хранилища контейнеров.

        KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
        keyStore.load(null, null);

        // Загрузка хранилища доверенных корневых
        // сертификатов.

        KeyStore trustStore = KeyStore.getInstance(JCP.CERT_STORE_NAME);
        try (FileInputStream is = new FileInputStream(TRUST_STORE)) {
            trustStore.load(is, TRUST_STORE_PASSWORD);
        }

        KeyManagerFactory kmFactory = KeyManagerFactory.getInstance(Provider.KEYMANGER_ALG, Provider.PROVIDER_NAME);
        kmFactory.init(keyStore, KEY_STORE_PASSWORD);

        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(Provider.TRUSTMANGER_ALG, Provider.PROVIDER_NAME);
        tmFactory.init(trustStore);

        // Инициализация SSL контекста.

        SSLContext sslContext = SSLContext.getInstance(Provider.ALGORITHM, Provider.PROVIDER_NAME);
        sslContext.init(kmFactory.getKeyManagers(), tmFactory.getTrustManagers(), null);

        // Создание подключения.

        URL url = new URL(URL);
        System.out.println(url);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setSSLSocketFactory(sslContext.getSocketFactory());

        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        connection.setRequestProperty("Content-Type", "application/json");
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.write("{\"Login\":\"RestTestUser\",\"Email\":\"test@cp.ru\",\"PhoneNumber\":\"+79150510528\"}".getBytes());
        }

        connection.connect();

        // Получение и вывод ответа.

        int responseCode = connection.getResponseCode();
        BufferedReader bufferedReader = null;

        try {

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Invalid http response: " + responseCode);
            }

            InputStreamReader inputStreamReader = new InputStreamReader(
                connection.getInputStream(), "UTF-8");

            bufferedReader = new BufferedReader(inputStreamReader);
            String input;

            while ((input = bufferedReader.readLine()) != null) {
                System.out.println(input);
            }

        } finally {

            if (bufferedReader != null) {
                bufferedReader.close();
            }

            connection.disconnect();

        }

    }

}
