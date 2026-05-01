/**
 * $RCSfileOkHTTPAuthClientSample.java,v $
 * version $Revision: 36379 $
 * created 13.02.2020 17:13 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * (C) ООО Крипто-Про 2004-2020.
 * <p/>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p/>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.JTLS_samples;

import JTLS_samples.OkHTTPClientSample;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.reprov.RevCheck;

import ru.CryptoPro.ssl.Provider;
import ru.CryptoPro.ssl.util.TLSContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.Security;

/**
 * Пример двухсторонней аутентификации TLS клиента.
 * В примере используется класс OkHttpClient.
 *
 * В примере должен использоваться провайдер Java CSP.
 * {@link ru.CryptoPro.JCSP.JCSP}
 *
 * Для работы примера нужны библиотеки:
 * kotlin-stdlib-1.3.72.jar
 * okhttp-4.8.0.jar
 * okio-2.7.0.jar
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class OkHTTPAuthClientSample {

    /**
     * Запуск примера.
     *
     * В примере должен использоваться провайдер Java CSP.
     * {@link ru.CryptoPro.JCSP.JCSP}
     *
     * @param args Аргументы.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Security.addProvider(new JCSP());
        Security.addProvider(new RevCheck());
        Security.addProvider(new Provider());

        String trustStorePath = "C:/Projects/trust-2.store";
        String trustStorePassword = "1";

        String keyStoreProvider = JCSP.PROVIDER_NAME;
        String keyStoreType = JCSP.HD_STORE_NAME;

        String urlPath = "https://www.cryptopro.ru:4444/test/tls-cli.asp";

        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.ibm.security.enableCRLDP", "true");

        TrustManager[] trustManagers = new TrustManager[1];

        // Загрузка хранилища доверенных сертификатов. Инициализация
        // на основе его TrustManagerFactory. Создание и инициализация
        // контекста SSLContext.
        //
        // Пароль к контейнеру ok_client не передается, т.к. будет
        // запрошен в окне ввода пароля CSP.

        SSLContext ctx = TLSContext.initAuthClientSSL(
            Provider.PROVIDER_NAME,
            "TLSv1.2",
            keyStoreProvider,
            keyStoreType,
            "ok_client",
            JCP.PROVIDER_NAME,
            JCP.CERT_STORE_NAME,
            trustStorePath,
            trustStorePassword,
            trustManagers
        );

        SSLSocketFactory factory = ctx.getSocketFactory();
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
        OkHTTPClientSample.connect(urlPath, factory, trustManager);

    }


}
