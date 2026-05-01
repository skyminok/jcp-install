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

package wss4j.manager;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.w3c.dom.Document;

/*
 * Test collects statistics about operations of signing and verifying
 * of XML documents in main thread.
 */
public class TestManagerSingle extends TestManager {

	private long circleCount = 1000;
	private long totalSigningTime = 0;
	private long totalVerifyingTime = 0;
		
	public TestManagerSingle(String name, long crCount, SignatureManager signatureManager) 
	throws KeyStoreException, NoSuchAlgorithmException, CertificateException, ClassNotFoundException {
		super(name, signatureManager);
		circleCount = crCount;
	}

	protected void callRealTestExecution() {
		
		for (int i = 0; i < circleCount; i++) {
			
			long startTime = java.lang.System.currentTimeMillis();
			Document signedDoc = signatureManager.signDoc(signatureManager.getMessage());
			totalSigningTime += (java.lang.System.currentTimeMillis() - startTime);

			startTime = java.lang.System.currentTimeMillis();
			boolean result = signatureManager.verifyDoc(signedDoc, false);
			totalVerifyingTime += (java.lang.System.currentTimeMillis() - startTime);
			
			if (!result) {
				System.out.println("Delta verification failed (" + result + ")");
			}
		}
	}

	public void report() {
		
		double mediumSigningRate = (double)circleCount/(totalSigningTime*0.001);
		double mediumVerifyingTime = (double)circleCount/(totalVerifyingTime*0.001);
		
		System.out.println("###### Report: ######");
		System.out.println("Count of documents which were signed and verified: " + circleCount);
		System.out.println("Total (signing & verifying) time: " + decFormat.format(getTestTimeInSec()) + " sec");
		System.out.println("Rate of signing: " + decFormat.format(mediumSigningRate) + " op/s");
		System.out.println("Rate of verifying: " + decFormat.format(mediumVerifyingTime) + " op/s");
		System.out.println("Rate of (signing & verifying) operation: " + 
				decFormat.format( (double)circleCount/(getTestTimeInSec()) ) + 
				" op/s (1 action rate ~ " + 
				decFormat.format( (double)circleCount*2/(getTestTimeInSec()) ) + " op/s)");
	}
}
