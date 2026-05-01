/**
 * PFXStoreImportExportExample.java,v $
 * version $
 * created 11.12.2020 18:33 by afevma
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
package JCSP.Container;

import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;
import ru.CryptoPro.JCP.tools.AlgorithmTools;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCP.tools.Encoder;

import ru.CryptoPro.JCSP.JCSP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * Example of import of certificates and keys from PFX file,
 * and export to PFX file.
 *
 * Algorithm: GOST
 * Provider: Java CSP
 *
 * Example requires a PFX file {@link #PFX_IN_FILE} with
 * password {@link #PFX_PASSWORD}.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class PFXStoreImportExportExample {

    public static final String PROVIDER = JCSP.PROVIDER_NAME; // provider name
    public static final byte[] SIGN_DATA = "security".getBytes(); // signed data
    public static final String PFX_IN_FILE = System.getProperty("user.dir") + File.separator + "import_test.pfx"; // pfx for import
    public static final String PFX_OUT_FILE = System.getProperty("user.dir") + File.separator + "export_test.pfx"; // pfx name for export
    public static final char[] PFX_PASSWORD = "123456".toCharArray(); // pfx password
    public static final String EXPORT_ALIAS = "export_key"; // exported key
    public static final char[] EXPORT_PASSWORD = "654321".toCharArray(); // exported key's password

    public static final int FLAG_OPEN_KEY   = 0x0010; // open using getKey
    public static final int FLAG_OPEN_ENTRY = 0x0020; // open using getEntry

    /**
     * Example.
     *
     * @param args Arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // Adding java providers. Java CSP is default.
        JCPInit.initProviders(true);

        importFromPfx();
        exportToPfx();

    }

    /**
     * Signing with a private key.
     *
     * The function may:
     * * work successfully if the key does not have a password;
     * * ask the password using CSP dialog and work successfully if entered password is valid, or fail otherwise;
     * * cause errors if the key is unavailable or has invalid state (access denied etc.).
     *
     * @param privateKey Private key.
     * @param provider Provider name.
     * @return binary signature.
     * @throws Exception
     */
    public static byte[] testSign(PrivateKey privateKey,
        String provider) throws Exception {

        String sigAlgorithm = AlgorithmTools.getSignatureAlgorithmByPrivateKey(privateKey);
        System.out.println("\tSigning with algorithm " + sigAlgorithm + "...");

        Signature signature = Signature.getInstance(sigAlgorithm, provider);

        signature.initSign(privateKey);
        signature.update(SIGN_DATA);

        byte[] sign = signature.sign();
        System.out.println("\tSignature: " + (new Encoder()).encode(sign));

        return sign;

    }

    /**
     * Verifying with public key. Failure if the
     * signature is invalid.
     *
     * @param publicKey Public key.
     * @param provider Provider name.
     * @param sign binary signature.
     * @throws Exception
     */
    public static void testVerify(PublicKey publicKey,
        String provider, byte[] sign) throws Exception {

        String sigAlgorithm = AlgorithmTools.getSignatureAlgorithmByPrivateKey(publicKey);
        System.out.println("\tVerifying with algorithm " + sigAlgorithm + "...");

        Signature signature = Signature.getInstance(sigAlgorithm, provider);

        signature.initVerify(publicKey);
        signature.update(SIGN_DATA);

        boolean verified = signature.verify(sign);

        if (!verified) {
            throw new Exception("Invalid signature.");
        }

        System.out.println("\tSignature has been verified.");

    }

    /**
     * Check a key store: reading, signing, verifying, saving
     * to another key container.
     * System store can disable some options.
     *
     * @param keyStore Loaded key store.
     * @param signingCheck True, if key check is required (signing
     * and verifying).
     * @param openFlags Flags that say how to read key.
     * @param provider Provider name.
     * @param exportStoreType Store type for exporting key (the
     * first found).
     * @param exportAlias Alias of exporting key.
     * @param exportPassword Password for exporting key.
     * @throws Exception
     */
    public static void checkStore(KeyStore keyStore, boolean signingCheck,
        int openFlags, String provider, String exportStoreType, String
        exportAlias, char[] exportPassword) throws Exception {

        System.out.println("Starting check of the store...");
        Enumeration<String> aliases = keyStore.aliases();

        System.out.println("Items have been found...");
        boolean firstItemForExport = true;

        while (aliases.hasMoreElements()) {

            String alias = aliases.nextElement();
            System.out.println("%% ALIAS = " + alias);

            boolean isKeyEntry = keyStore.isKeyEntry(alias);
            System.out.println("\tIs key entry: " + isKeyEntry);

            boolean isCertificateEntry = keyStore.isCertificateEntry(alias);
            System.out.println("\tIs certificate entry: " + isCertificateEntry);

            PrivateKey privateKey = null;
            Certificate[] chain = null;

            if (isKeyEntry) { // certificate & key

                // Variant 1.

                if ((openFlags & FLAG_OPEN_KEY) != 0) { // use key from getKey

                    // Password may be asked later on signing.

                    privateKey = (PrivateKey) keyStore.getKey(alias, null); // without password if PFX
                    System.out.println("\tPrivate key: " + privateKey);

                } // if
                else {
                    System.out.println("\tReading of key is disabled.");
                } // else

                Certificate certificate = keyStore.getCertificate(alias);
                byte[] signature = null;

                if (signingCheck && ((openFlags & FLAG_OPEN_KEY) != 0)) { // if check is required and key has been read
                    signature = testSign(privateKey, provider);
                } // if
                else {
                    System.out.println("\tSigning check is disabled or key has not been read.");
                } // else

                if (certificate != null) {

                    System.out.println("\tCertificate: " + ((X509Certificate)certificate).getSubjectDN());

                    chain = new Certificate[1];
                    chain[0] = certificate;

                    if (signingCheck && (signature != null)) { // if check is required and signature has been created
                        testVerify(certificate.getPublicKey(), provider, signature);
                    } // if

                } // if

                chain = keyStore.getCertificateChain(alias);

                // Variant 2.

                if ((openFlags & FLAG_OPEN_ENTRY) != 0) { // use key from getEntry

                    // Password is required immediately.

                    JCPProtectionParameter parameter = new JCPProtectionParameter(null, true, true); // without password if PFX
                    JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) keyStore.getEntry(alias, parameter);

                    privateKey = entry.getPrivateKey();
                    chain = entry.getCertificateChain();

                } // if
                else {
                    System.out.println("\tReading of entry is disabled.");
                } // else

                if (signingCheck && ((openFlags & FLAG_OPEN_ENTRY) != 0)) { // if check is required and entry has been read
                    signature = testSign(privateKey, provider);
                } // if
                else {
                    System.out.println("\tSigning check is disabled or entry has not been read.");
                } // else

                if (chain != null && chain.length > 0) {

                    System.out.println("\tCertificate[0] from chain: " + ((X509Certificate)chain[0]).getSubjectDN());

                    if (signingCheck && (signature != null)) { // if check is required and signature has been created
                        testVerify(chain[0].getPublicKey(), provider, signature);
                    } // if

                } // if

            } // if

            if (isCertificateEntry) { // certificate only

                // Variant 1.

                Certificate certificate = keyStore.getCertificate(alias);

                if (certificate != null) {
                    System.out.println("\tCertificate only: " + ((X509Certificate) certificate).getSubjectDN());
                } // if

                // Variant 2.

                KeyStore.TrustedCertificateEntry entry = (KeyStore.TrustedCertificateEntry) keyStore.getEntry(alias, null);

                if (entry != null && entry.getTrustedCertificate() != null) {
                    System.out.println("\tTrusted certificate only: " + ((X509Certificate) entry.getTrustedCertificate()).getSubjectDN());
                } // if

                // Variant 3.

                chain = keyStore.getCertificateChain(alias);

                if (chain != null && chain.length > 0) {
                    System.out.println("\tCertificate[0] only from chain: " + ((X509Certificate) chain[0]).getSubjectDN());
                } // if

            } // if

            // Take an item and export its key and certificates
            // to the HDIMAGE storage.

            if (firstItemForExport && isKeyEntry && exportStoreType != null &&
                privateKey != null && chain != null && chain.length > 0) {

                KeyStore exportKeyStore = KeyStore.getInstance(exportStoreType, provider);
                exportKeyStore.load(null, null);

                try {

                    System.out.println("Deleting " + exportAlias);
                    exportKeyStore.deleteEntry(exportAlias); // deleting previous key if exists, for test only

                } catch (Exception e) {}

                System.out.println("Saving " + exportAlias);

                JCPProtectionParameter parameter = new JCPProtectionParameter(exportPassword);
                JCPPrivateKeyEntry entry = new JCPPrivateKeyEntry(privateKey, chain);

                exportKeyStore.setEntry(exportAlias, entry, parameter);
                firstItemForExport = false; // do not export anymore

            } // if
            else {
                System.out.println("Ignore export for " + alias);
            } // else

            // Also another functions can be used as setCertificateEntry etc.

        } // while

        System.out.println("Check completed.");

    }

    /**
     * Import of PFX to a key container: reading, signing,
     * verifying, saving to another key container. It uses
     * both version of reading the key: getKey and getEntry.
     *
     * @throws Exception
     */
    public static void importFromPfx() throws Exception {

        System.out.println("Starting import from " + PFX_IN_FILE);
        KeyStore keyStore = KeyStore.getInstance(JCSP.PFX_STORE_NAME, PROVIDER);

        try (FileInputStream is = new FileInputStream(PFX_IN_FILE)) {
            keyStore.load(is, PFX_PASSWORD);
        }

        checkStore(
            keyStore,
            true,
            FLAG_OPEN_KEY | FLAG_OPEN_ENTRY, // PFX does not need a password for key or entry, so we use both methods
            PROVIDER,
            JCSP.HD_STORE_NAME, // export first found key to the disk
            EXPORT_ALIAS,
            EXPORT_PASSWORD
        );

        System.out.println("Import completed.");

    }

    /**
     * Export of previously saved key container to
     * a new PFX file.
     *
     * @throws Exception
     */
    public static void exportToPfx() throws Exception {

        System.out.println("Starting export to " + PFX_OUT_FILE);
        KeyStore keyStore = KeyStore.getInstance(JCSP.HD_STORE_NAME, PROVIDER);

        keyStore.load(null, null);
        System.out.println("Reading key store...");

        JCPProtectionParameter parameter = new JCPProtectionParameter(EXPORT_PASSWORD);
        JCPPrivateKeyEntry entry = (JCPPrivateKeyEntry) keyStore.getEntry(EXPORT_ALIAS, parameter);

        KeyStore exportKeyStore = KeyStore.getInstance(JCSP.PFX_STORE_NAME, PROVIDER);

        exportKeyStore.load(null, null);
        System.out.println("Exporting...");

        exportKeyStore.setEntry(EXPORT_ALIAS, entry, null); // without password
        try (FileOutputStream os = new FileOutputStream(PFX_OUT_FILE)) {
            exportKeyStore.store(os, PFX_PASSWORD);
        }

        byte[] pfx = Array.readFile(PFX_OUT_FILE);
        System.out.println("PFX:\n" + (new Encoder()).encode(pfx));

        System.out.println("Export completed.");

    }

}
