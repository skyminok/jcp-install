/**
 * $RCSfile$
 * version $Revision$
 * created 10.07.2007 10:01:02 by kunina
 * last modified $Date$ by $Author$
 *
 * Copyright 2004-2005 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JTLS_samples;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * INSERT BRIEF DESCRIPTION HERE!.
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class URLClient {
/**
 *
 */
private URLClient() {
}

public static void main(String[] args) {
    try {
        //final String urlName = "http://www.cryptopro.ru";
        final String urlName = "https://www.cryptopro.ru:9443";
        final URL url = new URL(urlName);
        final URLConnection connection = url.openConnection();

        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.connect();

        //вывод полей заголовка
        int n = 1;
        String key;
        while ((key = connection.getHeaderFieldKey(n)) != null) {
            String value = connection.getHeaderField(n);
            System.out.println(key + ": " + value);
            n++;
        }
        //вывод данных вместе с названием функции
        System.out.println("-----------------");
        System.out.println("getContentType: " + connection.getContentType());
        System.out
                .println("getContentLength: " + connection.getContentLength());
        System.out.println(
                "getContentEncoding: " + connection.getContentEncoding());
        System.out.println("getDate: " + connection.getDate());
        System.out.println("getExpiration: " + connection.getExpiration());
        System.out.println("getLastModified: " + connection.getLastModified());
        System.out.println("------------------");
        System.out.println("getHeaderFields:\n" + connection.getHeaderFields());
        System.out.println("------------------");

        final BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        //вывод полученных данных в файл
        String line;
        String text = "";
        n = 1;
        while ((line = in.readLine()) != null) {
            text = text + line;
            n++;
        }

        final byte[] data = text.getBytes();/**/

        final FileOutputStream fout =
                new FileOutputStream("D:\\Job\\test\\myData\\url.html");
        try {
            fout.write(data);
        } finally {
            if (fout != null)
                fout.close();
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}
