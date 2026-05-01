/**
 * JKSStoreImportExportExample.java,v $
 * version $
 * created 11.12.2020 18:32 by afevma
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

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;

import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.JCPProtectionParameter;

import ru.CryptoPro.JCSP.JCSP;
import ru.CryptoPro.JCSP.JCSPRSA;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Example of import of certificates and keys from JKS file.
 *
 * Algorithm: RSA
 * Provider: Java CSP RSA
 *
 * Example requires a JKS file {@link #JKS_PATH} with
 * alias {@link #EXPORT_ALIAS} and password {@link #JKS_PASSWORD}.
 * It will be generated automatically using bouncycastle (BC)
 * provider.
 *
 * @author Copyright 2004-2020 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class JKSStoreImportExportExample {

    public static final String PROVIDER = JCSPRSA.PROVIDER_NAME; // provider name
    public static final String JKS_PATH = System.getProperty("user.dir") + File.separator + "import_test.jks"; // jks for import
    public static final String JKS_ALIAS = "jks_key_gen"; // jks key
    public static final char[] JKS_PASSWORD = "123456".toCharArray(); // jks password
    public static final String EXPORT_ALIAS = "export_jks_key"; // exported key
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

        generateJKS();
        importFromJKSAndExportToHD();

    }

    /**
     * Generating a JKS with RSA key and certificates.
     * It uses bouncycastle provider.
     *
     * @throws Exception
     */
    public static void generateJKS() throws Exception {

        Security.addProvider(new BouncyCastleProvider());
        System.out.println("Generating JKS using BC...");

        try {

            System.out.println("Initiating generator...");

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(1024);

            System.out.println("Generating pair...");
            KeyPair pair = keyPairGenerator.generateKeyPair();

            System.out.println("Preparing params...");

            AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
            AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

            System.out.println("Preparing keys...");

            AsymmetricKeyParameter privateKeyAsymKeyParam = PrivateKeyFactory.createKey(pair.getPrivate().getEncoded());
            SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(pair.getPublic().getEncoded());

            System.out.println("Building signer...");
            ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(privateKeyAsymKeyParam);

            System.out.println("Adding common name and dates...");
            X500Name name = new X500Name("CN=" + JKS_ALIAS);

            Date from = new Date();
            Date to = new Date(from.getTime() + 30 * 86400000L);

            BigInteger sn = new BigInteger(64, new SecureRandom());
            System.out.println("Preparing certificate generator...");

            X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder(name, sn, from, to, name, subPubKeyInfo);
            System.out.println("Building certificate...");

            X509CertificateHolder certificateHolder = v1CertGen.build(sigGen);
            X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certificateHolder);

            System.out.println("Loading key store...");
            KeyStore keyStore = KeyStore.getInstance("JKS");

            keyStore.load(null, null);
            System.out.println("Adding entry...");

            keyStore.setKeyEntry(JKS_ALIAS, pair.getPrivate(), JKS_PASSWORD, new Certificate[] {cert});
            System.out.println("Saving key store...");

            try (FileOutputStream os = new FileOutputStream(JKS_PATH)) {
                keyStore.store(os, JKS_PASSWORD);
            }

            System.out.println("Completed.");

        } finally {
            Security.removeProvider("BC");
        }

    }

    /**
     * Import of JKS and saving to a key container with
     * check by signing and signature verifying.
     *
     * @throws Exception
     */
    public static void importFromJKSAndExportToHD() throws Exception {

        System.out.println("Importing JKS store...");
        KeyStore jksKeyStore = KeyStore.getInstance("JKS");

        try (FileInputStream is = new FileInputStream(JKS_PATH)) {
            jksKeyStore.load(is, JKS_PASSWORD);
        }

        KeyStore.ProtectionParameter jksParameter = new KeyStore.PasswordProtection(JKS_PASSWORD);
        KeyStore.PrivateKeyEntry jksEntry = (KeyStore.PrivateKeyEntry) jksKeyStore.getEntry(JKS_ALIAS, jksParameter);

        PrivateKey privateKey = jksEntry.getPrivateKey();
        Certificate[] certificates = jksEntry.getCertificateChain();

        System.out.println("%% JKS private key: " + privateKey);
        System.out.println("%% JKS certificates");

        for (Certificate cert : certificates) {
            System.out.println("\tCertificate: " + ((X509Certificate)cert).getSubjectDN());
        } // for

        System.out.println("Import completed. Saving to the disk...");
        KeyStore hdKeyStore = KeyStore.getInstance(JCSP.HD_STORE_NAME, PROVIDER);

        System.out.println("Reading...");
        hdKeyStore.load(null, null);

        try {

            System.out.println("Deleting " + EXPORT_ALIAS);
            hdKeyStore.deleteEntry(EXPORT_ALIAS); // deleting previous key if exists, for test only

        } catch (Exception e) {}

        System.out.println("Saving...");
        JCPProtectionParameter hdParameter = new JCPProtectionParameter(EXPORT_PASSWORD);

        hdKeyStore.setEntry(EXPORT_ALIAS, jksEntry, hdParameter);
        System.out.println("Saving completed. Trying to check...");

        JCPPrivateKeyEntry hdEntry = (JCPPrivateKeyEntry) hdKeyStore.getEntry(EXPORT_ALIAS, hdParameter);

        privateKey = hdEntry.getPrivateKey();
        certificates = hdEntry.getCertificateChain();

        System.out.println("%% HD private key: " + privateKey);
        System.out.println("%% HD certificates");

        for (Certificate cert : certificates) {
            System.out.println("\tCertificate: " + ((X509Certificate)cert).getSubjectDN());
        } // for

        byte[] signature = PFXStoreImportExportExample.testSign(privateKey, PROVIDER);
        PFXStoreImportExportExample.testVerify(certificates[0].getPublicKey(), PROVIDER, signature);

        System.out.println("Check completed.");

    }

}
