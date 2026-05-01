/**
 * AESSimpleEncryptionExample.java,v $
 * version $
 * created 25.09.2020 12:37 by afevma
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

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Encoder;

import ru.CryptoPro.JCSP.JCSPRSA;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * This example demonstrates encryption and decryption
 * with AES key (JCSPRSA).
 *
 * Providers Java CSP RSA and CSP RSA are required.
 *
 */
public class AESSimpleEncryptionExample {

    public static String bytes2hex(byte[] bytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {

            String tmp = Integer.toHexString(0xFF & b);
            if (tmp.length() == 1) {
                tmp = "0" + tmp;
            }

            sb.append(tmp);

        }

        return sb.toString();

    }

    /**
     * Function encrypts and decrypts some data.
     *
     * @throws Exception
     */
    public static void encryptDecrypt() throws Exception {

        final String SECRET_KEY_ALGORITHM = JCSPRSA.AES_NAME; // secret key algorithm
        final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding"; // encryption algorithm
        final String PROVIDER = JCSPRSA.PROVIDER_NAME; // name of cryptographic provider
        final String MESSAGE = "Message for encryption and decryption"; // source data to be encrypted

        final byte[] DATA = MESSAGE.getBytes();

        System.out.println("Source message (HEX): " +
            bytes2hex(DATA));

        // Adding java providers.

        JCPInit.initProviders(true); // JCSP is default.

        // Creating initialization vector (IV) for encryption.
        // CBC mode is used, IV is required.

        final SecureRandom rnd = SecureRandom.getInstance(JCP.CP_RANDOM, JCSPRSA.PROVIDER_NAME);
        byte[] iv = new byte[16]; // for AES key with length 128

        rnd.nextBytes(iv);
        IvParameterSpec params = new IvParameterSpec(iv);

        // Generating a secret key (AES with length 128 bits).

        KeyGenerator kg = KeyGenerator.getInstance(
            SECRET_KEY_ALGORITHM, PROVIDER);

        kg.init(128); // key length
        SecretKey symmetricKey = kg.generateKey();

        // Encrypting data.

        Cipher encrypt = Cipher.getInstance(ENCRYPTION_ALGORITHM, PROVIDER);
        encrypt.init(Cipher.ENCRYPT_MODE, symmetricKey, params);

        byte[] encryptedMessage = encrypt.doFinal(DATA);
        System.out.println("Encrypted message (HEX): " + bytes2hex(encryptedMessage));

        // Decrypting data.

        Cipher decrypt = Cipher.getInstance(ENCRYPTION_ALGORITHM, PROVIDER);
        decrypt.init(Cipher.DECRYPT_MODE, symmetricKey, params);

        byte[] decryptedMessage = decrypt.doFinal(encryptedMessage);
        System.out.println("Decrypted message (HEX): " + bytes2hex(decryptedMessage));

        // Checking messages.

        if (DATA.length != decryptedMessage.length) {
            throw new Exception("Invalid length of encrypted or decrypted message.");
        } // if

        if (!Arrays.equals(DATA, decryptedMessage)) {
            throw new Exception("Invalid encrypted or decrypted message.");
        } // if

        System.out.println("Example completed successfully.");

    }

    /**
     * Example.
     *
     * @param args Arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        encryptDecrypt();
    }

}
