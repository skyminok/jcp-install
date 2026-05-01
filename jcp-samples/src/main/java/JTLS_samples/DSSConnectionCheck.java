/**
 * Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JTLS_samples;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.tools.Decoder;

import ru.CryptoPro.ssl.Provider;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Пример создания защищенного подключения с использованием
 * ГОСТ и иностранных алгоритмов.
 *
 * Рекомендуется использовать не HttpsURLConnection, а иные
 * http-клиенты (ok http, apache http client).
 *
 */
public class DSSConnectionCheck {

    /**
     * Создание подключения.
     *
     * @param url Адрес для подключения.
     * @param sslCtx Защищенный контекст.
     * @throws Exception
     */
    private static void testConnection(URL url, SSLContext sslCtx)
        throws Exception {

        // Создаем SSL factory.

        SSLSocketFactory sslSocketFactory = sslCtx.getSocketFactory();
        HttpsURLConnection connection = null;

        try {

            // Подключаемся и выводим информацию.

            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sslSocketFactory);

            System.out.println("Response code: " +    connection.getResponseCode());
            System.out.println("Response message: " + connection.getResponseMessage());
            System.out.println("Cipher suite: " +     connection.getCipherSuite());

            Certificate[] localCert  = connection.getLocalCertificates();
            Certificate[] serverCert = connection.getServerCertificates();

            if (localCert != null && localCert.length > 0) { // ГОСТ fixed: JCP-1649

                System.out.println("Local certificate: " +
                    ((X509Certificate) localCert[0]).getSubjectDN());

            } // if

            if (serverCert != null && serverCert.length > 0) {

                System.out.println("Server certificate: " +
                    ((X509Certificate) serverCert[0]).getSubjectDN());

            } // if

            TLSUtility.print_content(connection, null);

        } finally {
            if (connection != null) {
                connection.disconnect();
            } // if
        }

    }

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        // Эти параметры для того, чтобы можно было использовать
        // CRL DP из сертификатов цепочки сервера для проверки
        // цепочки сертификатов сервера.

        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        // Задаем настройки по умолчанию для случая HttpsURLConnection,
        // если они переопределены при установке cpSSL в java.security.

        Security.setProperty("ssl.KeyManagerFactory.algorithm",   "SunX509");
        Security.setProperty("ssl.TrustManagerFactory.algorithm", "PKIX");

        Security.setProperty("ssl.SocketFactory.provider",       "");
        Security.setProperty("ssl.ServerSocketFactory.provider", "");

        //-------------------------------------------- Корневые сертификаты --------------------------------------------

        // Хранилище доверенных сертификатов.
        // Должно содержать КОРНЕВОЙ сертификат цепочки сервера.
        //
        // Сертификат Минкомсвязи - корневой сертификат цепочки
        // сервера.

        final String MIN_KOM_ROOT =
            "MIIFFDCCBMGgAwIBAgIQTm1HiybyfWV/do4CXOPTkzAKBggqhQMHAQEDAjCCASQx\n" +
            "HjAcBgkqhkiG9w0BCQEWD2RpdEBtaW5zdnlhei5ydTELMAkGA1UEBhMCUlUxGDAW\n" +
            "BgNVBAgMDzc3INCc0L7RgdC60LLQsDEZMBcGA1UEBwwQ0LMuINCc0L7RgdC60LLQ\n" +
            "sDEuMCwGA1UECQwl0YPQu9C40YbQsCDQotCy0LXRgNGB0LrQsNGPLCDQtNC+0Lwg\n" +
            "NzEsMCoGA1UECgwj0JzQuNC90LrQvtC80YHQstGP0LfRjCDQoNC+0YHRgdC40Lgx\n" +
            "GDAWBgUqhQNkARINMTA0NzcwMjAyNjcwMTEaMBgGCCqFAwOBAwEBEgwwMDc3MTA0\n" +
            "NzQzNzUxLDAqBgNVBAMMI9Cc0LjQvdC60L7QvNGB0LLRj9C30Ywg0KDQvtGB0YHQ\n" +
            "uNC4MB4XDTE4MDcwNjEyMTgwNloXDTM2MDcwMTEyMTgwNlowggEkMR4wHAYJKoZI\n" +
            "hvcNAQkBFg9kaXRAbWluc3Z5YXoucnUxCzAJBgNVBAYTAlJVMRgwFgYDVQQIDA83\n" +
            "NyDQnNC+0YHQutCy0LAxGTAXBgNVBAcMENCzLiDQnNC+0YHQutCy0LAxLjAsBgNV\n" +
            "BAkMJdGD0LvQuNGG0LAg0KLQstC10YDRgdC60LDRjywg0LTQvtC8IDcxLDAqBgNV\n" +
            "BAoMI9Cc0LjQvdC60L7QvNGB0LLRj9C30Ywg0KDQvtGB0YHQuNC4MRgwFgYFKoUD\n" +
            "ZAESDTEwNDc3MDIwMjY3MDExGjAYBggqhQMDgQMBARIMMDA3NzEwNDc0Mzc1MSww\n" +
            "KgYDVQQDDCPQnNC40L3QutC+0LzRgdCy0Y/Qt9GMINCg0L7RgdGB0LjQuDBmMB8G\n" +
            "CCqFAwcBAQEBMBMGByqFAwICIwEGCCqFAwcBAQICA0MABEB1OSpFp7milX33EP0i\n" +
            "kge6HbZacYp9fVj8sUa5RWFXrB27SKX5SvtIGepqKev69RSYeHHKR+jT9YX2NuSK\n" +
            "9wONo4IBwjCCAb4wgfUGBSqFA2RwBIHrMIHoDDTQn9CQ0JrQnCDCq9Ca0YDQuNC/\n" +
            "0YLQvtCf0YDQviBIU03CuyDQstC10YDRgdC40LggMi4wDEPQn9CQ0JogwqvQk9C+\n" +
            "0LvQvtCy0L3QvtC5INGD0LTQvtGB0YLQvtCy0LXRgNGP0Y7RidC40Lkg0YbQtdC9\n" +
            "0YLRgMK7DDXQl9Cw0LrQu9GO0YfQtdC90LjQtSDihJYgMTQ5LzMvMi8yLzIzINC+\n" +
            "0YIgMDIuMDMuMjAxOAw00JfQsNC60LvRjtGH0LXQvdC40LUg4oSWIDE0OS83LzYv\n" +
            "MTA1INC+0YIgMjcuMDYuMjAxODA/BgUqhQNkbwQ2DDTQn9CQ0JrQnCDCq9Ca0YDQ\n" +
            "uNC/0YLQvtCf0YDQviBIU03CuyDQstC10YDRgdC40LggMi4wMEMGA1UdIAQ8MDow\n" +
            "CAYGKoUDZHEBMAgGBiqFA2RxAjAIBgYqhQNkcQMwCAYGKoUDZHEEMAgGBiqFA2Rx\n" +
            "BTAGBgRVHSAAMA4GA1UdDwEB/wQEAwIBBjAPBgNVHRMBAf8EBTADAQH/MB0GA1Ud\n" +
            "DgQWBBTCVPG0a9RMt+BtNrQjkPH+wzybBjAKBggqhQMHAQEDAgNBAJr6/eI7rHL7\n" +
            "+FsQnoH2i6DVxqalbIxLKj05edpZGPLLb6B2PTAMya7pSt9hb8QnFABgsR4IE5gT\n" +
            "4VVkDWbX/n4=";

        // Сертификат  Certum Trusted Network CA - корневой
        // сертификат цепочки сервера.

        final String YANDEX_ROOT =
            "MIIDuzCCAqOgAwIBAgIDBETAMA0GCSqGSIb3DQEBBQUAMH4xCzAJBgNVBAYTAlBM\n" +
            "MSIwIAYDVQQKExlVbml6ZXRvIFRlY2hub2xvZ2llcyBTLkEuMScwJQYDVQQLEx5D\n" +
            "ZXJ0dW0gQ2VydGlmaWNhdGlvbiBBdXRob3JpdHkxIjAgBgNVBAMTGUNlcnR1bSBU\n" +
            "cnVzdGVkIE5ldHdvcmsgQ0EwHhcNMDgxMDIyMTIwNzM3WhcNMjkxMjMxMTIwNzM3\n" +
            "WjB+MQswCQYDVQQGEwJQTDEiMCAGA1UEChMZVW5pemV0byBUZWNobm9sb2dpZXMg\n" +
            "Uy5BLjEnMCUGA1UECxMeQ2VydHVtIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MSIw\n" +
            "IAYDVQQDExlDZXJ0dW0gVHJ1c3RlZCBOZXR3b3JrIENBMIIBIjANBgkqhkiG9w0B\n" +
            "AQEFAAOCAQ8AMIIBCgKCAQEA4/t9o3K6wvDJFIf1awFO4W5AB7ptJ11/91sts1rH\n" +
            "UV+rpDKmYYe2bg+G0jACl/jXaVehGDldamR5xgFZrDwxSjh80gTSSyjoIF87B6LM\n" +
            "TXPb865Px1bVWqeWifrzq2jUI4ZZJ88JJ7ysbnKDHDBy3+Ci6dLhdHUZvSqeexVU\n" +
            "BBvXQzmtVSjF4hq79MDkrjhJM8x2hZ85RdKknvISjFH4fOQtf/WsX+sWn7Et0brM\n" +
            "kUJ3TCXJkDhv2/DM+44el1k+1WBO5gUo7Ul5E0u6SNsv+XLTOcr+H9g0cvW0QM8x\n" +
            "AcPs3hEtF10fuFDRXhmnad4HMyjKUJX5p1TLVIZQRan5SQIDAQABo0IwQDAPBgNV\n" +
            "HRMBAf8EBTADAQH/MB0GA1UdDgQWBBQIds3LB/8k9sXN7buQvOKEN0Z19zAOBgNV\n" +
            "HQ8BAf8EBAMCAQYwDQYJKoZIhvcNAQEFBQADggEBAKaorSLOAT2mo/9i0Eidi15y\n" +
            "sHhE49wcrwn9I0j6vSrEuVUEtRCjjSfeC4Jj0O7eDDd5QVsisrCaQVymcODU0HfL\n" +
            "I9MA4GxWL+FpDQ3Zqr8hgVDZBqWo/5U30Kr+4rP1mS1FhIrlQgnXdAIv94nYmem8\n" +
            "J9RHjboNRhx3zxSkHLmkMcScKHQDNP8zGSal6Q10tz6XxnboJ5ajZt3hrvJBW8qY\n" +
            "VoNzcOSGGtIxQbovvi0TWnZvTuhOgQ4/WwMioBK+ZlgRSssDxLQqKi2WF+A5VLxI\n" +
            "03YnnZotBqbJ7DnSq9ufmgsnAjUpsUCV5/nonFWIGUbWtzT1fs45mtk48VH3Tyw=";

        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null);

        byte[] minkomRootBin = (new Decoder()).decodeBuffer(MIN_KOM_ROOT);
        byte[] yandexRootBin = (new Decoder()).decodeBuffer(YANDEX_ROOT);

        X509Certificate minkomRoot = (X509Certificate) factory
            .generateCertificate(new ByteArrayInputStream(minkomRootBin));

        X509Certificate yandexRoot = (X509Certificate) factory
            .generateCertificate(new ByteArrayInputStream(yandexRootBin));

        // Добавление корневого сертификата в хранилище
        // доверенных сертификатов.

        trustStore.setCertificateEntry("min_kom", minkomRoot);
        trustStore.setCertificateEntry("yandex_root", yandexRoot);

        //-------------------------------------------- ГОСТ ------------------------------------------------------------
        // С клиентской аутентификацией.

        // Адрес подключения (DSS).

        URL url = new URL("https://saas-dev.cryptopro.ru:4430/ndbdevidp/oauth/authorize/certificate?client_id=ndbdev&response_type=code&scope=dss&redirect_uri=urn:ietf:wg:oauth:2.0:oob:auto&resource=urn:cryptopro:dss:signserver:ndbdevss");

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
            Provider.TRUSTMANGER_ALG,
            Provider.PROVIDER_NAME
        );

        tmf.init(trustStore);

        // Хранилище клиентских контейнеров. Нужно, если на сервере
        // включена клиентская аутентификация. Ключ клиента будет
        // выбран по двум критериям: пароль к нему и тип хранилища.
        // Сертификат клиента должен содержать использование ключа =
        // "Клиентская аутентификация". Если длина клиентской цепочки
        // больше 2, например, CLIENT -> CA -> ROOT, то в контейнер
        // клиента должна быть установлена ВСЯ цепочка.

        KeyStore keyStore = KeyStore.getInstance(
            JCP.HD_STORE_NAME, // тип хранилища - на диске
            JCP.PROVIDER_NAME
        );

        keyStore.load(null, null);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
            Provider.KEYMANGER_ALG,
            Provider.PROVIDER_NAME
        );

        kmf.init(keyStore, "12345678".toCharArray()); // пароль к предполагаемому ключевому контейнеру

        // Создаем защищенный контекст.

        SSLContext sslCtx = SSLContext.getInstance(
            Provider.ALGORITHM_12, // TLS v.1.2 и ниже
            Provider.PROVIDER_NAME
        );

        sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        try {
            testConnection(url, sslCtx);
        } catch (Exception e) { // ловим исключение из-за переадресации
            e.printStackTrace(System.err);
        }

        //------------------------------------------- Иностр. ----------------------------------------------------------
        // Без клиентской аутентификации.

        URL urlOther = new URL("https://ya.ru/");

        String trustAlgorithmOther   = TrustManagerFactory.getDefaultAlgorithm(); // согласно алгоритму из Security
        TrustManagerFactory tmfOther = TrustManagerFactory.getInstance(trustAlgorithmOther);

        tmfOther.init(trustStore);
        SSLContext sslCtxOther = SSLContext.getInstance("TLSv1.2");

        sslCtxOther.init(null, tmfOther.getTrustManagers(), null);
        testConnection(urlOther, sslCtxOther);

    }

}
