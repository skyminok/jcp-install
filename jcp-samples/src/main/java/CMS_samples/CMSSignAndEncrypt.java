/**
 * $RCSfile$
 * version $Revision$
 * created 21.05.2008 12:17:43 by kunina
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
import com.objsys.asn1j.runtime.Asn1Null;
import com.objsys.asn1j.runtime.Asn1ObjectIdentifier;
import com.objsys.asn1j.runtime.Asn1OctetString;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.*;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax.Gost28147_89_EncryptedKey;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax.Gost28147_89_IV;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax.Gost28147_89_ParamSet;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax.Gost28147_89_Parameters;
import ru.CryptoPro.JCP.ASN.Gost28147_89_EncryptionSyntax._Gost28147_89_EncryptionSyntaxValues;
import ru.CryptoPro.JCP.ASN.GostR3410_EncryptionSyntax.GostR3410_KeyTransport;
import ru.CryptoPro.JCP.ASN.GostR3410_EncryptionSyntax.GostR3410_TransportParameters;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.AlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.CertificateSerialNumber;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Name;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.RDNSequence;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.SubjectPublicKeyInfo;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Util.JCPInit;
import ru.CryptoPro.JCP.params.*;
import ru.CryptoPro.JCP.tools.AlgorithmUtility;
import ru.CryptoPro.JCP.tools.Array;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.x500.X500Principal;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.X509Certificate;

/**
 * CryptSignAndEncryptMessage.
 * <br>
 * CryptDecryptAndVerifyMessage with:
 * <br>
 * csptest -sfse -decrypt -in envCMS.txt -out envCMS.txt.decr
 *
 * @author Copyright 2004-2009 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class CMSSignAndEncrypt {

private static final String CMS_FILE = "cms_data_encr_sgn";

// ГОСТ Р 34.10-2001
protected static final String CMS_FILE_PATH =
    CMStools.TEST_PATH + CMStools.SEPAR + CMS_FILE;

// ГОСТ Р 34.10-2012 (256)
protected static final String CMS_FILE_PATH_2012_256 =
    CMStools.TEST_PATH + CMStools.SEPAR + CMS_FILE + "_2012_256";

// ГОСТ Р 34.10-2012 (512)
protected static final String CMS_FILE_PATH_2012_512 =
    CMStools.TEST_PATH + CMStools.SEPAR + CMS_FILE + "_2012_512";

protected static final String CIPHER_MODE = "GOST28147/CFB/NoPadding";

/**
 * вектор усложнения ключа согласования
 */
private static final byte[] sv =
        {0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11, 0x11};

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

    main(CMStools.SIGN_KEY_NAME, CMStools.SIGN_KEY_PASSWORD, CMStools.RECIP_KEY_NAME,
        CMStools.KEY_ALG_NAME, AlgIdSpec.getDefaultCryptParams(), CMS_FILE_PATH);

    main(CMStools.SIGN_KEY_NAME_2012_256, CMStools.SIGN_KEY_PASSWORD_2012_256,
        CMStools.RECIP_KEY_NAME_2012_256, CMStools.KEY_ALG_NAME_2012_256,
            CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z),
                CMS_FILE_PATH_2012_256);

    main(CMStools.SIGN_KEY_NAME_2012_512, CMStools.SIGN_KEY_PASSWORD_2012_512,
        CMStools.RECIP_KEY_NAME_2012_512, CMStools.KEY_ALG_NAME_2012_512,
            CryptParamsSpec.getInstance(CryptParamsSpec.Rosstandart_TC26_Z),
                CMS_FILE_PATH_2012_512);

}

/**
 * Формирование подписи и зашифрование.
 *
 * @param senderAlias Алиас ключа отправителя.
 * @param senderPassword Пароль к ключу отправителя.
 * @param recipientAlias Алиас ключа (сертификата) получателя.
 * @param agreeAlgorithm Алгоритм согласования.
 * @param crp_params Параметры ключа шифрования.
 * @param outFileName Файл с подписью.
 * @throws Exception
 */
public static void main(String senderAlias, char[] senderPassword,
    String recipientAlias, String agreeAlgorithm, ParamsInterface
    crp_params, String outFileName) throws Exception {

    // данные для подписи и последющего шифрования
    final byte[] data = Array.readFile(CMStools.DATA_FILE_PATH);

    //Загрузка хранилища
    final KeyStore hdImageStore = KeyStore.getInstance(CMStools.STORE_TYPE);
    hdImageStore.load(null, null);

    //ключ отправителя
    final PrivateKey senderKey =
        (PrivateKey) hdImageStore.getKey(senderAlias, senderPassword);
    final X509Certificate publicSenderCert =
        (X509Certificate) hdImageStore.getCertificate(senderAlias);

    //ключ получателя
    final X509Certificate publicCert =
        (X509Certificate) hdImageStore.getCertificate(recipientAlias);

    String keyAlg       = senderKey.getAlgorithm();
    String keyDigestOid = AlgorithmUtility.keyAlgToDigestOid(keyAlg);
    String keyOid       = AlgorithmUtility.keyAlgToKeyAlgorithmOid(keyAlg); // алгоритм ключа подписи
    String signOid      = AlgorithmUtility.keyAlgToSignatureOid(keyAlg);

    //создание SignedData
    final ContentInfo contentSign = new ContentInfo();
    contentSign.contentType =
        new Asn1ObjectIdentifier(
            new OID(CMStools.STR_CMS_OID_SIGNED).value);
    final SignedData signedData = new SignedData();
    contentSign.content = signedData;
    signedData.version = new CMSVersion(1);

    // digest
    signedData.digestAlgorithms = new DigestAlgorithmIdentifiers(1);
    final DigestAlgorithmIdentifier a = new DigestAlgorithmIdentifier(
        new OID(keyDigestOid).value);
    a.parameters = new Asn1Null();
    signedData.digestAlgorithms.elements[0] = a;
    signedData.encapContentInfo =
            new EncapsulatedContentInfo(new Asn1ObjectIdentifier(
                new OID(CMStools.STR_CMS_OID_DATA).value),
                    new Asn1OctetString(data));

    // certificates
    signedData.certificates = new CertificateSet(1);
    signedData.certificates.elements = new CertificateChoices[1];
    final Certificate certificate = new Certificate();
    final Asn1BerDecodeBuffer decodeBuffer =
        new Asn1BerDecodeBuffer(publicSenderCert.getEncoded());
    certificate.decode(decodeBuffer);
    signedData.certificates.elements[0] = new CertificateChoices();
    signedData.certificates.elements[0].set_certificate(certificate);

    // Signature.getInstance
    final Signature signature = Signature.getInstance(signOid);

    // signer infos
    signedData.signerInfos = new SignerInfos(1);
    signature.initSign(senderKey);
    signature.update(data);
    final byte[] sign = signature.sign();
    signedData.signerInfos.elements[0] = new SignerInfo();
    signedData.signerInfos.elements[0].version = new CMSVersion(1);
    signedData.signerInfos.elements[0].sid = new SignerIdentifier();

    final byte[] encodedName =
        publicSenderCert.getIssuerX500Principal().getEncoded();
    final Asn1BerDecodeBuffer nameBuf =
        new Asn1BerDecodeBuffer(encodedName);
    final Name name = new Name();
    name.decode(nameBuf);

    final CertificateSerialNumber num = new CertificateSerialNumber(
            publicSenderCert.getSerialNumber());
    signedData.signerInfos.elements[0].sid.set_issuerAndSerialNumber(
            new IssuerAndSerialNumber(name, num));
    signedData.signerInfos.elements[0].digestAlgorithm =
            new DigestAlgorithmIdentifier(new OID(keyDigestOid).value);
    signedData.signerInfos.elements[0].digestAlgorithm.parameters =
            new Asn1Null();
    signedData.signerInfos.elements[0].signatureAlgorithm =
            new SignatureAlgorithmIdentifier(new OID(keyOid).value);
    signedData.signerInfos.elements[0].signatureAlgorithm.parameters =
            new Asn1Null();
    signedData.signerInfos.elements[0].signature = new SignatureValue(sign);

    // encode
    final Asn1BerEncodeBuffer asnBuf = new Asn1BerEncodeBuffer();
    contentSign.encode(asnBuf, true);

    //данные для envelopedData
    final byte[] buffer = asnBuf.getMsgCopy();

    //===============================

    final PublicKey responderPublic = publicCert.getPublicKey();

    // выработка ключа согласования отправителем
    final KeyAgreement senderKeyAgree =
            KeyAgreement.getInstance(agreeAlgorithm);
    senderKeyAgree.init(senderKey, new IvParameterSpec(sv), null);
    senderKeyAgree.doPhase(responderPublic, true);
    final SecretKey alisaSecret =
            senderKeyAgree.generateSecret(CMStools.SEC_KEY_ALG_NAME);

    // Генерирование симметричного ключа с параметрами шифрования из контрольной панели.
    final KeyGenerator kg = KeyGenerator.getInstance(CMStools.SEC_KEY_ALG_NAME);
    //final ParamsInterface paramss = AlgIdSpec.getDefaultCryptParams();
    kg.init(/*paramss*/ crp_params);
    final SecretKey simm = kg.generateKey();

    // Зашифрование текста на симметричном ключе.
    final Cipher cipher = Cipher.getInstance(CIPHER_MODE);
    cipher.init(Cipher.ENCRYPT_MODE, simm, (SecureRandom) null);
    final byte[] iv = cipher.getIV();
    final byte[] text = cipher.doFinal(buffer, 0, buffer.length);

    //Зашифрование симметричного ключа на ключе согласования отправителя
    cipher.init(Cipher.WRAP_MODE, alisaSecret, (SecureRandom) null);
    final byte[] key = cipher.wrap(simm);

    //формирование CMS-сообщения
    final ContentInfo all = new ContentInfo();
    all.contentType =
            new Asn1ObjectIdentifier(
                    new OID(CMStools.STR_CMS_OID_ENVELOPED).value);
    final EnvelopedData cms = new EnvelopedData();
    all.content = cms;

    cms.version = new CMSVersion(0);

    cms.recipientInfos = new RecipientInfos(1);
    cms.recipientInfos.elements = new RecipientInfo[1];
    cms.recipientInfos.elements[0] = new RecipientInfo();

    final KeyTransRecipientInfo keytrans = new KeyTransRecipientInfo();

    keytrans.version = new CMSVersion(0);

    final Asn1BerEncodeBuffer ebuf = new Asn1BerEncodeBuffer();
    final SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo();
    final Asn1BerDecodeBuffer dbuff = new Asn1BerDecodeBuffer(
            publicSenderCert.getPublicKey().getEncoded());
    spki.decode(dbuff);
    dbuff.reset();
    final AlgIdInterface algid = new AlgIdSpec(spki.algorithm);
    final AlgorithmIdentifier id = (AlgorithmIdentifier) algid.getDecoded();
    id.encode(ebuf);
    Asn1BerDecodeBuffer dbuf = new Asn1BerDecodeBuffer(ebuf.getMsgCopy());
    keytrans.keyEncryptionAlgorithm = new KeyEncryptionAlgorithmIdentifier();
    keytrans.keyEncryptionAlgorithm.decode(dbuf);
    ebuf.reset();
    dbuf.reset();
    keytrans.rid = new RecipientIdentifier();
    final IssuerAndSerialNumber issuer = new IssuerAndSerialNumber();
    final X500Principal issuerName = publicCert.getIssuerX500Principal();
    dbuf = new Asn1BerDecodeBuffer(issuerName.getEncoded());
    issuer.issuer = new Name();
    final RDNSequence rnd = new RDNSequence();
    rnd.decode(dbuf);
    issuer.issuer.set_rdnSequence(rnd);
    issuer.serialNumber = new CertificateSerialNumber(
            publicCert.getSerialNumber());
    keytrans.rid.set_issuerAndSerialNumber(issuer);
    dbuf.reset();
    final GostR3410_KeyTransport encrKey = new GostR3410_KeyTransport();
    dbuf = new Asn1BerDecodeBuffer(key);
    encrKey.sessionEncryptedKey = new Gost28147_89_EncryptedKey();
    encrKey.sessionEncryptedKey.decode(dbuf);
    dbuf.reset();
    encrKey.transportParameters = new GostR3410_TransportParameters();
    encrKey.transportParameters.encryptionParamSet =
            new Gost28147_89_ParamSet(algid.getCryptParams()
                    .getOID().value);
    encrKey.transportParameters.ephemeralPublicKey = new SubjectPublicKeyInfo();
    dbuf = new Asn1BerDecodeBuffer(
            publicSenderCert.getPublicKey().getEncoded());
    encrKey.transportParameters.ephemeralPublicKey.decode(dbuf);
    dbuf.reset();
    encrKey.transportParameters.ukm = new Asn1OctetString(sv);
    encrKey.encode(ebuf);
    keytrans.encryptedKey = new EncryptedKey(ebuf.getMsgCopy());
    ebuf.reset();
    cms.recipientInfos.elements[0].set_ktri(keytrans);

    cms.encryptedContentInfo = new EncryptedContentInfo();
    final OID contentType = new OID(CMStools.STR_CMS_OID_DATA);
    cms.encryptedContentInfo.contentType = new ContentType(contentType.value);
    final Gost28147_89_Parameters params = new Gost28147_89_Parameters();
    params.iv = new Gost28147_89_IV(iv);
    params.encryptionParamSet =
            new Gost28147_89_ParamSet(/*paramss*/crp_params.getOID().value);
    cms.encryptedContentInfo.contentEncryptionAlgorithm =
            new ContentEncryptionAlgorithmIdentifier(
                _Gost28147_89_EncryptionSyntaxValues.id_Gost28147_89,
                    params);
    cms.encryptedContentInfo.encryptedContent = new EncryptedContent(text);

    all.encode(ebuf);
    Array.writeFile(outFileName, ebuf.getMsgCopy());
    if (CMStools.logger != null) {
        CMStools.logger.info("ok");
    }
}
}
