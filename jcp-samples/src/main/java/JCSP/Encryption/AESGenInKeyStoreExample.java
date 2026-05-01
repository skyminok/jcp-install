/**
 * AESGenInKeyStoreExample.java,v $
 * version $
 * created 28.09.2020 13:48 by afevma
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

import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.AlgIdInterface;
import ru.CryptoPro.JCP.params.AlgIdSpecForeign;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.spec.NameAlgIdSpecForeign;
import ru.CryptoPro.JCSP.JCSPRSA;
import ru.CryptoPro.JCSP.params.PasswordParamsSpec;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyStore;

/**
 * This example demonstrates generating of
 * AES key (JCSPRSA).
 *
 * Providers Java CSP RSA and CSP RSA are required.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class AESGenInKeyStoreExample {

    /**
     * Functions generates AES key (128).
     *
     * @param keyStoreType Key store type.
     * @param alias Key alias.
     * @param password Key password.
     * @param in_store True if key should be
     * generated in the storage without saving.
     * @throws Exception
     */
    public static void generate(String keyStoreType,
        String alias, char[] password, boolean in_store)
        throws Exception {

        System.out.println("Generating...");

        final String SECRET_KEY_ALGORITHM = JCSPRSA.AES_NAME; // secret key algorithm
        final String PROVIDER = JCSPRSA.PROVIDER_NAME; // name of cryptographic provider

        KeyGenerator kg = KeyGenerator.getInstance(SECRET_KEY_ALGORITHM, PROVIDER);
        kg.init(128); // key length

        if (in_store) { // immediately in the destined storage

            System.out.println("In store...");

            String containerPath = "\\\\.\\" + keyStoreType + "\\" + alias; // full path
            AlgIdInterface containerParam = new NameAlgIdSpecForeign(containerPath);
            kg.init(containerParam);

            PasswordParamsSpec passwordParam = new PasswordParamsSpec(password);
            kg.init(passwordParam); // password

        } // if

        SecretKey symmetricKey = kg.generateKey();
        System.out.println(symmetricKey);

        if (in_store) { // the key has been created in the storage

            System.out.println("Completed.");
            return;

        } // if

        // The key has been created in memory.

        System.out.println("Copying from memory...");

        KeyStore keyStore = KeyStore.getInstance(
            keyStoreType, JCSPRSA.PROVIDER_NAME);

        keyStore.load(null, null);

        JCPProtectionParameter parameter = new
            JCPProtectionParameter(password);

        KeyStore.SecretKeyEntry secretKeyEntry = new
            KeyStore.SecretKeyEntry(symmetricKey);

        keyStore.setEntry(alias, secretKeyEntry, parameter);
        System.out.println("Saved.");

    }

    /**
     * Example.
     *
     * @param args Arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (args.length < 4) {
            System.err.println("Usage: store-type alias password in-store\n" +
                "if no password then use null, in-store = true or false (default: false).");
            return;
        } // if

        String keyStoreType = args[0];
        String alias = args[1];
        String password = args[2];
        String inStore = args[3];

        if (password.equalsIgnoreCase("null")) {
            password = null;
        } // if

        boolean in_store = inStore.equalsIgnoreCase("true");

        // Adding java providers.

        JCPInit.initProviders(true); // JCSP is default.

        generate(keyStoreType, alias, (password != null)
            ? password.toCharArray() : null, in_store);

    }

}
