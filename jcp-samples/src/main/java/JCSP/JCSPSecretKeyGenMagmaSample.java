package JCSP;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCSP.JCSP;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyStore;


/**
 * Пример генерации сессионного секретного ключа на GOST_M_CIPHER_NAME для провайдера JCSP
 * с сохранением ключа в контейнере HDIMAGE
 */

public class JCSPSecretKeyGenMagmaSample {

    /** Генерация секретного ключа
     *
     * @param algorithm
     * @param provider
     */
    public static SecretKey generateSecretKey(String algorithm, String provider) throws Exception {

        KeyGenerator keygen = KeyGenerator.getInstance(algorithm, provider);
        SecretKey secretKey = keygen.generateKey();

        System.out.println("generateSecretKey() completed");
        return secretKey;

    }

    /** Сохранение секретного ключа
     *
     * @param secretKey
     * @param storeType Тип контейнера
     * @param alias
     * @param password
     * @param provider
     */
    public static void saveSecretKey(SecretKey secretKey, String storeType, String alias, char[] password, String provider) throws Exception {

        KeyStore keyStore = KeyStore.getInstance(storeType, provider);
        keyStore.load(null, null);

        // Если контейнер не пустой, то удаляем его
        try {
            keyStore.deleteEntry(alias);
        } catch (Exception e) {

        }

        JCPProtectionParameter parameter = new JCPProtectionParameter(password);
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
        keyStore.setEntry(alias, entry, parameter);

        System.out.println("saveSecretKey() completed");

    }


    public static void main(String[] args) throws Exception {

        JCPInit.initProviders(true);

        SecretKey secretKey =
                generateSecretKey(JCP.GOST_M_CIPHER_NAME,
                JCSP.PROVIDER_NAME);

        saveSecretKey(secretKey,
                "HDIMAGE",
                "magma_secret_key",
                "1".toCharArray(),
                JCSP.PROVIDER_NAME);

    }
}
