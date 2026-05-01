package JCSP.Digest;

import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Encoder;
import ru.CryptoPro.JCSP.JCSPRSA;

import java.security.MessageDigest;

/**
 * Пример получения хеша сообщения с помощью провайдера JCSP.
 */
public class RsaDigestExample {

    /**
     * Хеширование сообщения.
     *
     * @param algName Алгоритм хеширования.
     * @throws Exception
     */
    public static void digest(String algName) throws Exception {
        final String message = "Message for digest";
        final MessageDigest messageDigest =
                MessageDigest.getInstance(algName, JCSPRSA.PROVIDER_NAME);

        messageDigest.update(message.getBytes());
        final byte[] digest = messageDigest.digest();

        Encoder encoder = new Encoder();
        System.out.println("Digest: " + encoder.encode(digest) +
                "\nfor Message: " + message);
    }

    public static void main(String[] args) throws Exception {

        JCPInit.initProviders(true);

        // RIPEMD160
        digest(JCSPRSA.DIGEST_RIPEMD160);

    }
}
