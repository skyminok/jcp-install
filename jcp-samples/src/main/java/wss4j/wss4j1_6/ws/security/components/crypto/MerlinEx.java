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

package wss4j.wss4j1_6.ws.security.components.crypto;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.util.Properties;

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Merlin;
import ru.CryptoPro.JCP.JCP;

/*
 * This class extends default class Merlin to cache a private key and to avoid permanent
 * reading of key from store.
 */
public class MerlinEx extends Merlin {

    private PrivateKey cachedPrivateKey = null;
    private String cachedAlias = null;
	
	public MerlinEx(){
		super();
		System.out.println("Using MerlinEx");
	}

	public MerlinEx(Properties properties) throws CredentialException,
			IOException {
		super(properties);
		System.out.println("Using MerlinEx (Properties)");
	}
	
	public MerlinEx(Properties properties, ClassLoader loader) throws CredentialException,
			IOException {
		super(properties, loader);
		System.out.println("Using MerlinEx (Properties, ClassLoader)");
		String keyStoreType = properties.getProperty(KEYSTORE_TYPE);
		if (keyStoreType != null)
			keyStoreType = keyStoreType.trim();

		if (keyStoreType.equalsIgnoreCase(JCP.HD_STORE_NAME)) {
			KeyStore keyStore = null;
			try {
				keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
				keyStore.load(null, null);
			} catch (KeyStoreException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (CertificateException e) {
				e.printStackTrace();
			}
			this.keystore = keyStore;
		}

	}


	public PrivateKey getPrivateKey(String alias, String password) {
    	
    	if (cachedPrivateKey == null || 
    			(cachedAlias != null && !cachedAlias.equalsIgnoreCase(alias))) {
    		cachedAlias = alias;
    		try {
				cachedPrivateKey = super.getPrivateKey(alias, password);
			} catch (WSSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	return cachedPrivateKey;
    }
}
