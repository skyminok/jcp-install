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
package JCPxml.dsig.internal.utility;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit;

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * Class with auxiliary functions.
 */
public class SpecUtility {
	
	// All properties
	private static Properties props = new Properties();
	// Default properties. Alternative configuration is wss40 (+ our own variables related to
	// WSS4J files in /keys directory: 'wss40' is valid, 'wss40rev' is revoked)
	public static final String DEFAULT_CRYPTO_PROPERTIES =  System.getProperty("user.dir") +
            "/resources/crypto.properties";

	public static String DEFAULT_STORETYPE = JCP.HD_STORE_NAME;
	public static String DEFAULT_KEYSTORE = null;
	public static String DEFAULT_ALIAS = null;
	public static char[] DEFAULT_PASSWORD = null;
	public static String DEFAULT_CERT_FILE = null;
	public static String DEFAULT_CA_FILE = null;
	public static String DEFAULT_CRL_FILE = null;
	
	// Load all settings from crypto.properties
	static {
		
		try {

			try (FileInputStream is = new FileInputStream(DEFAULT_CRYPTO_PROPERTIES)) {
				props.load(is);
			}
			
			// Standard WSS4J variables
			DEFAULT_STORETYPE = props.getProperty("org.apache.ws.security.crypto.merlin.keystore.type");
			DEFAULT_KEYSTORE = props.getProperty("org.apache.ws.security.crypto.merlin.file");
			DEFAULT_ALIAS = props.getProperty("org.apache.ws.security.crypto.merlin.keystore.alias");
			
			String password = props.getProperty("org.apache.ws.security.crypto.merlin.keystore.password");
			if (password != null) {
				DEFAULT_PASSWORD = password.toCharArray();
			}
			
			// Our own variables
			DEFAULT_CERT_FILE = props.getProperty("cert.file");
			DEFAULT_CA_FILE = props.getProperty("ca.file");
			DEFAULT_CRL_FILE = props.getProperty("crl.file");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	/**
	 * Functions initializes XML JCP.
	 */
	public static void initJCP() {
		
		if(!JCPXMLDSigInit.isInitialized())
    		JCPXMLDSigInit.init();
		
		System.setProperty("com.ibm.security.enableCRLDP", "false");
	}

	/**
	 * Function loads store information from key store.
	 * @param storeType - type of key store (HDImageStore, JKS, PKCS12).
	 * @param store - store file.
	 * @param storePassword - password to store.
	 * @return loaded key store.
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static KeyStore loadKeyStore( String storeType, File store, char[] storePassword ) 
	throws KeyStoreException, NoSuchAlgorithmException, CertificateException, 
		FileNotFoundException, IOException {

		KeyStore keyStore = KeyStore.getInstance(storeType);
		FileInputStream inputStream = null;

		if (store != null) {
			inputStream = new FileInputStream(store);
        }

		try (InputStream is = inputStream) {
			keyStore.load(is, storePassword);
		}

		return keyStore;		 
	}
}
