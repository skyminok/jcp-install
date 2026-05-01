/**
 * Copyright 2004-2012 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.Signature;

import JCSP.Container.IContainers;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Encoder;
import ru.CryptoPro.JCSP.JCSP;

import java.security.*;

/**
 * Пример подписью и проверки подписи с помощью
 * криптопровайдера JCSP. Ключи читаются из контейнера.
 */
public class SignByLoadKeyExample implements IContainers {

    /**
     * Подпись и проверка подписи.
     *
     * @param signAlgName Алгоритм подписи.
     * @param alias Алиас ключа.
     * @param password Пароль к ключу.
     * @param askPinInWindow True, если вводить пароль нужно
     * в окне CSP.
     * @throws Exception
     */
    public static void signAndVerify(String signAlgName, String alias,
        char[] password, boolean askPinInWindow) throws Exception {

        // Сообщение для подписи.
        final String message = "Message for signature";

        KeyStore keyStore = KeyStore.getInstance(JCSP.HD_STORE_NAME,
            JCSP.PROVIDER_NAME);
        keyStore.load(null, null);

        PrivateKey privateKey;
        PublicKey publicKey;

        if (askPinInWindow) {
            privateKey = (PrivateKey) keyStore.getKey(alias, null);
            publicKey = keyStore.getCertificate(alias).getPublicKey();
        } // if
        else {

            KeyStore.ProtectionParameter protectedParam =
                new KeyStore.PasswordProtection(password);

            JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry)
                keyStore.getEntry(alias, protectedParam);

            privateKey = entry.getPrivateKey();
            publicKey = entry.getCertificate().getPublicKey();

        } // else

        Signature signer = Signature.getInstance(
            signAlgName, JCSP.PROVIDER_NAME);

        signer.initSign(privateKey);
        signer.update(message.getBytes());

        byte[] signature = signer.sign();

        Encoder encoder = new Encoder();
        System.out.println("Signature: " + encoder.encode(signature) +
            "\nfor Message: " + message);

        Signature validator = Signature.getInstance(
            signAlgName, JCSP.PROVIDER_NAME);

        validator.initVerify(publicKey);
        validator.update(message.getBytes());

        System.out.println("Signature verified: " +
            validator.verify(signature));

    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        JCPInit.initProviders(true);

        // False, если не хотим вводить пин-код в окне CSP.
        boolean askPinInWindow = false;

        // ГОСТ Р 34.10-2001 DH
        // signAndVerify(JCP.CRYPTOPRO_SIGN_NAME, ALIAS_01,
        //     PASSWORD_01, askPinInWindow);

        // ГОСТ Р 34.10-2012 (256) DH
        signAndVerify(JCP.CRYPTOPRO_SIGN_2012_256_NAME,
            ALIAS_2012_256, PASSWORD_2012_256, askPinInWindow);

        // ГОСТ Р 34.10-2012 (512) DH
        signAndVerify(JCP.CRYPTOPRO_SIGN_2012_512_NAME,
            ALIAS_2012_512, PASSWORD_2012_512, askPinInWindow);

    }

}
