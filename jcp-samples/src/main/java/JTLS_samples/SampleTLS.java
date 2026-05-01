/**
 * $RCSfile$
 * version $Revision$
 * created 03.07.2007 13:36:23 by kunina
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2007.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован 
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JTLS_samples;

import ru.CryptoPro.JCP.Util.JCPInit;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Простейший пример использования TLS.
 *
 * @author Copyright 2004-2007 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class SampleTLS {
/**
 *
 */
private SampleTLS() {
}

/**
 * @param args /
 * @throws IOException ошибки ввода-вывода
 */
public static void main(String[] args) throws IOException {

    final int sslPort = 8443;
    Server server = null;

    try {

        //JCPInit.initProviders(false);

        // System.setProperty("javax.net.ssl.supportGVO","true");
        // Настройки для клиента или сервера (смотря в какой роли выступает приложение), если
        // необходима двухсторонняя аутентификация.
        // При двусторонней аутентификации эти настройки обязательны: тип хранилища и пароль к ключу, по
        // которому будет загружен первый подходящий ключ.
        // Если используется односторонняя аутентификация, то задавать тип ключа и пароль к ключу для
        // клиента или сервера необязательно.
        System.setProperty("javax.net.ssl.keyStoreType", "HDImageStore");
        System.setProperty("javax.net.ssl.keyStorePassword", "1");
        // Обязательные настройки при односторонней и двухсторонней аутентификации: указание, какое хранилище
        // содержит доверенный корневой сертификат, и пароль к хранилищу.
        System.setProperty("javax.net.ssl.trustStoreType", "HDImageStore");
        System.setProperty("javax.net.ssl.trustStore",
                "C:\\test\\empty.store");
        System.setProperty("javax.net.ssl.trustStorePassword", "1");

        server = new Server();
        server.create(sslPort, false, "C:\\test\\serverDir");
        server.setTimeout(100000);
        server.start();

        final Client client = new Client("localhost", sslPort);
        client.setTimeout(100000);

        if (client.get("myDoc.txt", "out.html", null) != 0) {
            throw new IOException("Couldn't get data.");
        } // if

        if (!server.isAlive())
            throw new IOException();

    } catch (Exception e) {
        Logger.getLogger("LOGGER").log(Level.SEVERE, e.toString());
        //e.printStackTrace();
    } finally {
        if (server != null)
            server.stop();
    }


}
}
