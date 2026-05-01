package JCSP.RSA;

import ru.CryptoPro.JCP.KeyStore.ContainerStore;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.spec.NameAlgIdSpecForeign;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.JCSPRSA;
import ru.CryptoPro.JCSP.params.PasswordParamsSpec;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;

/**
 * Данный пример демонстрирует генерацию и сохранение секретных ключей AES, 3DES с помощью криптопровайдера JCSPRSA.
 */
public class GenSecretKeyExample {

    //Тип хранилища для записи ключей
    static String STORETYPE = JCSP.HD_STORE_NAME;

    /**
     * Генерация иностранных секретных ключей.
     * В примере осуществляется генерация ключа (с сохранением в контейнер).
     * @param keyAlg Имя алгоритма ключа, который будет сгенерен.
     * @param alias Алиас ключа.
     * @param password Пароль.
     * @param keyLen Длина ключа
     * @param useStorePath Передавать имя хранилища генератору
     @throws Exception
     */
    public static void generateKey( String keyAlg, String alias, char[] password, int keyLen, boolean useStorePath) throws Exception{

        // Пробуем удалить контейнер на случай, если такой есть.

        try {
            deleteContainer(alias, password);
        } catch (Exception e) {
            // ignore
        }

        // Генерация ключа заданной длины.

        KeyGenerator kg = KeyGenerator.getInstance(keyAlg, JCSPRSA.PROVIDER_NAME);
        kg.init(keyLen);

        // Можно генератору сразу передать пароль и имя хранилища.
        // В  этом случае ключ сразу сохранится в контейнере в момент генерации.
        if (useStorePath) {
            NameAlgIdSpecForeign spec1 = new NameAlgIdSpecForeign("\\\\.\\" + STORETYPE + "\\" + alias);
            kg.init(spec1);
            PasswordParamsSpec spec2 = new PasswordParamsSpec(password);
            kg.init(spec2);
        }

        SecretKey key = kg.generateKey();

        System.out.println(key);
        System.out.println("generateKey() completed.");

        // Если имя контейнера не было передано генератору, ключ необходимо дополнительно сохранить
        if (!useStorePath) {
            saveSymmetricContainer(alias, password, key);
        }
    }

    /**
     * Удаление контейнера.
     *
     * @param alias Алиас ключа.
     * @param password Пароль.
     * @throws Exception
     */
    public static void deleteContainer( String alias, char[] password)
            throws Exception {

        System.out.println("deleteContainer() started...");

        KeyStore keyStore = KeyStore.getInstance(
                STORETYPE, JCSPRSA.PROVIDER_NAME);

        keyStore.load(null, null);

        if (password != null) {

            alias += ContainerStore.PASSWORD_PREFIX
                    + String.valueOf(password);

        } // if

        keyStore.deleteEntry(alias);
        System.out.println("deleteContainer() completed.");

    }

    /**
     * Сохранение симметричного ключа в контейнер.
     *
     * @param alias Алиас сохраняемого ключа.
     * @param password Пароль к ключу.
     * @param secretKey Симметричный ключ.
     * @throws Exception
     */
    public static void saveSymmetricContainer(String alias,
                                              char[] password, SecretKey secretKey) throws Exception {

        System.out.println("saveContainer() started...");


        KeyStore keyStore = KeyStore.getInstance(STORETYPE, JCSPRSA.PROVIDER_NAME);
        keyStore.load(null, null);

        JCPProtectionParameter parameter = new JCPProtectionParameter(password);
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);

        keyStore.setEntry(alias, entry, parameter);
        System.out.println("saveContainer() completed.");

    }


    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Добавление провайдеров.
        JCPInit.initProviders(true);

        //Генерация и одновременное сохранение ключа подписи AES длиной 192
        generateKey(JCSP.AES_NAME,
                "aeskey",
                "1".toCharArray(),
                192,
                true);

        //Генерация и последующее сохранение ключа подписи AES длиной 256
        generateKey(JCSP.AES_NAME,
                "aeskey2",
                "1".toCharArray(),
                256,
                false);

        //Генерация и одновременное сохранение ключа подписи 3DES длиной 192
        generateKey(JCSP.TRIPLE_DES_NAME,
                "3deskey",
                "1".toCharArray(),
                192,
                true);

        //Генерация и последующее сохранение ключа подписи 3DES длиной 128
        generateKey(JCSP.TRIPLE_DES_NAME,
                "3deskey2",
                "1".toCharArray(),
                128,
                false);

    }


}

