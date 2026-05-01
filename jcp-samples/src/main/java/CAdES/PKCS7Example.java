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
import CAdES.configuration.SimpleConfiguration;
import CAdES.configuration.container.Container2012_256;

import CAdES.configuration.container.ISignatureContainer;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.selector.X509CertificateHolderSelector;
import org.bouncycastle.cms.*;

import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import ru.CryptoPro.CAdES.CAdESSignature;
import ru.CryptoPro.CAdES.tools.CAdESUtility;
import ru.CryptoPro.CAdES.tools.verifier.*;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.tools.Array;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.*;
import java.util.*;

/**
 * Пример формирования простой подписи PKCS7 с помощью
 * BouncyCastle и проверки CAdES API.
 * 
 * 20/04/2012
 *
 */
public class PKCS7Example {

    /**
     * Запуск примера.
     *
     * @param args Аргументы.
     * @exception Exception
     */
    public static void main(String[] args) throws Exception {
        main0(new Container2012_256(), SimpleConfiguration.TEMP_PATH
            + "/pkcs7.bin", SimpleConfiguration.CRL_PATH, JCP.PROVIDER_NAME,
                JCP.GOST_DIGEST_2012_256_OID, JCP.GOST_PARAMS_SIG_2012_256_KEY_OID);
    }

	/**
	 * Создание и проверка подписи PKCS7.
     *
     * @param signatureContainer Параметры ключа.
     * @param outFilePath Путь к файлу для сохранения подписи.
     * Может быть null.
     * @param crlPath Путь к CRL для проверки подписи. Может
     * быть null.
     * @exception Exception
	 */
	public static void main0(ISignatureContainer signatureContainer,
        String outFilePath, String crlPath, final String providerName,
        String expectedDigOid, String expectedEncOid) throws Exception {

		// Этот вызов делается автоматически при использовании
		// класса CAdESSignature, однако тут необходимо его выполнить
		// специально, т.к. начинаем работать с ГОСТ без упоминания
		// CAdESSignature.

		JCPInit.initProviders(false);
		CAdESUtility.initJCPAlgorithms();
		
		try {
		
			List<X509Certificate> chain  = new ArrayList<X509Certificate>();
			Set<X509Certificate> certSet = new HashSet<X509Certificate>(chain);

			PrivateKey privateKey = Configuration.loadConfiguration(
				new Container2012_256(), chain);
			
			// Сертификат подписи - первый в списке.

			final X509Certificate signerCert = chain.iterator().next();
			Store certStore = new JcaCertStore(chain);
			
			// Подготавливаем подпись.

			CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
			ContentSigner contentSigner = new GostContentSignerProvider(privateKey, providerName);

			SignerInfoGenerator signerInfoGenerator = new JcaSignerInfoGeneratorBuilder(
				new GostDigestCalculatorProvider(privateKey, JCP.PROVIDER_NAME))
                    .build(contentSigner, signerCert);

			generator.addSignerInfoGenerator(signerInfoGenerator);
			generator.addCertificates(certStore);
			  
			// Создаем совмещенную подпись PKCS7.

			CMSProcessable content = new CMSProcessableByteArray(Configuration.DATA);
			CMSSignedData signedData = generator.generate((CMSTypedData) content, true);

            // Сформированная подпись.

            byte[] pkcs7 = signedData.getEncoded();
            Array.writeFile(outFilePath, pkcs7);

            // Проверка идентификаторов.

            CMSSignedData signedData2 = new CMSSignedData(pkcs7);

            SignerInformationStore signerStore = signedData2.getSignerInfos();
            Collection signers = signerStore.getSigners();

            SignerInformation signer = (SignerInformation) signers.iterator().next();

            String digAlgId = signer.getDigestAlgOID();
            String encAlgId = signer.getEncryptionAlgOID();

            if (!digAlgId.equalsIgnoreCase(expectedDigOid)) {
                throw new Exception("Invalid digest algorithm identifier");
            } // if

            if (!encAlgId.equalsIgnoreCase(expectedEncOid)) { // only signature oid
                throw new Exception("Invalid encryption algorithm identifier");
            } // if

            // Проверка средствами BC.

            boolean verified = signedData2.verifySignatures(new SignerInformationVerifierProvider() {

                @Override
                public SignerInformationVerifier get(SignerId sid) throws OperatorCreationException {

                    try {

                    return new SignerInformationVerifier(
                        new GostCMSSignatureAlgorithmNameGenerator(),
                        new GostSignatureAlgorithmIdentifierFinder(),
                        new GostContentVerifierProvider(signerCert, providerName),
                        new GostDigestCalculatorProvider(signerCert.getPublicKey(), providerName));

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }
            }, true);

            if (!verified) {
                throw new Exception("Signature validation failed.");
            } // if

            // Проверка средствами CAdES.
			// Подпись в тесте была совмещенная, потому данные равны null. Предположим, что
			// подписей несколько, тогда лучше указать тип null и положиться на самоопределение
			// типа подписи.

			CAdESSignature pkcs7Signature = new CAdESSignature(pkcs7, null, null);
			
			// Если задан CRL, то читаем его из файла.
			if (crlPath != null) {

                try (FileInputStream is = new FileInputStream(crlPath)) {
                    X509CRL crl = (X509CRL) CertificateFactory.getInstance("X.509").generateCRL(is);
                    pkcs7Signature.verify(certSet, Collections.singleton(crl));
                }

			} // if
            else {
				pkcs7Signature.verify(certSet);
			} // else

            // Configuration.printSignatureInfo(pkcs7Signature);
			
		} catch (Exception e) {
            // Configuration.printCAdESException(e);
            throw e;
        }
	}
}
