/**
 * SystemStoreExample.java,v $
 * version $
 * created 02.02.2021 14:00 by afevma
 * last modified $ by $
 * (C) ООО Крипто-Про 2004-2021.
 * <p>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 * <p>
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package JCSP.Container;

import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.KeyStore.FILE;

import java.io.File;
import java.security.KeyStore;

/**
 * Example of working with system stores and SST
 * (serialized store of certificates).
 *
 * Algorithm: GOST
 * Provider: Java CSP
 *
 * @author Copyright 2004-2021 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class SystemStoreExample {

    public static final String SST_FILE_PATH = System.getProperty("user.dir") + File.separator + "test.sst"; // path to sst (serialized store) file
    public static final String PROVIDER = JCSP.PROVIDER_NAME; // provider name
    public static final String EXPORT_ALIAS = "export_system_key"; // exported key
    public static final char[] EXPORT_PASSWORD = "654321".toCharArray(); // exported key's password

    /**
     * Example.
     *
     * @param args Arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Adding java providers. Java CSP is default.

        JCPInit.initProviders(true);

        // System stores.

        checkSystemStore(JCSP.MY_STORE_NAME);
        checkSystemStore(JCSP.CA_STORE_NAME);
        checkSystemStore(JCSP.ROOT_STORE_NAME);
        checkSystemStore(JCSP.ADDRESS_BOOK_STORE_NAME);

        // File store for certificates (SST - serialized store).

        checkFileStore();

    }

    /**
     * Reading a system store and check it: aliases, states,
     * creating and verifying signature with every found key.
     *
     * Attention! This function calls every key it finds and
     * asks its password using special CSP dialog (through getKey).
     * It does not use getEntry because we do not know a required
     * password in advance.
     *
     * @param storeType System store type.
     * @throws Exception
     */
    public static void checkSystemStore(String storeType) throws Exception {

        System.out.println("Reading system store: " + storeType);

        KeyStore keyStore = KeyStore.getInstance(storeType, JCSP.PROVIDER_NAME);
        keyStore.load(null, null);

        // The call may cause errors during access to private keys.

        PFXStoreImportExportExample.checkStore(
            keyStore,
            true,
            PFXStoreImportExportExample.FLAG_OPEN_KEY, // we need a dialog to enter the password, so we use only getKey
            PROVIDER,
            JCSP.HD_STORE_NAME, // export first found key to the disk
            EXPORT_ALIAS,
            EXPORT_PASSWORD
        );

        System.out.println("Reading completed.");

    }

    /**
     * Reading a SST store and check it: aliases, states.
     * Keys are ignored if found as private links for
     * certificates.
     *
     * @throws Exception
     */
    public static void checkFileStore() throws Exception {

        // Variant 1, no parameters.

        System.out.println("Reading [1] SST store: " + SST_FILE_PATH);
        KeyStore keyStore = FILE.Builder.newInstance(SST_FILE_PATH, null).getKeyStore();

        PFXStoreImportExportExample.checkStore(
            keyStore,
            false,
            0, // we do not need a key
            PROVIDER,
            null, // we do not need export
            null,
            null
        );

        System.out.println("Reading [1] completed.");

        // Variant 2, using key store parameters.

        System.out.println("Reading [2] SST store: " + SST_FILE_PATH);

        FILE.FileStoreProtection protection = new FILE.FileStoreProtection(SST_FILE_PATH, null);
        FILE.FileLoadStoreParameter parameter = new FILE.FileLoadStoreParameter(protection);

        keyStore = KeyStore.getInstance(JCSP.FILE_STORE_NAME, PROVIDER);
        keyStore.load(parameter);

        PFXStoreImportExportExample.checkStore(
            keyStore,
            false,
            0, // we do not need a key
            PROVIDER,
            null, // we do not need export
            null,
            null
        );

        System.out.println("Reading [2] completed.");

    }

}
