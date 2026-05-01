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
 * Test provides different threads for signing and verifying of SOAP 
 * XML documents synchronizing by queue.
 */

package wss4j.wss4j1_6.tests;

import wss4j.manager.SignatureManager;
import wss4j.manager.TestManager;
import wss4j.manager.TestManagerMulti;
import wss4j.utility.SpecUtility;
import wss4j.wss4j1_6.manager.SOAPXMLSignatureManager_1_6;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class EfficiencyTestMulti {
	
	private static final int PRODUCER_COUNT = 2;
	private static final int PROCESSOR_COUNT = 2;
	private static final int WORKOUT_PERIOD = 30000;
	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws KeyStoreException, NoSuchAlgorithmException, 
	CertificateException, ClassNotFoundException {
		
		SignatureManager signatureManager = 
			new SOAPXMLSignatureManager_1_6(SpecUtility.DEFAULT_CRYPTO_PROPERTIES,
				SpecUtility.DEFAULT_ALIAS, SpecUtility.DEFAULT_PASSWORD);
		
		TestManager testMulti = new TestManagerMulti( "EfficiencyTestMulti", 
				PRODUCER_COUNT, PROCESSOR_COUNT, WORKOUT_PERIOD, signatureManager);
		
		testMulti.execute();
		testMulti.report();
	}
}
