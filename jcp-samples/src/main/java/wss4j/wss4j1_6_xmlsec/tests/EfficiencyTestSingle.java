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

package wss4j.wss4j1_6_xmlsec.tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import wss4j.manager.SignatureManager;
import wss4j.manager.TestManager;
import wss4j.manager.TestManagerSingle;
import wss4j.utility.SpecUtility;
import wss4j.wss4j1_6_xmlsec.manager.SOAPXMLSignatureManager_1_6_xmlsec;

/*
 * Test provides one main thread for signing and verifying of SOAP
 * XML documents as one operation (simple circle).
 */
public class EfficiencyTestSingle {

	private static final int CIRCLE_COUNT = 1000;
	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * @throws UnrecoverableKeyException 
	 */
	public static void main(String[] args) throws KeyStoreException, NoSuchAlgorithmException, 
	CertificateException, ClassNotFoundException, UnrecoverableKeyException, 
	FileNotFoundException, IOException {

		SignatureManager signatureManager = 
			new SOAPXMLSignatureManager_1_6_xmlsec(SpecUtility.DEFAULT_CRYPTO_PROPERTIES,
				SpecUtility.DEFAULT_ALIAS, SpecUtility.DEFAULT_PASSWORD, SpecUtility.DEFAULT_PASSWORD);
		
		TestManager testSingle = new TestManagerSingle( "EfficiencyTestSingle", 
							CIRCLE_COUNT, signatureManager);
		testSingle.execute();
		testSingle.report();
	}
}
