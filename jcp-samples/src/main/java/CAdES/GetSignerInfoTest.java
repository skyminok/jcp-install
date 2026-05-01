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
package CAdES;

import CAdES.configuration.Configuration;
import CAdES.configuration.container.Container2012_256;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.JCP.tools.Array;

import ru.CryptoPro.JCP.Util.JCPInit;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Пример получения списка подписантов.
 * 
 * 24/04/2012
 *
 */
public class GetSignerInfoTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {

			JCPInit.initProviders(false);
		
			Collection<X509Certificate> chain = new ArrayList<X509Certificate>();
			Configuration.loadConfiguration(new Container2012_256(), chain);

			// Читаем подпись из файла.
			byte[] cadesCms = Array.readFile(Configuration.TEMP_PATH + "/CAdESSignature.bin");
	
			// Подпись в тесте была совмещенная, потому данные равны null.
			// Предположим, что подписей несколько, тогда лучше указать
			// тип null и положиться на самоопределение типа подписи.

			CAdESSignature cadesSignature = new CAdESSignature(cadesCms, null, null);
			// Configuration.printSignatureInfo(cadesSignature);
			
			System.out.println("------------------------------\n Signer #1 (" +
				cadesSignature.getCAdESSignerInfo(0).getSignatureType() + ")");

		} catch (Exception e) {
            e.printStackTrace();
        }
	}
}
