/**
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 *
 * Пример зашифрования/расшифрования PKCS7 (detached/attached).
 * Составлен из примеров CMS, CMSDcrypt, CMSSignAndEncrypt. 
 * Требуется наличие набора примеров samples.jar.
 */
package CMS_samples;

import com.objsys.asn1j.runtime.*;

import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.*;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax.*;
import ru.CryptoPro.JCP.ASN.GostR3410_EncryptionSyntax.GostR3410_12_KEG_Parameters;
import ru.CryptoPro.JCP.ASN.GostR3410_EncryptionSyntax.GostR3410_GostR3412_KeyTransport;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.*;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.KeyStore.JCPPrivateKeyEntry;
import ru.CryptoPro.JCP.params.*;
import ru.CryptoPro.JCP.spec.X509PublicKeySpec;
import ru.CryptoPro.JCP.tools.Array;

import ru.CryptoPro.JCSP.JCSP;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.X509EncodedKeySpec;

import static CMS_samples.CMStools.*;

/**
 * Низкоуровневый пример шифрования CMS на алгоритме Магма.
 *
 *	*.txt - файл с исходной текстовой информацией.
 *	*.cms - файл с подписью PKCS7 (DETACHED/ATTACHED).
 *	*.env - файл с зашифрованной подписью PKCS7 (ENVELOPED).
 *	client_exch - сертификат отправителя.
 *	afevma_gost_exch_server - сертификат получателя.
 *	Формирование PKCS7 DETACHED:
 *		csptest -cmssfsign -sign -in "in.txt" -my client_exch -add -detached -signature "out.cms"
 *	Проверка PKCS7 CMS DETACHED:
 *		csptest -cmssfsign -verify -in "in.txt" -my client_exch -add -detached -signature "out.cms"
 *	Формирование PKCS7 CMS ATTACHED:
 *		csptest -cmssfsign -sign -in "in.txt" -my client_exch -add -signature "out.cms"
 *	Проверка PKCS& CMS ATTACHED:
 *		csptest -cmssfsign -verify -in "out.cms" -my client_exch -add
 *	Зашифрование PKCS7 CMS DETACHED/ATTACHED на алгоритме Магма:
 *		csptest -sfenc -encrypt -out "in.cms" -in "pkcs7.env" -cert client_exch -my afevma_gost_exch_server -alg GR3412_OMAC_M
 *	Расшифрование PKCS7 CMS DETACHED/ATTACHED на алгоритме Магма:
 *		csptest -sfenc -decrypt -out "out.cms" -in "pkcs7.env" -cert afevma_gost_exch_server -my client_exch
 *	Подпись и зашифование PKCS7 CMS ATTACHED на алгоритме Магма:
 *		csptest -sfse -encrypt -in "in.txt" -out "pkcs7.env" -senderDN client_exch -recipDN afevma_gost_exch_server -alg GR3412_OMAC_M
 *	Проверка и расшифрование PKCS7 CMS ATTACHED на алгоритме Магма:
 *		csptest -sfse -decrypt -in "pkcs7.env" -out "out.txt" -senderDN client_exch -recipDN afevma_gost_exch_server
 */
public class PKCS7EnvEphTransportMagma {

	/**
	 * Режим шифрования.
	 */
	private final static String CIPHER = "GOST3412_2015_M";

	/**
	 * Режим шифрования данных.
	 */
	private final static String CIPHER_MODE = CIPHER + "/OMAC_CTR/NoPadding";
	/**
	 * Режим шифрования ключа.
	 */
	private final static String WRAP_MODE = CIPHER + "/KEXP_2015_M_EXPORT/NoPadding";

	/**
	 * Провайдер хранилища, подписи, хеширования.
	 */
	private final static String PROVIDER_NAME = JCSP.PROVIDER_NAME;

	/**
	 * Провайдер шифрования.
	 */
	private final static String CRYPT_PROVIDER_NAME =  JCSP.PROVIDER_NAME;

	/**
	 * Тип контейнера.
	 */
	private final static String STORE_TYPE = JCSP.HD_STORE_NAME;

	/**
	 * Хранилище контейнеров.
	 */
	private final static KeyStore keyStore;

	static {
		keyStore = loadKeyStore();
	}
	
	/**
	 * Загрузка контейнеров.
	 * @return указатель на загруженные контейнеры.
	 */
	public static KeyStore loadKeyStore() {

		try {

			KeyStore keyStore = KeyStore.getInstance(STORE_TYPE, PROVIDER_NAME);
			keyStore.load(null, null);

			return keyStore;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Создание Enveloped CMS.
	 *
	 * @param recipientAlias - алиас получателя (сертификат).
	 * @param data - исходное сообщение (PKCS7).
	 * @return зашифрованное сообщение (enveloped CMS).
	 * @throws Exception
	 */
	public static byte[] EncryptPKCS7(String recipientAlias, byte[] data) throws Exception {

		final X509Certificate recipientCert = (X509Certificate) keyStore.getCertificate(recipientAlias);
		return EncryptPKCS7(recipientCert, data);

	}
	
	/**
	 * Создание enveloped CMS.
	 *
	 * @param recipientCert - сертификат получателя.
	 * @param data - исходное сообщение (PKCS7).
	 * @return зашифрованное сообщение (enveloped CMS).
	 * @throws Exception
	 */
	public static byte[] EncryptPKCS7(X509Certificate recipientCert, byte[] data) throws Exception {

		final PublicKey recipientPublic = recipientCert.getPublicKey();

		// Генерирование симметричного ключа.

		final KeyGenerator kg = KeyGenerator.getInstance(CMStools.MAGMA_ALG_NAME, CRYPT_PROVIDER_NAME);
    	final SecretKey symmetricKey = kg.generateKey();

		// Генерация параметров для шифрования в режиме OMAC-ACPKM.

		byte[] ukm = new byte[(JCP.G28147_BLOCKLEN >> 1) + JCP.CMS_GR3412_KEG_SEED_LEN];
		SecureRandom random = SecureRandom.getInstance("CPRandom", PROVIDER_NAME);

		random.nextBytes(ukm);
		G3412ParamsSpec g3412ParameterSpec = new G3412ParamsSpec(ukm, true);

		// Зашифрование текста на симметричном ключе.

		Cipher cipher = Cipher.getInstance(CIPHER_MODE, CRYPT_PROVIDER_NAME);
		cipher.init(Cipher.ENCRYPT_MODE, symmetricKey, g3412ParameterSpec, (SecureRandom) null);

		final byte[] text = cipher.doFinal(data, 0, data.length);

		// Получаем зашифрованную имиту. Ее нужно
		// сохранить и использовать при расшифровании.

		byte[] omac = null;
		AlgorithmParameters omacParams = cipher.getParameters();
		OmacParamsSpec spec;

		if (omacParams != null && omacParams.getAlgorithm().equalsIgnoreCase(JCP.GOST_OMAC_NAME)) {
			try {
				spec = omacParams.getParameterSpec(OmacParamsSpec.class);
				omac = spec.getOmacValue();
			} catch (InvalidParameterSpecException e) {
				throw new IOException(e);
			}
		} // if

		// Зашифрование симметричного ключа.

		final byte[] keyTransport = wrap(symmetricKey, recipientPublic);

		// Формирование CMS-сообщения.

		final ContentInfo all = new ContentInfo();
		all.contentType = new Asn1ObjectIdentifier(new OID(CMStools.STR_CMS_OID_ENVELOPED).value);

		final EnvelopedData cms = new EnvelopedData();

		all.content = cms;
		cms.version = new CMSVersion(0);

		cms.recipientInfos = new RecipientInfos(1);
		cms.recipientInfos.elements = new RecipientInfo[1];
		cms.recipientInfos.elements[0] = new RecipientInfo();

		final KeyTransRecipientInfo key_trans = new KeyTransRecipientInfo();
        key_trans.version = new CMSVersion(0);

		final Asn1ObjectIdentifier alg_pb = new Asn1ObjectIdentifier((new OID(STR_KEY_WRAP_ALG_ID_M)).value);
		final Asn1OpenType der_params;

		final Asn1BerEncodeBuffer ebuf = new Asn1BerEncodeBuffer();
		OID wrapDhOid;

		PublicKey publicKey = recipientCert.getPublicKey();
		String pubKeyAlg = publicKey.getAlgorithm();

		if ((pubKeyAlg.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME)) ||
			(pubKeyAlg.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME))) {
			wrapDhOid = new OID(STR_WRAP_GOST_2012_256_ESDH);
		} else if (pubKeyAlg.equalsIgnoreCase(JCP.GOST_DH_2012_512_NAME) ||
			(pubKeyAlg.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME))) {
			wrapDhOid = new OID(STR_GOST_2012_512_ESDH);
		} else {
			throw new Exception("Invalid key algorithm: " + pubKeyAlg);
		}

		GostR3410_12_KEG_Parameters params = new GostR3410_12_KEG_Parameters(wrapDhOid.value);
		params.encode(ebuf);

		der_params = new Asn1OpenType(ebuf.getMsgCopy());
		ebuf.reset();

		key_trans.keyEncryptionAlgorithm = new KeyEncryptionAlgorithmIdentifier(alg_pb, der_params);

        key_trans.rid = new RecipientIdentifier();
		final IssuerAndSerialNumber issuer = new IssuerAndSerialNumber();

		final X500Principal issuerName = recipientCert.getIssuerX500Principal();
		Asn1BerDecodeBuffer dbuf = new Asn1BerDecodeBuffer(issuerName.getEncoded());
	    
		issuer.issuer = new Name();
		final RDNSequence rnd = new RDNSequence();
		rnd.decode(dbuf);
	    
		issuer.issuer.set_rdnSequence(rnd);
		issuer.serialNumber = new CertificateSerialNumber(recipientCert.getSerialNumber());

        key_trans.rid.set_issuerAndSerialNumber(issuer);
		dbuf.reset();

        key_trans.encryptedKey = new EncryptedKey(keyTransport);
		ebuf.reset();
	    
		cms.recipientInfos.elements[0].set_ktri(key_trans);
		cms.encryptedContentInfo = new EncryptedContentInfo();

		final OID contentType = new OID(CMStools.STR_CMS_OID_DATA);
		cms.encryptedContentInfo.contentType = new ContentType(contentType.value);

		final Gost3412_15_Encryption_Parameters  parameters = new Gost3412_15_Encryption_Parameters();
		parameters.ukm = new Asn1OctetString(ukm);

		cms.encryptedContentInfo.contentEncryptionAlgorithm = new ContentEncryptionAlgorithmIdentifier(_Gost28147_89_EncryptionSyntaxValues.id_tc26_cipher_gost_3412_2015_M_ctr_acpkm_omac, parameters);
		cms.encryptedContentInfo.encryptedContent = new EncryptedContent(text);

		// Записываем OMAC в неподписанные аттрибуты.

		Attribute[] attr = new Attribute[1];
		Asn1OctetString octetString = new Asn1OctetString(omac);

		octetString.encode(ebuf);
		Asn1OpenType value = new Asn1OpenType(ebuf.getMsgCopy());

		ebuf.reset();
		Attribute_values values = new Attribute_values(new Asn1Type[]{value});

		attr[0] = new Attribute((new OID(CMStools.STR_CMS_GR3412_OMAC)).value, values);
		cms.unprotectedAttrs = new UnprotectedAttributes(attr);

		all.encode(ebuf);
		return ebuf.getMsgCopy();

	}
		
	/**
	 * Расшифрование Enveloped CMS.
	 *
	 * @param recipientAlias - алиас получателя.
	 * @param recipientPassword - пароль получателя.
	 * @param enveloped - зашифрованное сообщение (enveloped CMS).
	 * @param data - исходные данные (нужны при проверке detached CMS подписи).
	 * @param detached - флаг detached подписи.
	 * @return результат проверки.
	 * @throws Exception
	 */
	public static boolean DecryptPKCS7(String recipientAlias, char[] recipientPassword, byte[] enveloped,
		byte[] data, boolean detached) throws Exception {

		// Разбор CMS-сообщения.

		Asn1BerDecodeBuffer dbuf = new Asn1BerDecodeBuffer(enveloped);
		final ContentInfo all = new ContentInfo();

		all.decode(dbuf);
		dbuf.reset();
		
		final EnvelopedData cms = (EnvelopedData) all.content;
		KeyTransRecipientInfo key_trans;

        // Только key_trans.

		if (cms.recipientInfos.elements[0].getChoiceID() == RecipientInfo._KTRI) {
            key_trans = (KeyTransRecipientInfo) (cms.recipientInfos.elements[0].getElement());
		}
		else {
			throw new Exception("Unknown recipient info.");
		}

		// получаем omac
		byte[] omac = null;
		UnprotectedAttributes attributeTable = cms.unprotectedAttrs;

		if (attributeTable != null) {

			Attribute[]  elements = attributeTable.elements;
			for (Attribute next : elements) {

				if (Array.compare(next.type.value, (new OID(CMStools.STR_CMS_GR3412_OMAC).value))) {

					dbuf = new Asn1BerDecodeBuffer(((Asn1OpenType)next.values.elements[0]).value);
					Asn1OctetString octetString = new Asn1OctetString();

					octetString.decode(dbuf);
					omac = octetString.value;

					dbuf.reset();
					break;

				}

			} // for

		} // if

		// Зашифрованный ключ + зашифрованный mac.

		final byte[] wrapKey = key_trans.encryptedKey.value;

		// Параметры.

		Gost3412_15_Encryption_Parameters params = new Gost3412_15_Encryption_Parameters();
		dbuf = new Asn1BerDecodeBuffer(((Asn1OpenType)cms.encryptedContentInfo.contentEncryptionAlgorithm.parameters).value);

		params.decode(dbuf);
		dbuf.reset();

		if (params.ukm.value.length != ((JCP.G28147_BLOCKLEN >> 1) + JCP.CMS_GR3412_KEG_SEED_LEN)) {
			throw new Exception("Invalid UKM length");
		} // if

		final byte[] iv = params.ukm.value;
		final byte[] text = cms.encryptedContentInfo.encryptedContent.value;

		// Получатель - закрытый ключ.

        final JCPProtectionParameter protectionParameter = new JCPProtectionParameter(recipientPassword);
        final JCPPrivateKeyEntry recipientEntry = (JCPPrivateKeyEntry) keyStore.getEntry(recipientAlias, protectionParameter);

		// Выработка ключа согласования получателем и
		// расшифрование симметричного ключа.

		final SecretKey symmetricKey = unwrap(wrapKey, recipientEntry.getPrivateKey());

		// Расшифрование текста на симметричном ключе.
		// Формируем параметры для шифрования: omac для проверки имиты,
		// а также указываем, что шифруем cms.

		final byte[] finalOmac = omac;
		final AlgorithmParameterSpec spec = new OmacParamsSpec(new OmacTransportInterface() {

			@Override
			public byte[] getOmac() throws IOException {
				return finalOmac;
			}

		}, iv, true);

		Cipher cipher = Cipher.getInstance(CIPHER_MODE, CRYPT_PROVIDER_NAME);
		cipher.init(Cipher.DECRYPT_MODE, symmetricKey, spec, null);

		final byte[] result = cipher.doFinal(text, 0, text.length);

		Array.writeFile(PKCS7EnvEphTransport.TEST_DIR + "cms_cms.bin", result);
		// checkPKCS7(result, detached, data, PKCS7EnvEphTransport.TEST_DIR + "cms_cms_data_magma.txt");

		return true;

	}

	/**
	 * Зашифрование сессионного ключа.
	 *
	 * @param secretKey Сессионный ключ.
	 * @param recipientKey Открытый ключ получателя.
	 * @return транспортная структура GostR3410_GostR3412_KeyTransport.
	 * @throws Exception
	 */
	private static byte[] wrap(SecretKey secretKey, PublicKey recipientKey) throws Exception {

        // Определение алгоритма эфемерного ключа.

        String keyAlgorithm = recipientKey.getAlgorithm();
        String ephKeyAlgorithm = JCP.GOST_EL_DH_EPH_NAME;

        if (keyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME)) {
            ephKeyAlgorithm = JCP.GOST_EPH_DH_2012_256_NAME;
        } // if
        else if (keyAlgorithm.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME)) {
            ephKeyAlgorithm = JCP.GOST_EPH_DH_2012_512_NAME;
        } // else

		// Генерация эфемерной пары.

		KeyPairGenerator kgp = KeyPairGenerator.getInstance(ephKeyAlgorithm, CRYPT_PROVIDER_NAME);

		// Устанавливаем нужные параметры, как у
		// получателя.

        AlgorithmParameterSpec spec = new X509PublicKeySpec(recipientKey.getEncoded());
        kgp.initialize(spec);

		// Генерируем эфемерную пару. Ключи получат
		// параметры recipientKey, а у него параметры
		// - recipientTransportParameters.

		KeyPair ephPair = kgp.generateKeyPair();

		PrivateKey privateKey = ephPair.getPrivate();
		PublicKey publicKey = ephPair.getPublic();

		// Генерим ключевой материал.

		byte[]  ukm = new byte[JCP.CMS_GR3412_UKM_LEN];
		SecureRandom random = SecureRandom.getInstance("CPRandom", PROVIDER_NAME);
		random.nextBytes(ukm);

		// Ставим UKM для VKO в рамках KEG — первые 16 байт ukm.
		// Согласно документу на TLS-2015 представлен в big-endian

		byte[] bUKM = new byte[JCP.CMS_GR3412_KEG_UKM_LEN];
		for (int i = 0; i < JCP.CMS_GR3412_KEG_UKM_LEN; ++i) {
			bUKM[i] = ukm[JCP.CMS_GR3412_KEG_UKM_LEN - i - 1];
		}

		// Параметры для согласования.

		IvParameterSpec agreeSpec = new IvParameterSpec(bUKM);
		byte[] expUkm = new byte[JCP.G28147_BLOCKLEN / 2];

		Array.copy(ukm, JCP.CMS_GR3412_KEXP15_IV_OFFSET, expUkm, 0, JCP.G28147_BLOCKLEN / 2);
		byte[] extentedUkm = null;

		// Если ключ имеет длину 256, его нужно нарастить.

		String keyAlgName = publicKey.getAlgorithm();

		if (keyAlgName.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME) ||
			keyAlgName.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME)) {
			extentedUkm = new byte[JCP.CMS_GR3412_KEG_SEED_LEN];
			Array.copy(ukm, JCP.CMS_GR3412_KEG_UKM_LEN, extentedUkm, 0, JCP.CMS_GR3412_KEG_SEED_LEN);
		}

		// Параметры для зашифрования ключа.

		Kexp15ParamsSpec kExpSpec = new Kexp15ParamsSpec(expUkm, extentedUkm);

		// Выработка ключа согласования.

		KeyAgreement ka = KeyAgreement.getInstance(privateKey.getAlgorithm(), CRYPT_PROVIDER_NAME);
		ka.init(privateKey, agreeSpec);

		ka.doPhase(recipientKey, true);
		Key dh = ka.generateSecret(CIPHER); // dh получит параметры из privateKey

		// Зашифрование симметричного ключа на ключе согласования
		// отправителя. Передаются параметры, содержащие UKM

		final Cipher cipher = Cipher.getInstance(WRAP_MODE, CRYPT_PROVIDER_NAME);
		cipher.init(Cipher.WRAP_MODE, dh, kExpSpec);

		final byte[] wrappedKey = cipher.wrap(secretKey);

		// Кодирование открытого ключа в SubjectPublicKeyInfo.

		byte[] publicKeyBytes = publicKey.getEncoded();
		SubjectPublicKeyInfo publicKeyInfo = new SubjectPublicKeyInfo();

		Asn1BerDecodeBuffer decoder = new Asn1BerDecodeBuffer(publicKeyBytes);
		publicKeyInfo.decode(decoder);

		// Кодирование GostR3410_GostR3412_KeyTransport.

		GostR3410_GostR3412_KeyTransport keyTransport = new GostR3410_GostR3412_KeyTransport();
		keyTransport.encryptedKey = new Asn1OctetString(wrappedKey);

		keyTransport.ephemeralPublicKey = publicKeyInfo;
		keyTransport.ukm = new Asn1OctetString(ukm);

		Asn1BerEncodeBuffer encoder = new Asn1BerEncodeBuffer();
		keyTransport.encode(encoder);

		return encoder.getMsgCopy();

	}

	/**
	 * Расшифрование сессионного ключа.
	 *
	 * @param wrappedKey Зашифрованный сессионный ключ (транспортная
	 * структура GostR3410_GostR3412_KeyTransport).
	 * @param recipientKey Закрытый ключ получателя.
	 * @return сессионный ключ.
	 * @throws Exception
	 */
	private static SecretKey unwrap(byte[] wrappedKey, Key recipientKey) throws Exception {

		// Декодирование GostR3410_GostR3412_KeyTransport.

		GostR3410_GostR3412_KeyTransport keyTransport = new GostR3410_GostR3412_KeyTransport();
		Asn1BerDecodeBuffer decoder = new Asn1BerDecodeBuffer(wrappedKey);
		keyTransport.decode(decoder);

		// Зашифрованный ключ + зашифрованный mac.

		byte[] wrapped = keyTransport.encryptedKey.value;

		// Декодирование открытого ключа.
		Asn1BerEncodeBuffer encoder = new Asn1BerEncodeBuffer();
		keyTransport.ephemeralPublicKey.encode(encoder);

		byte[] encodedPublic = encoder.getMsgCopy();
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedPublic);

        KeyFactory kf = KeyFactory.getInstance(recipientKey.getAlgorithm(), PROVIDER_NAME);
		PublicKey publicKey = kf.generatePublic(keySpec);

		// Параметры шифрования.
		byte[] ukm = keyTransport.ukm.value;

		if (ukm.length != JCP.CMS_GR3412_UKM_LEN) {
			throw new InvalidKeyException("Invalid UKM length");
		} // if

		// Ставим UKM для VKO в рамках KEG — первые 16 байт ukm.
		// Согласно документу на TLS-2015 представлен в big-endian

		byte[] bUKM = new byte[JCP.CMS_GR3412_KEG_UKM_LEN];
		for (int i = 0; i < JCP.CMS_GR3412_KEG_UKM_LEN; ++i) {
			bUKM[i] = ukm[JCP.CMS_GR3412_KEG_UKM_LEN - i - 1];
		}

		// Параметры для согласования.

		IvParameterSpec agreeSpec = new IvParameterSpec(bUKM);
		byte[] expUkm = new byte[JCP.G28147_BLOCKLEN / 2];

		Array.copy(ukm, JCP.CMS_GR3412_KEXP15_IV_OFFSET, expUkm, 0, JCP.G28147_BLOCKLEN / 2);
		byte[] extentedUkm = null;

		// Если ключ имеет длину 256, его нужно нарастить.

		String keyAlgName = publicKey.getAlgorithm();

		if (keyAlgName.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME) ||
			keyAlgName.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME)) {
			extentedUkm = new byte[JCP.CMS_GR3412_KEG_SEED_LEN];
			Array.copy(ukm, JCP.CMS_GR3412_KEG_UKM_LEN, extentedUkm, 0, JCP.CMS_GR3412_KEG_SEED_LEN);
		}

		// Параметры для зашифрования ключа.

		Kexp15ParamsSpec kExpSpec = new Kexp15ParamsSpec(expUkm, extentedUkm);

		// Выработка ключа согласования.

		KeyAgreement ka = KeyAgreement.getInstance(recipientKey.getAlgorithm(), CRYPT_PROVIDER_NAME);
		ka.init(recipientKey, agreeSpec);

		ka.doPhase(publicKey, true);
		Key dh = ka.generateSecret(CIPHER); // dh получит параметры из recipientKey

		// Расшифрование сессионного ключа.
		Cipher cipher = Cipher.getInstance(WRAP_MODE, CRYPT_PROVIDER_NAME);
		cipher.init(Cipher.UNWRAP_MODE, dh, kExpSpec);

		return (SecretKey) cipher.unwrap(wrapped, CIPHER, Cipher.SECRET_KEY);

	}

	/**
	 * Выполнение создания PKCS7 подписи, зашифрования, расшифрования и
	 * проверки подписи.
	 *
	 * @throws Exception
	 */
	private static void test_SignEncrypt_DecryptVerify() throws Exception {

		// "C:\Program Files\Crypto Pro\CSP\csptest" -cmssfsign -sign -in "in.txt" -my signencr -add -out "csp_attached_cms_magma.bin" (создание CSP)
		// "C:\Program Files\Crypto Pro\CSP\csptest" -cmssfsign -verify -in "cms_cms_magma.bin" -my signencr (проверка подписи JCP с помощью CSP)

		// Файл с PKCS7 для зашифрования.
		final byte[] encryptingData = PKCS7EnvEphTransport.DATA; // Array.readFile(TEST_DIR + "jcp_attached_cms_magma.bin");

		// 2. Зашифрование PKCS7.

		byte[] enveloped =  EncryptPKCS7(PKCS7EnvEphTransport.RECIPIENT.getAlias(), encryptingData);
		// "C:\Program Files\Crypto Pro\CSP\csptest" -sfenc -encrypt -in "csp_attached_cms_magma.bin" -out "csp_enveloped_cms_magma.bin" -cert signenc -alg GR3412_OMAC_M (создание CSP)

		Array.writeFile(PKCS7EnvEphTransport.TEST_DIR + "jcp_enveloped_cms_magma.bin", enveloped);

		// 3*. Расшифрование PKCS7 и проверка подписи.

		enveloped = Array.readFile(PKCS7EnvEphTransport.TEST_DIR + "jcp_enveloped_cms_magma.bin");
		System.out.println("Verified: " + DecryptPKCS7(PKCS7EnvEphTransport.RECIPIENT.getAlias(), PKCS7EnvEphTransport.RECIPIENT.getPassword(), enveloped, null, false));

        // Для low data:
        // "C:\Program Files\Crypto Pro\CSP\csptest" -lowenc -decrypt -in "jcp_enveloped_cms_magma.bin" -my gost_2012_512_exchange_dh_client
        // для CMS:
		// "C:\Program Files\Crypto Pro\CSP\csptest" -sfenc -decrypt -in "jcp_enveloped_cms_magma.bin" -out "csp_jcp_cms_data_content_magma.txt" (проверка JCP с помощью CSP)
		// "C:\Program Files\Crypto Pro\CSP\csptest" -sfenc -decrypt -in "csp_enveloped_cms_magma.bin" -out "csp_cms_data_content_magma.txt" (проверка CSP с помощью CSP)
		// content: *_content.txt == in.txt

	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		test_SignEncrypt_DecryptVerify();
	}

}