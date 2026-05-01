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

/*
 * Class provides generating and verifying of signature.
 */

package wss4j.wss4j1_6.manager;

import org.apache.axis.message.SOAPEnvelope;
import org.apache.ws.security.*;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecSignature;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import ru.CryptoPro.JCP.JCP;
import wss4j.manager.SignatureManager;
import wss4j.utility.SOAPUtility;
import wss4j.utility.SpecUtility;
import wss4j.wss4j1_6.ws.security.components.crypto.MerlinEx;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Properties;

/**
 * Класс, реализующий функции подписи и проверки подписи средствами wss4j
 *  (WSSecSignature - для подписи и WSSecurityEngine - для проверки).
 *  Для ускорения работы может быть использован класс wss4j.wss4j1_6.ws.security.components.crypto.MerlinEx.
 *
 */
public class SOAPXMLSignatureManager_1_6 extends SignatureManager {

	private Crypto crypto = null;
	private String alias;
	private char[] keyPassword = null;
	private Properties props = new Properties();

    static {

        // Initialize JCP
        com.sun.org.apache.xml.internal.security.Init.init();
        SpecUtility.initJCP();

        // Load CryptoPro XMLDSig service provider
        Security.insertProviderAt(new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI(), 1);

        // Override methods
        Security.getProvider("XMLDSig").put("XMLSignatureFactory.DOM",
                "ru.CryptoPro.JCPxml.dsig.internal.dom.DOMXMLSignatureFactory");
        Security.getProvider("XMLDSig").put("KeyInfoFactory.DOM",
                "ru.CryptoPro.JCPxml.dsig.internal.dom.DOMKeyInfoFactory");
    }

	public SOAPXMLSignatureManager_1_6(String propertyFile, String alias, char[] password)
	throws ClassNotFoundException, KeyStoreException, NoSuchAlgorithmException, CertificateException {

        props.clear();
		this.alias = alias;
		this.keyPassword = password;
		
		try {

			// Load properties (store type, key alias etc)
			props.load(new FileInputStream(propertyFile));

			// Create object (Merlin) to sign and verify SOAP XML messages. It uses crypto.properties and
			// key store must to contain certificate and key

            KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
            keyStore.load(null, null);

			//загрузка класса Merlin (либо MerlinEx)
			Merlin merlin = null;
			if (SOAPUtility.is_MerlinEx(SpecUtility.MERLIN_PROVIDER ))
				merlin = new MerlinEx();
			else
				merlin = new Merlin();
            merlin.setKeyStore(keyStore);
			crypto = merlin;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	/**
	 * Function signs XML SOAP document.
	 * @param docStr - XML SOAP string.
	 * @return signed document.
	 */
	public Document signDoc(String docStr) {
		
		Document signedDoc = null;
		
		try {
			SOAPEnvelope unsignedEnvelope = SOAPUtility.getSOAPEnvelopeFromString(docStr);
			// Prepare object to sign secured message
			WSSConfig.setAddJceProviders(false);
			WSSConfig config = new WSSConfig();
			config.setWsiBSPCompliant(false);

			WSSecSignature sign = new WSSecSignature();
			sign.setWsConfig(config);

			// Set properties: alias, password, algorithm etc.
			String pswrd = null;
			if (keyPassword != null) {
				pswrd = new String(keyPassword);
			}
	        sign.setUserInfo(alias, pswrd);
	        
	        sign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);
	        sign.setSignatureAlgorithm(ru.CryptoPro.JCPxml.Consts.URI_GOST_SIGN);
	        sign.setDigestAlgo(ru.CryptoPro.JCPxml.Consts.URI_GOST_DIGEST);

	        Document sourceDoc = unsignedEnvelope.getAsDocument();

			WSSecHeader secHeader = new WSSecHeader();
			secHeader.setActor("http://smev.gosuslugi.ru/actors/smev");
			secHeader.setMustUnderstand(true);
	        secHeader.insertSecurityHeader(sourceDoc);

	        // Sign document
	        signedDoc = sign.build(sourceDoc, crypto, secHeader);
			
		} catch (Exception e) {
			System.out.println("Error due sign document");
			e.printStackTrace();
		}
		
		return signedDoc;
		
	}
	
	/**
	 * Function verifies a signature in SOAP XML document.
	 * @param signedDoc - verifiable SOAP XML document with signature.
	 * @param printCert - option to print certificate.
	 * @return object with result.
	 * @throws Exception
	 */
	public boolean verifyDoc(Document signedDoc, boolean printCert) {
		
		boolean result = false;
		
		if (signedDoc == null)
			return false;
		
		try {

			WSSConfig.setAddJceProviders(false);
			WSSConfig config = new WSSConfig();

			config.setWsiBSPCompliant(false);

			WSSecurityEngine engine = new WSSecurityEngine();
			engine.setWssConfig(config);


			// Verify signature
			List<WSSecurityEngineResult> results = engine.processSecurityHeader(signedDoc, "http://smev.gosuslugi.ru/actors/smev" , null, crypto);
			// Ensure actionResult != null
			WSSecurityEngineResult actionResult = WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
			result = (actionResult != null);
			
			// Print signer cerificate
			if (printCert && actionResult != null) {
				System.out.println(actionResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE));
			}
			
		} catch (WSSecurityException e) {
			System.out.println("Error due verify document");
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Error due verify document");
			e.printStackTrace();
		}
		
		return result;
	}

	/**
	 * Function returns SOAP message.
	 */
	public String getMessage() {
		return SOAPUtility.SOAPMSG;
	}
}
