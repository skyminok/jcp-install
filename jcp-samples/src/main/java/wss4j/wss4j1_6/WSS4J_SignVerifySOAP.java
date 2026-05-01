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

/**
 * Class for signing & verifying of SOAP XML document.
 */

package wss4j.wss4j1_6;

import org.w3c.dom.Document;
import wss4j.manager.SignatureManager;
import wss4j.utility.SpecUtility;
import wss4j.wss4j1_6.manager.SOAPXMLSignatureManager_1_6;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Пример, описывающий создание и проверку подписи простого SOAP-сообщения
 * средствами wss4j.
 */
public class WSS4J_SignVerifySOAP {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws KeyStoreException, NoSuchAlgorithmException, 
	CertificateException, ClassNotFoundException {

		// Initialize JCP
		SpecUtility.initJCP();
		
		System.out.println("###### Example WSS4J_SignVerifySOAP 1.6.3 is begun ######");
		
		// Load key store
		SignatureManager manager = 
			new SOAPXMLSignatureManager_1_6(SpecUtility.DEFAULT_CRYPTO_PROPERTIES,
				SpecUtility.DEFAULT_ALIAS, SpecUtility.DEFAULT_PASSWORD);
		
		// Sign XML SOAP document
		Document signedDoc = manager.signDoc(manager.getMessage());
        String outputString = org.apache.ws.security.util.XMLUtils.PrettyDocumentToString(signedDoc);
        System.out.println("Signed document: ");
        System.out.println(outputString);
		
        // Verify signature in XML SOAP document
        boolean printCert = true;
        boolean result = manager.verifyDoc(signedDoc, printCert);
        System.out.println("\nVerified: " + result);
		
		System.out.println("###### Example WSS4J_SignVerifySOAP is finished ######");
	}
}
