package CAdES.enveloped;

import CAdES.configuration.Configuration;
import CAdES.configuration.SimpleConfiguration;
import CAdES.configuration.container.EnvContainer2012_256;

import ru.CryptoPro.CAdES.BufferedEnvelopedSignature;
import ru.CryptoPro.CAdES.EncryptionKeyAlgorithm;

import ru.CryptoPro.JCP.tools.Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Пример демонстрирует использование класса BufferedEnvelopedSignature для зашифрования
 * данных фиксированной длины в сообщение формата Enveloped CMS.
 *
 */
public class BufferedEnvelopedCMSExample implements IEnvelopedData {

    /**
     * Данные для зашифрования.
     */
    private static final byte[] DATA = "Hello".getBytes(StandardCharsets.UTF_8);

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Ключ обмена и подписи на алгоритме ГОСТ 2012 (256) DH.
        SimpleConfiguration cfg = new SimpleConfiguration(new EnvContainer2012_256(), false);
        // Секретный ключ шифрования на алгоритме Магма. Структура - key_transport.
        EncryptionKeyAlgorithm keyAlg = EncryptionKeyAlgorithm.ekaMagmaMac;
        boolean key_trans = true;
        byte[] encrypted = encrypt(cfg, DATA, key_trans, keyAlg);
        System.out.println("----- ENVELOPED CMS BEGIN -----\n" + (new Encoder()).encode(encrypted) + "\n----- ENVELOPED CMS END -----");
        byte[] decrypted = decrypt(cfg, encrypted);
        checkResult(DATA, decrypted);
    }

    /**
     * Буферное зашифрование данных.
     *
     * @param configuration Параметры для расшифрования.
     * @param data Шифруемые данные.
     * @param transport True, если использовать key_transport, иначе key_agreement.
     * @param encryptionKeyAlgorithm Алгоритм ключа шифрования.
     * @return зашифрованное сообщение.
     * @throws Exception
     */
    private static byte[] encrypt(Configuration configuration, byte[] data, boolean transport, EncryptionKeyAlgorithm encryptionKeyAlgorithm) throws Exception {
        BufferedEnvelopedSignature signature = new BufferedEnvelopedSignature(encryptionKeyAlgorithm);
        if (transport) {
            signature.addKeyTransRecipient(configuration.getCertificate());
        } // if
        else {
            signature.addKeyAgreeRecipient(configuration.getCertificate());
        } // else
        return signature.encrypt(data);
    }

    /**
     * Расшифрование данных в буферном режиме.
     *
     * @param configuration Параметры для расшифрования.
     * @param encrypted Зашифрованное сообщение.
     * @return расшифрованное сообщение.
     * @throws Exception
     */
    private static byte[] decrypt(Configuration configuration, byte[] encrypted) throws Exception {
        BufferedEnvelopedSignature signature = new BufferedEnvelopedSignature(new ByteArrayInputStream(encrypted));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        signature.decrypt(null, configuration.getPrivateKey(), outStream);
        outStream.close();
        return outStream.toByteArray();
    }

    /**
     * Проверка соответствия двух сообщений.
     *
     * @param expected Ожидаемые данные.
     * @param actual Полученные данные.
     * @throws Exception
     */
    private static void checkResult(byte[] expected, byte[] actual) throws Exception {
        if (!Arrays.equals(expected, actual)) {
            throw new Exception("Data arrays atr not equal.");
        } // if
    }

}
