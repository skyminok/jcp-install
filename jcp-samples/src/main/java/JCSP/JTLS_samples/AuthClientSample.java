/**
 * $RCSfileAuthClientSample.java,v $
 * version $Revision: 36379 $
 * created 13.02.2020 16:54 by afevma
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

import JTLS_samples.ClientSample;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.reprov.RevCheck;

import ru.CryptoPro.ssl.Provider;
import ru.CryptoPro.ssl.util.TLSContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.security.Security;

/**
 * Пример двухсторонней аутентификации TLS клиента.
 * В примере используется класс HttpsURLConnection.
 *
 * В примере должен использоваться провайдер Java CSP.
 * {@link ru.CryptoPro.JCSP.JCSP}
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class AuthClientSample {

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

        // Т.к. в ClientSample.connect используется HttpsURLConnection,
        // который создаст дефолтный (static) контекст при том, что в
        // в HttpsURLConnection передается динамический контекст, то,
        // чтобы избежать появления лишних окон CSP для ввода пароля,
        // нужно задать это свойство.

        System.setProperty("disable_default_context", "true");

        SSLContext ctx = TLSContext.initAuthClientSSL(
            keyStoreProvider,
            keyStoreType,
            "ok_client",
            null,
            trustStorePath,
            trustStorePassword,
            null
        );

        SSLSocketFactory factory = ctx.getSocketFactory();
        ClientSample.connect(factory, urlPath);

    }

}
