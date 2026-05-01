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

import org.w3c.dom.Document;

/**
 * Simple class defines some methods to be used in thread tests.
 */
public abstract class SignatureManager {

	/**
	 * Function produces text XML SOAP message.
	 * @return XML SOAP message.
	 */
	public abstract String getMessage();
	
	/**
	 * Function sets signature in XML SOAP document.
	 * @param docStr - XML SOAP string.
	 * @return signed document.
	 */
	public abstract Document signDoc(String docStr);
	
	/**
	 * Function verifies signed XML SOAP document.
	 * @param signedDoc - signed document.
	 * @param printCert - option to print certificate.
	 * @return verification result.
	 */
	public abstract boolean verifyDoc(Document signedDoc, boolean printCert);
}
