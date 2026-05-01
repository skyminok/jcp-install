/**
 * TLSUtility.java,v $
 * version $
 * created 02.11.2020 9:45 by afevma
 * last modified $ by $
 * (C) ООО Крипто-Про 2004-2020.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JTLS_samples;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Служебные функции.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class TLSUtility {

    /**
     * Вывод полученных данных.
     *
     * @param connection Соединение.
     */
    public static void print_content(HttpsURLConnection
        connection, String encoding) throws Exception {

        if (encoding == null) {
            encoding = "windows-1251";
        } // if

        if (connection != null) {

            System.out.println("------ CONTENT BEGIN ------");

            BufferedReader br = new BufferedReader(new InputStreamReader(
                connection.getInputStream(), encoding));

            String input;

            while ((input = br.readLine()) != null) {
                System.out.println(input);
            } // while

            br.close();
            System.out.println("------ CONTENT END ------");

        } // if

    }

}
