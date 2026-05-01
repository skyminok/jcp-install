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

import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.reprov.RevCheck;

import ru.CryptoPro.ssl.Provider;
import ru.CryptoPro.ssl.util.TLSContext;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.Security;

/**
 * Пример односторонней аутентификации TLS клиента.
 * В примере используется класс HttpsURLConnection.
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class ClientSample {

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

        SSLContext ctx = TLSContext.initClientSSL(
            null,
            trustStorePath,
            trustStorePassword,
            null
        );

        SSLSocketFactory factory = ctx.getSocketFactory();
        connect(factory, urlPath);

    }

    /**
     * Функция устанавливает подключение по заданному адресу
     * на основе переданного SSLSocketFactory.
     *
     * @param factory Объект SSLSocketFactory.
     * @param urlPath Адрес для подключения.
     *
     * @throws Exception
     */
    public static void connect(SSLSocketFactory factory,
        String urlPath) throws Exception {

        URL url = new URL(urlPath);

        // Установка нового соединения с заданным адресом.
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        // Задание для него требуемого SSLSocketFactory.
        connection.setSSLSocketFactory(factory);

        // Вывод на экран содержимого запрошенной страницы.
        printContent(connection);

        // Разрыв соединения.
        connection.disconnect();

    }

    /**
     * Функция выводит на экран содержимое запрошенной страницы.
     *
     * @param connection Соединение.
     * @throws Exception
     */
    private static void printContent(HttpsURLConnection connection)
        throws Exception {

        if (connection != null) {
            printStream(connection.getInputStream());
        } // if

    }

    /**
     * Функция выводит на экран содержимое потока.
     *
     * @param inputStream Поток.
     * @throws Exception
     */
    public static void printStream(InputStream inputStream) throws Exception {

        BufferedReader br = new BufferedReader(new InputStreamReader(
            inputStream, "windows-1251"));

        String input;
        while ((input = br.readLine()) != null) {
            System.out.println(input);
        } // while

        br.close();

    }

}
