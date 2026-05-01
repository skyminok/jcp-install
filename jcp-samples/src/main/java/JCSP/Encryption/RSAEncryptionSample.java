/**
 * RSAEncryptionSample.java,v $
 * version $
 * created 21.11.2020 13:01 by afevma
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
package JCSP.Encryption;

import ru.CryptoPro.JCP.Key.PrivateKeyInterface;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.AlgIdInterface;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.spec.NameAlgIdSpecForeign;
import ru.CryptoPro.JCP.tools.HexString;

import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.JCSPRSA;
import ru.CryptoPro.JCSP.params.PasswordParamsSpec;
import ru.CryptoPro.JCSP.params.RSAExchangeKeySpec;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Arrays;

/**
 * Пример шифрования на ключе с алгоритмом RSA.
 * Требуются провайдеры Java CSP RSA и CSP RSA.
 *
 * This example demonstrates encryption and decryption
 * with RSA key.
 * Providers Java CSP RSA and CSP RSA are required.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class RSAEncryptionSample {

    // Данные для шифрования размером не больше длины ключа в байтах.
    // Source data to be encrypted with size less than the key size.
    private static final byte[] DATA = "security".getBytes();

    // Тип хранилища.
    // Key store type.
    private static final String KEY_STORE_TYPE = JCSP.HD_STORE_NAME;

    // Алиас ключа.
    // Key alias.
    private static final String ALIAS = "rsa-2048-hd-sample";

    // Пароль к ключу.
    // Password to key store.
    private static final char[] PASSWORD = "123".toCharArray();

    /**
     * Создание закрытого ключа в ключевом контейнере
     * и зашифрование/расшифрование.
     *
     * This function creates a RSA key in the key container
     * and executes encryption/decryption operations.
     *
     * @throws Exception
     */
    private static void createKeyThenEncryptDecrypt() throws Exception {

        System.out.println("Creating RSA key...");

        // Создаем ключ подписи и обмена в контейнере.
        //
        // Creating a RSA key with type of signature
        // and key exchange in the key container.

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(JCSPRSA.RSA_NAME, JCSPRSA.PROVIDER_NAME);

        kpg.initialize(2048); // длина ключа
        String container = "\\\\.\\" + KEY_STORE_TYPE + "\\" + ALIAS;

        // Указываем путь к контейнеру, чтобы создать ключ
        // и контейнер сразу в генераторе. Это не обязательно,
        // можно создать ключ с помощью генератора и позднее
        // сохранить в контейнер с помощью KeyStore.
        //
        // Setting a path to the key container for creating
        // a key in it after key pair generation. It is not
        // strictly required, because key can be created in
        // key pair generator and saved later using KeyStore.

        AlgIdInterface containerParam = new NameAlgIdSpecForeign(container);
        kpg.initialize(containerParam);

        // Указываем пароль на контейнер.
        //
        // Setting a password.

        PasswordParamsSpec passwordParam = new PasswordParamsSpec(PASSWORD);
        kpg.initialize(passwordParam);

        // Указываем, что ключ подписи и обмена! Иначе
        // расшифрование на закрытом ключе не будет
        // работать.
        //
        // Setting a key type. It must be exchange key
        // otherwise it will can not be used for decryption.

        RSAExchangeKeySpec exchangeParam = new RSAExchangeKeySpec();
        kpg.initialize(exchangeParam);

        // Создаем ключ и контейнер. Контейнер будет
        // содержать только закрытый ключ!
        //
        // Creating a key and it's container. The container
        // will have the key only inside.

        KeyPair keyPair = kpg.genKeyPair();
        System.out.println("Getting public key...");

        // Получаем открытый ключ из закрытого ключа.
        //
        // Getting a public key from the private key.

        KeyFactory kf = KeyFactory.getInstance(JCSPRSA.CP_RSA_NAME, JCSPRSA.PROVIDER_NAME);
        PrivateKeyInterface pki = kf.getKeySpec(keyPair.getPrivate(), PrivateKeyInterface.class);
        PublicKey generatedPublicKey = kf.generatePublic(pki);

        // Проверка шифрованием.
        //
        // Encryption/decryption.

        encryptDecrypt(keyPair.getPrivate(), generatedPublicKey);
        System.out.println("Completed.");

    }

    /**
     * Чтение ранее созданного закрытого ключа из ключевого
     * контейнера и зашифрование/расшифрование.
     *
     * This function reads existing RSA key from the key
     * container and executes encryption/decryption
     * operations.
     *
     * @throws Exception
     */
    private static void readKeyThenEncryptDecrypt() throws Exception {

        System.out.println("Loading key store...");

        // Загрузка хранилища.
        //
        // Loading a key store.

        KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE, JCSPRSA.PROVIDER_NAME);
        keyStore.load(null, null);

        System.out.println("Reading RSA key...");

        // Допускаем чтение только закрытого ключа,
        // без сертификата.
        //
        // Allow reading a key  without certificate.

        JCPProtectionParameter parameter = new JCPProtectionParameter(PASSWORD, true, true); // 3. allow = true
        JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry)keyStore.getEntry(ALIAS, parameter);

        PrivateKey privateKey = entry.getPrivateKey();
        System.out.println("Getting public key...");

        // Получаем открытый ключ из закрытого ключа,
        // т.к. сертификата нет.
        //
        // Getting a public key from the private key.

        KeyFactory kf = KeyFactory.getInstance(JCSPRSA.CP_RSA_NAME, JCSPRSA.PROVIDER_NAME);
        PrivateKeyInterface pki = kf.getKeySpec(privateKey, PrivateKeyInterface.class);
        PublicKey generatedPublicKey = kf.generatePublic(pki);

        // Проверка шифрованием.
        //
        // Encryption/decryption.

        encryptDecrypt(privateKey, generatedPublicKey);
        System.out.println("Completed.");

    }

    /**
     * Зашифрование и расшифрование.
     *
     * Encryption/decryption.
     *
     * @param privateKey Закрытый ключ. Private key.
     * @param publicKey Открытый ключ. Public key.
     * @throws Exception
     */
    private static void encryptDecrypt(PrivateKey privateKey,
        PublicKey publicKey) throws Exception {

        System.out.println("Encrypting...");

        Cipher encCipher = Cipher.getInstance(JCSPRSA.RSA_NAME, JCSPRSA.PROVIDER_NAME);
        encCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encrypted = encCipher.doFinal(DATA, 0, DATA.length);
        System.out.println("ENCRYPTED: " + HexString.toHexNoSpaces(encrypted));

        System.out.println("Decrypting...");

        Cipher decCipher = Cipher.getInstance(JCSPRSA.RSA_NAME, JCSPRSA.PROVIDER_NAME);
        decCipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decrypted = decCipher.doFinal(encrypted, 0, encrypted.length);
        System.out.println("DECRYPTED: " + HexString.toHexNoSpaces(decrypted));

        if (DATA.length != decrypted.length) {
            throw new Exception("Invalid length of decrypted data.");
        } // if

        if (!Arrays.equals(DATA, decrypted)) {
            throw new Exception("Invalid decrypted data.");
        } // if

        System.out.println("Completed.");

    }

    /**
     * Запуск примера.
     *
     * Function runs the example.
     *
     * @param args Аргументы. Arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Добавление провайдеров.
        //
        // Adding java providers.

        JCPInit.initProviders(true); // JCSP по умолчанию. // JCSP is default.
        System.out.println("DATA: " + HexString.toHexNoSpaces(DATA));

        try {

            // Удаление тестового контейнера.
            //
            // Deleting of the test key container.

            KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE, JCSPRSA.PROVIDER_NAME);
            keyStore.load(null, null);
            keyStore.deleteEntry(ALIAS);

        } catch (Exception e) {}

        createKeyThenEncryptDecrypt();
        readKeyThenEncryptDecrypt();

    }

}
