package cmsutil.asn1;

import cmsutil.tools.ProviderUtil;
import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1Exception;
import ru.CryptoPro.Crypto.CryptoProvider;
import cmsutil.asn1.base.ASNCommon;
import cmsutil.asn1.base.ASNInteger;
import cmsutil.asn1.base.ASNSequence;
import cmsutil.asn1.base.ASNSet;
import cmsutil.asn1.encrypted.CipherProcessor;
import cmsutil.asn1.encrypted.EncryptedContentInfo;
import cmsutil.asn1.kari.RecipientCertInfo;
import cmsutil.asn1.kari.RecipientInfos;
import cmsutil.tools.OID;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.SubjectPublicKeyInfo;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.Key.KeyInterface;
import ru.CryptoPro.JCP.Key.SpecKey;
import ru.CryptoPro.JCP.params.*;
import ru.CryptoPro.JCP.tools.Platform;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование структуры EnvelopedData.
 */
public class EnvelopedData extends ASNSequence {
    /** Версия структуры */
    private ASNInteger version;
    /** Структура RecipientInfos */
    private RecipientInfos recipientInfos;
    /** Структура EncryptedContentInfo*/
    private EncryptedContentInfo encryptedContentInfo;

    private final static OID cipherParamOID = OID.OID_Crypt_VerbaO;

    /**
     * Конструктор, используемый при создании сообщения CMS Enveloped.
     * @param textLength Длина открытого текста
     * @param recipientCertificate Сертификат открытого ключа получателя.
     * @throws CMSCryptographyException Ошибка криптографической операции.
     * @throws ASNDecodeException Ошибка декодирования.
     */
    public EnvelopedData(long textLength, X509Certificate recipientCertificate, String provider)
        throws CMSCryptographyException, ASNDecodeException
    {
        version = new ASNInteger(new BigInteger("2"));
        SecretKey cek;
        try {
            String enc_provider = ProviderUtil.findEncryptionProvider(provider);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(CryptoProvider.GOST_CIPHER_NAME, enc_provider);
            EllipticParamsInterface ellipticParams = getAdditionalParams(recipientCertificate);
            if (!provider.equalsIgnoreCase(JCP.PROVIDER_NAME)) { // Java CSP
                keyGenerator.init(ellipticParams);
            }
            cek = keyGenerator.generateKey();
        }
        catch (NoSuchAlgorithmException e) {
            throw new CMSCryptographyException(e.getMessage());
        } catch (NoSuchProviderException e) {
            throw new CMSCryptographyException(e.getMessage());
        } catch (Asn1Exception e) {
            throw new CMSCryptographyException(e.getMessage());
        } catch (CertificateEncodingException e) {
            throw new CMSCryptographyException(e.getMessage());
        } catch (IOException e) {
            throw new CMSCryptographyException(e.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        recipientInfos = new RecipientInfos(cek, recipientCertificate, provider);
        encryptedContentInfo = new EncryptedContentInfo(textLength, cek, cipherParamOID, provider);
        subs = new ArrayList<ASNCommon>(3);
        subs.add(version);
        subs.add(recipientInfos);
        subs.add(encryptedContentInfo);
        encode();
    }

    /**
     * Дополнительные параметры.
     *
     * @param recipientCertificate Сертификат получателя.
     * @throws Asn1Exception
     * @throws IOException
     * @throws CertificateEncodingException
     */
    private EllipticParamsInterface getAdditionalParams(X509Certificate
        recipientCertificate) throws Asn1Exception, IOException,
        CertificateEncodingException {

        final SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo();
        final PublicKey recipientPublicKey = recipientCertificate.getPublicKey();

        final Asn1BerDecodeBuffer dbuff = new Asn1BerDecodeBuffer(
            recipientPublicKey.getEncoded());

        spki.decode(dbuff);
        dbuff.reset();

        final AlgIdInterface algid = new AlgIdSpec(spki.algorithm);
        return (EllipticParamsInterface) algid.getSignParams();

    }

    /**
     * Конструктор, используемый при разборе сообщения CMS Enveloped.
     * @param structure ASN1-структура, соответствующая классу.
     * @param recipientPrivateKey Секретный ключ получателя ГОСТ Р 34.10-2001.
     * @throws ASNDecodeException Ошибка декодирования.
     * @throws CMSCryptographyException Ошибка криптографической операции.
     */
    public EnvelopedData(ASNSequence structure, PrivateKey recipientPrivateKey, String provider)
            throws ASNDecodeException, CMSCryptographyException
    {
        this.subs = structure.getSubStructures();
        this.encodedValue = structure.getEncoded();
        this.realInternalLength = structure.getRealInternalLength();
        this.realEncodedLength = structure.getRealEncodedLength();
        this.virtualEncodedLength = structure.getVirtualEncodedLength();
        this.virtualInternalLength = structure.getVirtualInternalLength();
        if (!checkConsist())
            throw new ASNDecodeException("Structure EnvelopedData is corrupted!");
        this.version = (ASNInteger)subs.get(0);
        if (!this.version.getValue().equals(BigInteger.valueOf(2)))
            throw new ASNDecodeException("Structure EnvelopedData is corrupted!");
        this.recipientInfos = new RecipientInfos((ASNSet)subs.get(1), recipientPrivateKey, provider);
        this.encryptedContentInfo = new EncryptedContentInfo((ASNSequence)subs.get(2), this.recipientInfos.getCEK(), provider);
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        return (subs != null) && (subs.size() == 3) && (subs.get(0) instanceof ASNInteger) &&
                (subs.get(1) instanceof ASNSet) && (subs.get(2) instanceof ASNSequence);
    }

    /**
     * Метод, получающий высокоуровневый объект шифратора.
     * @return Высокоуровневый объект шифратора.
     */
    public CipherProcessor getCipherProcessor() {
        return encryptedContentInfo.getCipherProcessor();
    }

    /**
     * Метод, получающий информацию о сертификате получателя.
     * @return Информацию о сертификате получателя.
     */
    public RecipientCertInfo getRecipientCertInfo() {
        return recipientInfos.getRecipientCertInfo();
    }
}
