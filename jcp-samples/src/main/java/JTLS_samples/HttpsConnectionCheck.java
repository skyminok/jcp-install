/**
 * $RCSfileHttpsConnectionCheck.java,v $ version $Revision: 36379 $ created 28.08.2018
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

import ru.CryptoPro.ssl.Provider;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;

/**
 * Пример подключения к тестовому серверу с
 * использованием клиентского контейнера.
 *
 * @author Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class HttpsConnectionCheck {

    public static void main(String[] args) throws Exception {

        System.setProperty("com.sun.security.enableCRLDP", "true");
        URL url = new URL("https://testgost2012st.cryptopro.ru/gost2st.txt");

        KeyStore keyStore = KeyStore.getInstance("HDImageStore", "JCP");
        keyStore.load(null, null);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(Provider.KEYMANGER_ALG, Provider.PROVIDER_NAME);
        kmf.init(keyStore, "1".toCharArray());

        KeyStore trustedKeyStore = KeyStore.getInstance("JKS");
        try (FileInputStream is = new FileInputStream("C:/Projects/store.jks")) {
            trustedKeyStore.load(is, "123456".toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(Provider.TRUSTMANGER_ALG, Provider.PROVIDER_NAME);
        tmf.init(trustedKeyStore);

        SSLContext sslContext = SSLContext.getInstance(Provider.ALGORITHM, Provider.PROVIDER_NAME);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setSSLSocketFactory(socketFactory);

        connection.connect();
        TLSUtility.print_content(connection, null);

        connection.disconnect();
        System.out.println("OK");

    }

}
