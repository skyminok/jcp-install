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

package wss4j.utility;

import org.apache.axis.Message;
import org.apache.axis.SOAPPart;
import org.apache.axis.message.SOAPEnvelope;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Class with auxiliary SOAP functions.
 */
public class SOAPUtility {

	/**
	 * Simple XML SOAP example.
	 */
	public static final String SOAPMSG =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<SOAP-ENV:Envelope "
        +   "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
        +   "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
        +   "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
        +   "<SOAP-ENV:Body>"
        +       "<add xmlns=\"http://ws.apache.org/counter/counter_port_type\">"
        +           "<value xmlns=\"\">15</value>"
        +       "</add>"
        +   "</SOAP-ENV:Body>"
        + "</SOAP-ENV:Envelope>";

	/** Функция проверяет параметр 'org.apache.ws.security.crypto.provider' в файле
	 * 'crypto.properties', чтобы определить, использовать ли MerlinEX
	 * @param classFromProperty - прописанный в properties класс
	 * @return - true, если MerlinEx
	 */
	public static boolean is_MerlinEx( String classFromProperty ){

        if (classFromProperty == null)
            return false;
        try {
            Class classType = Class.forName(classFromProperty);
            String className = classType.getSimpleName();
		
    		// If properties contains MerlinEx class
	    	if ( className.equalsIgnoreCase("MerlinEx") ) {
		    	return true;
		    }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
		return false;
	}
	

    /**
     * Function creates SOAP XML message from string.
     * @return SOAP XML message.
     * @throws Exception
     */
    public static SOAPEnvelope getSOAPEnvelopeFromString(String message) throws Exception {

        InputStream input = new ByteArrayInputStream(message.getBytes());
        Message msg = new Message(input);
        return msg.getSOAPEnvelope();
    }



    /**
     * Получение документа из XML-файла.
     *
     * @param fileName Путь к файлу.
     * @return SOAP-сообщение.
     * @throws Exception
     */
    public static Document getDocumentFromFile(String fileName) throws Exception {

        SOAPEnvelope outEnv = getSOAPEnvelopeFromFile(fileName);
        return outEnv.getAsDocument();
/*        InputStream input = new FileInputStream( new File(fileName) );
        Message msg = new Message(input);
        SOAPPart part = (SOAPPart) msg.getSOAPPart();
        return part.getSOAPDocument();
  */  }

    /**
     * Получение SOAP-сообщения из XML-файла.
     *
     * @param fileName Путь к файлу.
     * @return SOAP-сообщение.
     * @throws Exception
     */
    public static SOAPEnvelope getSOAPEnvelopeFromFile(String fileName) throws Exception {
        try (InputStream input = new FileInputStream( new File(fileName) )) {
            Message msg = new Message(input);
            return msg.getSOAPEnvelope();
        }
    }
}
