/**
 * $RCSfile$
 * version $Revision$
 * created 28.05.2008 10:26:51 by kunina
 * last modified $Date$ by $Author$
 * (C) ООО Крипто-Про 2004-2008.
 *
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package CMS_samples;

import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1BerEncodeBuffer;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.spec.GostCipherSpec;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.ContentInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.EnvelopedData;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.KeyTransRecipientInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.RecipientInfo;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax.Gost28147_89_Parameters;
import ru.CryptoPro.JCP.ASN.GostR3410_EncryptionSyntax.GostR3410_KeyTransport;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCP.Util.JCPInit;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;

/**
 * Decrypt message.
 *
 * @author Copyright 2004-2009 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class CMSDecrypt {

//private static final String CMS_FILE_PATH = "envCMS.txt";

// ГОСТ Р 34.10-2001
private static final String CMS_FILE_PATH = CMSSignAndEncrypt.CMS_FILE_PATH;

// ГОСТ Р 34.10-2012 (256)
private static final String CMS_FILE_PATH_2012_256 = CMSSignAndEncrypt.CMS_FILE_PATH_2012_256;

// ГОСТ Р 34.10-2012 (512)
private static final String CMS_FILE_PATH_2012_512 = CMSSignAndEncrypt.CMS_FILE_PATH_2012_512;

// Режим шифрования
private static final String CIPHER_MODE = CMSSignAndEncrypt.CIPHER_MODE;

/**
 * @param args
 * @throws Exception
 */
public static void main(String[] args) throws Exception {

    JCPInit.initProviders(false);
    main_(args);

}

/**
 * @param args
 * @throws Exception
 */
public static void main_(String[] args) throws Exception {

    main_(CMS_FILE_PATH, CMStools.RECIP_KEY_NAME,
        CMStools.RECIP_KEY_PASSWORD, CMStools.KEY_ALG_NAME,
            CMStools.KEY_ALG_NAME);

    main_(CMS_FILE_PATH_2012_256, CMStools.RECIP_KEY_NAME_2012_256,
        CMStools.RECIP_KEY_PASSWORD_2012_256, CMStools.KEY_ALG_NAME_2012_256,
            CMStools.KEY_ALG_NAME_2012_256);

    main_(CMS_FILE_PATH_2012_512, CMStools.RECIP_KEY_NAME_2012_512,
        CMStools.RECIP_KEY_PASSWORD_2012_512, CMStools.KEY_ALG_NAME_2012_512,
            CMStools.KEY_ALG_NAME_2012_512);

}

/**
 * Расшифрование подписи.
 *
 * @param encryptedFile Зашифрованная подпись.
 * @param recipientAlias Алиас ключа получателя.
 * @param recipientPassword Пароль к ключу получателя.
 * @param pubKeyAlgorithm Алгоритм открытого ключа.
 * @param agreeAlgorithm Алгоритм согласования.
 * @param storeType Тип контейнера.
 * @param storeProviderName Провайдер контейнера.
 * @param cryptProviderName Провайдер шифрования.
 * @throws Exception
 */
public static byte[] decrypt(String encryptedFile, String recipientAlias,
    char[] recipientPassword, String pubKeyAlgorithm, String agreeAlgorithm,
    String storeType, String storeProviderName, String cryptProviderName)
    throws Exception {

    // cms-сообщение для расшифрования
    final byte[] buffer = Array.readFile(encryptedFile);

    //разбор CMS-сообщения
    Asn1BerDecodeBuffer dbuf = new Asn1BerDecodeBuffer(buffer);
    final ContentInfo all = new ContentInfo();
    all.decode(dbuf);
    dbuf.reset();
    final EnvelopedData cms = (EnvelopedData) all.content;

    KeyTransRecipientInfo keytrans = new KeyTransRecipientInfo();
    if (cms.recipientInfos.elements[0].getChoiceID() == RecipientInfo._KTRI)
        keytrans =
            (KeyTransRecipientInfo) (cms.recipientInfos.elements[0].getElement());
    final Asn1BerEncodeBuffer ebuf = new Asn1BerEncodeBuffer();
    dbuf = new Asn1BerDecodeBuffer(keytrans.encryptedKey.value);
    final GostR3410_KeyTransport encrKey = new GostR3410_KeyTransport();
    encrKey.decode(dbuf);
    dbuf.reset();
    encrKey.sessionEncryptedKey.encode(ebuf);
    final byte[] wrapKey = ebuf.getMsgCopy();
    ebuf.reset();
    encrKey.transportParameters.ephemeralPublicKey.encode(ebuf);
    final byte[] encodedPub = ebuf.getMsgCopy();
    ebuf.reset();
    final byte[] sv = encrKey.transportParameters.ukm.value;
    final Gost28147_89_Parameters params =
        (Gost28147_89_Parameters) cms.encryptedContentInfo.contentEncryptionAlgorithm.parameters;
    final byte[] iv = params.iv.value;
    final OID cipherOID = new OID(params.encryptionParamSet.value);
    final byte[] text = cms.encryptedContentInfo.encryptedContent.value;

    //Загрузка хранилища
    final KeyStore hdImageStore = KeyStore.getInstance(storeType, storeProviderName);
    hdImageStore.load(null, null);

    //получатель - закрытый ключ
    final PrivateKey responderKey = (PrivateKey)
        hdImageStore.getKey(recipientAlias, recipientPassword);

    //отправитель - открытый ключ из cms
    final X509EncodedKeySpec pspec = new X509EncodedKeySpec(encodedPub);
    final KeyFactory kf = KeyFactory.getInstance(pubKeyAlgorithm, storeProviderName);
    final PublicKey senderPublic = kf.generatePublic(pspec);

    // выработка ключа согласования получателем
    final KeyAgreement responderKeyAgree =
        KeyAgreement.getInstance(agreeAlgorithm, cryptProviderName);
    responderKeyAgree.init(responderKey, new IvParameterSpec(sv), null);
    responderKeyAgree.doPhase(senderPublic, true);
    final SecretKey responderSecret = responderKeyAgree
        .generateSecret(CMStools.SEC_KEY_ALG_NAME);

    // Расшифрование симметричного ключа.
    final Cipher cipher = Cipher.getInstance(CIPHER_MODE, cryptProviderName);
    cipher.init(Cipher.UNWRAP_MODE, responderSecret, (SecureRandom) null);
    final SecretKey simmKey = (SecretKey) cipher
        .unwrap(wrapKey, null, Cipher.SECRET_KEY);

    // Расшифрование текста на симметричном ключе.
    final GostCipherSpec spec = new GostCipherSpec(iv, cipherOID);
    cipher.init(Cipher.DECRYPT_MODE, simmKey, spec, null);
    final byte[] result = cipher.doFinal(text, 0, text.length);

    return result;
}

/**
 * Расшифрование и проверка подписи.
 *
 * @param encryptedFile Зашифрованная подпись.
 * @param recipientAlias Алиас ключа получателя.
 * @param recipientPassword Пароль к ключу получателя.
 * @param pubKeyAlgorithm Алгоритм открытого ключа.
 * @param agreeAlgorithm Алгоритм согласования.
 * @throws Exception
 */
public static void main_(String encryptedFile, String recipientAlias,
    char[] recipientPassword, String pubKeyAlgorithm, String agreeAlgorithm)
    throws Exception {

    byte[] result = decrypt(encryptedFile, recipientAlias, recipientPassword,
        pubKeyAlgorithm, agreeAlgorithm, CMStools.STORE_TYPE, JCP.PROVIDER_NAME,
            CryptoProvider.PROVIDER_NAME);

    // if result = signedData ($CMS_FILE)
    CMSVerify.CMSVerifyEx(result, null, null, JCP.PROVIDER_NAME);
}
}
