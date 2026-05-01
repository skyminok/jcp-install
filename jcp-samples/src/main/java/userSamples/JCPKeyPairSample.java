/**
 * $RCSfileJCPKeyPairSample.java,v $
 * version $Revision$
 * created 23.10.2019 18:39 by elvira
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2019.
 * <p/>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p/>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package userSamples;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.tools.JCPKeyPair;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;

/**
 * Пример использования класса JCPKeyPair для проверки
 * соответствия открытого и закрытого ключей.
 *
 * @author Copyright 2004-2019 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class JCPKeyPairSample {

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JCPInit.initProviders(false);

        // генерация ключей

        matchExample(JCP.GOST_EL_DEGREE_NAME,   JCP.PROVIDER_NAME, null, null, null, null, null);
        matchExample(JCP.GOST_EL_2012_256_NAME, JCP.PROVIDER_NAME, null, null, null, null, null);
        matchExample(JCP.GOST_EL_2012_512_NAME, JCP.PROVIDER_NAME, null, null, null, null, null);

        // чтение ключей

        matchExample(null, JCP.PROVIDER_NAME, JCP.HD_STORE_NAME, "clientTLS", "1".toCharArray(), "serverTLS", "1".toCharArray());
        matchExample(null, JCP.PROVIDER_NAME, JCP.HD_STORE_NAME, "le-30bc1465-456b-4317-9876-153e265bcc8d", "2".toCharArray(), "le-704999da-69b2-4c7f-ada7-49d6cad6c2c2", "2".toCharArray());
        matchExample(null, JCP.PROVIDER_NAME, JCP.HD_STORE_NAME, "le-5584fd0c-4670-46ab-9b40-a39700e5e851", "3".toCharArray(), "le-4b88f437-e0f7-4d57-b1e3-9404792dc0ed", "3".toCharArray());

    }

    /**
     *
     * @param algorithm Алгоритм ключа.
     * @param provider Имя провайдера.
     * @throws Exception
     */
    public static void matchExample(String algorithm,
        String provider, String keyStoreType, String
        alias1, char[] password1, String alias2, char[]
        password2) throws Exception {

        KeyStore keyStore = null;
        KeyPair keyPair1;
        KeyPair keyPair2;

        if (keyStoreType != null) {

            keyStore = KeyStore.getInstance(keyStoreType, provider);
            keyStore.load(null, null);

        } // if

        if (keyStore != null && alias1 != null) {

            // чтение ключа и сертификата

            JCPPrivateKeyEntry entry1 = (JCPPrivateKeyEntry) keyStore.getEntry(alias1,
                new JCPProtectionParameter(password1));

            keyPair1 = new KeyPair(entry1.getCertificate().getPublicKey(),
                entry1.getPrivateKey());

        } // if
        else {

            // генерация первой ключевой пары

            KeyPairGenerator keyGen1 = KeyPairGenerator.getInstance(algorithm, provider);
            keyPair1 = keyGen1.generateKeyPair();

        } // else

        if (keyStore != null && alias2 != null) {

            // чтение ключа и сертификата

            JCPPrivateKeyEntry entry2 = (JCPPrivateKeyEntry) keyStore.getEntry(alias2,
                new JCPProtectionParameter(password2));

            keyPair2 = new KeyPair(entry2.getCertificate().getPublicKey(),
                entry2.getPrivateKey());

        } // if
        else {

            // генерация второй ключевой пары

            KeyPairGenerator keyGen2 = KeyPairGenerator.getInstance(algorithm, provider);
            keyPair2 = keyGen2.generateKeyPair();

        } // else

        // ключи из одной пары должны соответствовать друг другу

        JCPKeyPair jcpKeyPair = new JCPKeyPair(
            keyPair1.getPublic(), keyPair1.getPrivate());

        if (!jcpKeyPair.match()) {
            throw new Exception("Keys should match");
        }

        // ключи из одной пары должны соответствовать друг другу

        jcpKeyPair = new JCPKeyPair(keyPair2.getPublic(),
            keyPair2.getPrivate());

        if (!jcpKeyPair.match()) {
            throw new Exception("Keys should match");
        }

        // ключи из разных пар не должны соответствовать друг другу

        jcpKeyPair = new JCPKeyPair(keyPair1.getPublic(),
            keyPair2.getPrivate());

        if (jcpKeyPair.match()) {
            throw new Exception("Keys should not match");
        }

        // ключи из разных пар не должны соответствовать друг другу

        jcpKeyPair = new JCPKeyPair(keyPair2.getPublic(),
            keyPair1.getPrivate());

        if (jcpKeyPair.match()) {
            throw new Exception("Keys should not match");
        }

        System.out.println("Completed.");

    }

}
