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
package CAdES.speed;

import CAdES.configuration.Configuration;
import CAdES.configuration.IConfiguration;
import CAdES.configuration.SimpleConfiguration;
import CAdES.configuration.container.ISignatureContainer;

import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;

import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.CAdESSigner;
import ru.CryptoPro.CAdES.CAdESType;
import ru.CryptoPro.CAdES.exception.CAdESException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Класс для проверки производительности определенной операции  - создание, 
 * проверка или усовершенствование подписи CAdES. Все настройки - ключ, 
 * сертификаты, СОС - загружаются по параметрам, записанным в файле Configuration.
 * 
 * 26/04/2012
 *
 */
public class OperationManager {

	/**
	 * Тип операции, выполняемой в потоке.
	 */
	public static enum OperationType { otSignCadesBes, otSignCadesXLongType1,
		otEnhanceCadesBes, otVerifyCadesBes, otVerifyCadesXLongType1 };

	/**
	 * Текущий тип выполняемой операции.
	 */
	private OperationType operationType;
    /**
     * Конфигуратор подписи.
     */
    private IConfiguration config = null;
		
	/**
	 * Конструктор. Загрузка цепочки сертификатов и закрытого ключа из 
	 * Configuration.
	 *
     * @param provider Имя провайдера для хеширования, подписи и проверки
     * подписи.
     * @param container Описание ключевого контейнера.
	 * @param otype Тип операции.
	 */
	public OperationManager(String provider, ISignatureContainer container,
        OperationType otype) {

		operationType = otype;

        try {
            config = new SimpleConfiguration(container, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * Выполнение операции в зависимости от типа.
	 * 
	 * @param data CAdES подпись для проверки.
	 * @return CAdES подпись.
	 */
	public byte[] execute(byte[] data) {
		
		try {
		
			switch (operationType) {
			
				case otSignCadesBes:
				case otSignCadesXLongType1:	{
					return sign();
				}
				
				case otVerifyCadesBes:
				case otVerifyCadesXLongType1: {
					verify(data);
					break;
				}
				
				case otEnhanceCadesBes: {
					return enhance(data);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Формирование совмещенной подписи в зависимости от типа.
	 * 
	 * @return CAdES подпись.
	 * @throws CAdESException
	 */
	private byte[] sign() throws CAdESException, IOException {
		
		CAdESSignature cadesSignature = new CAdESSignature(false);

		switch (operationType) {
		
			case otSignCadesBes: {
				cadesSignature.addSigner(config.getProviderName(),
                    config.getDigestOid(),
                    config.getPublicKeyOid(),
                    config.getPrivateKey(),
                    config.getChain(),
                    CAdESType.CAdES_BES,
                    null, false);
			}
            break;

			case otSignCadesXLongType1: {
				cadesSignature.addSigner(config.getProviderName(),
                    config.getDigestOid(),
                    config.getPublicKeyOid(),
                    config.getPrivateKey(),
                    config.getChain(),
                    CAdESType.CAdES_X_Long_Type_1,
                    config.getTSAAddress(), false);
			}
            break;

		} // switch

        ByteArrayOutputStream outSignatureStream = new ByteArrayOutputStream();

        cadesSignature.open(outSignatureStream);
        cadesSignature.update(Configuration.DATA);

        cadesSignature.close();
        outSignatureStream.close();

		return outSignatureStream.toByteArray();
	}
	
	/**
	 * Проверка совмещенной подписи.
	 * 
	 * @param data Подпись для проверки.
	 * @throws CAdESException
	 */
	private void verify(byte[] data) throws CAdESException {
		
		if (data == null) {
			throw new IllegalArgumentException("Data is null.");
		}

		CAdESSignature cadesSignature = new CAdESSignature(data, null, null);

		cadesSignature.verify(
            operationType.equals(OperationType.otVerifyCadesXLongType1)
            ? null : config.getChain(), config.getCRLs());
	}
	
	/**
	 * Усовершенствование подписи CAdES-BES до CAdES-X Long Type 1.
	 * 
	 * @param data CAdES-BES подпись.
	 * @return усовершенствованная CAdES-X Long Type 1 подпись.
	 * @throws CAdESException
	 */
	private byte[] enhance(byte[] data) throws CAdESException, IOException {
		
		if (data == null) {
			throw new IllegalArgumentException("Data is null.");
		} // if

		CAdESSignature cadesSignature = new CAdESSignature(data, null, null);
		
		// Список всех подписантов в исходной подписи.
		Collection<SignerInformation> srcSignerInfos = new ArrayList<SignerInformation>();
								
		for (CAdESSigner signer : cadesSignature.getCAdESSignerInfos()) {
			srcSignerInfos.add(signer.getSignerInfo());
		} // for
		
		// Получаем только первого подписанта CAdES-BES, усовершенствуем его подпись.
		// Остальных не трогаем.
		InputStream srcSignedData = new ByteArrayInputStream(data);
		CAdESSigner srcSigner = cadesSignature.getCAdESSignerInfo(0);
					
		// Исключаем его из исходного списка, т.к. его место займет подписант с
		// усовершенствованной подписью.
		srcSignerInfos.remove(srcSigner.getSignerInfo());
					
		// Усовершенствуем CAdES-BES до CAdES-X Long Type 1.
		srcSigner.enhance(config.getProviderName(),
            config.getDigestOid(),
            config.getChain(),
            config.getTSAAddress(),
            CAdESType.CAdES_X_Long_Type_1);

		if (srcSigner.getCAdESCTimestampToken() == null) {
			throw new IOException("Invalid cAdESC-timestamp value");
		}

		if (srcSigner.getSignatureTimestampToken() == null) {
			throw new IOException("Invalid signing-timestamp value");
		}

		if (srcSigner.getCAdESCTimestampTokenList() == null ||
			srcSigner.getCAdESCTimestampTokenList().size() != 1) {
			throw new IOException("Invalid cAdESC-timestamp count");
		}

		if (srcSigner.getSignatureTimestampTokenList() == null ||
			srcSigner.getSignatureTimestampTokenList().size() != 1) {
			throw new IOException("Invalid signing-timestamp count");
		}
								
		// Подписант с усовершенствованной подписью.
		SignerInformation enhSigner = srcSigner.getSignerInfo();
					
		// Добавляем его в исходный список подписантов.
		srcSignerInfos.add(enhSigner);
		
		// Список подписантов.
		SignerInformationStore dstSignerInfoStore = 
			new SignerInformationStore(srcSignerInfos);

        ByteArrayOutputStream outSignatureStream = new ByteArrayOutputStream();

		// Обновляем исходную подпись c ее начальным списком подписантов на тот же,
		// но с первым подписантом с усовершенствованной подписью.
		CAdESSignature.replaceSigners(srcSignedData,
            dstSignerInfoStore, outSignatureStream);

        outSignatureStream.close();
        return outSignatureStream.toByteArray();
	}
}
