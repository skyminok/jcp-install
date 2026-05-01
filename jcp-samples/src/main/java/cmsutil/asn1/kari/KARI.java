package cmsutil.asn1.kari;

import cmsutil.tools.ProviderUtil;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.JCP;
import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.CMSCryptographyException;
import cmsutil.asn1.base.*;
import cmsutil.tools.OID;
import ru.CryptoPro.JCP.Key.InternalGostPublicKey;

import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование и раскодирование структуры KeyAgreeRecipientInfo
 */
public class KARI extends ASNContextSpecificConstructed {
    /** Версия структуры */
    private ASNInteger version;
    /** Структура Originator, информация об отправителе*/
    private Originator originator;
    /** Ключевой материал, используемый в алгоритме ВКО */
    private UKM ukm;
    /** Алгоритм и наборы параметров шифрования ключа шифрования данных*/
    private KeyEncryptionAlgorithmIdentifier keyEncryptionAlgorithmIdentifier;
    /** Зашифрованные ключи для получателей*/
    private RecipientEncryptedKeys recipientEncryptedKeys;

    /** Объектный идентификатор набора параметров алгоритма шифрования ключа.*/
    private final static OID wrapParamOID = OID.OID_Crypt_VerbaO;
    private final static OID wrapParamOID_2012 = OID.OID_Gost28147_89_Rosstandart_TC26_Z_ParamSet;

    /**
     * Конструктор, используемый при создании сообщения CMS Enveloped. Осуществляет выработку UKM с помощью
     * ПДСЧ JCP, вырабатывает эфемерную ключевую пару отправителя, вырабатывает ключ шифрования ключа с помощью
     * алгоритма ВКО, после чего кодирует полученные данные.
     * @param cek Ключ шифрования данных.
     * @param keyWrapOID Объектный идентификатор алгоритма шифрования ключа.
     * @param recipientCertificate Сертификат получателя сообщения.
     * @throws ASNDecodeException Ошибка раскодирования.
     * @throws CMSCryptographyException Ошибка выработки ключа шифрования ключа.
     */
    public KARI(SecretKey cek, OID keyWrapOID, X509Certificate recipientCertificate, String provider)
            throws ASNDecodeException, CMSCryptographyException {
        //Diffie-Hellman
        byte[] ukmVal = new byte[8];
        SecretKey kek;
        KeyPair senderEphemeralKeyPair;
        OID wrapDhOid;
        OID keyWrapParamOID;
        try {
            //Create UKM
            SecureRandom rnd = SecureRandom.getInstance(JCP.CP_RANDOM, provider);
            rnd.nextBytes(ukmVal);
            IvParameterSpec ukmSpec = new IvParameterSpec(ukmVal);

            // Certificate algorithm
            final String keyAlgName = recipientCertificate.getPublicKey().getAlgorithm();
            String keyPairAlgName;

            if (keyAlgName.equalsIgnoreCase(JCP.GOST_EL_2012_256_NAME) ||
                keyAlgName.equalsIgnoreCase(JCP.GOST_DH_2012_256_NAME)) {
                keyPairAlgName = JCP.GOST_EPH_DH_2012_256_NAME;
                wrapDhOid = OID.gostR3410ESDHOID_2012;
                keyWrapParamOID = wrapParamOID_2012;
            } // if
            else if (keyAlgName.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME) ||
                keyAlgName.equalsIgnoreCase(JCP.GOST_DH_2012_512_NAME)) {
                keyPairAlgName = JCP.GOST_EPH_DH_2012_512_NAME;
                wrapDhOid = OID.gostR3410ESDHOID_2012;
                keyWrapParamOID = wrapParamOID_2012;
            } // if
            else {
                keyPairAlgName = JCP.GOST_EL_DH_EPH_NAME;
                wrapDhOid = OID.gostR3410ESDHOID;
                keyWrapParamOID = wrapParamOID;
            } // else

            //Create Ephemeral sender key pair
            String enc_provider = ProviderUtil.findEncryptionProvider(provider);
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyPairAlgName, enc_provider);
            AlgorithmParameterSpec dhSpec = ((InternalGostPublicKey)recipientCertificate.getPublicKey()).getSpec().getParams();
            keyPairGenerator.initialize(dhSpec);
            senderEphemeralKeyPair = keyPairGenerator.generateKeyPair();

            //Do DH
            KeyAgreement keyAgreement = KeyAgreement.getInstance(keyAlgName, enc_provider);
            keyAgreement.init(senderEphemeralKeyPair.getPrivate(), ukmSpec);
            keyAgreement.doPhase(recipientCertificate.getPublicKey(), true);
            kek = keyAgreement.generateSecret(CryptoProvider.GOST_CIPHER_NAME);

            //Encoding
            type = 1;
            version = new ASNInteger(new BigInteger("3"));
            originator = new Originator(senderEphemeralKeyPair.getPublic());
            ukm = new UKM(ukmVal);
            keyEncryptionAlgorithmIdentifier = new KeyEncryptionAlgorithmIdentifier(wrapDhOid, keyWrapOID, keyWrapParamOID);
            RecipientEncryptedKey recipientEncryptedKey[] = new RecipientEncryptedKey[1];
            recipientEncryptedKey[0] = new RecipientEncryptedKey(
                new RecipientCertInfo(recipientCertificate.getIssuerX500Principal(), recipientCertificate.getSerialNumber()),
                new Gost28147EncryptedKey(cek, kek, keyEncryptionAlgorithmIdentifier.getKeyWrapAlgorithm(), provider));
            recipientEncryptedKeys = new RecipientEncryptedKeys(recipientEncryptedKey);
            subs = new ArrayList<ASNCommon>(5);
            subs.add(version);
            subs.add(originator);
            subs.add(ukm);
            subs.add(keyEncryptionAlgorithmIdentifier);
            subs.add(recipientEncryptedKeys);
            encode();

        }
        catch (NoSuchAlgorithmException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (NoSuchProviderException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (InvalidKeyException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
    }

    /**
     * Конструктор, используемый при разборе сообщения CMS Enveloped. Раскодирует данные, после чего
     * вырабатывает ключ шифрования ключа с помощью алгоритма ВКО.
     * @param contextSpecificConstructed ASN1-структура, связанная с классом.
     * @param recipientPrivateKey - Секретный долговременнный ключ ключевого обмена получателя ГОСТ Р 34.10-2001.
     * @throws ASNDecodeException Ошибка раскодирования.
     * @throws CMSCryptographyException Ошибка выработки ключа шифрования ключа.
     */
    public KARI(ASNContextSpecificConstructed contextSpecificConstructed,
        PrivateKey recipientPrivateKey, String provider)
        throws ASNDecodeException, CMSCryptographyException
    {
        this.subs = contextSpecificConstructed.getSubStructures();
        this.encodedValue = contextSpecificConstructed.getEncoded();
        this.type = (byte)(encodedValue[0] & 0x0f);
        this.realInternalLength = contextSpecificConstructed.getRealInternalLength();
        this.realEncodedLength = contextSpecificConstructed.getRealEncodedLength();
        this.virtualEncodedLength = contextSpecificConstructed.getVirtualEncodedLength();
        this.virtualInternalLength = contextSpecificConstructed.getVirtualInternalLength();
        if (!checkConsist())
            throw new ASNDecodeException("Structure KARI is corrupted!");
        this.version = (ASNInteger)subs.get(0);
        this.originator = new Originator((ASNContextSpecificConstructed)subs.get(1), provider);
        this.ukm = new UKM((ASNContextSpecificConstructed)subs.get(2));
        this.keyEncryptionAlgorithmIdentifier = new KeyEncryptionAlgorithmIdentifier((ASNSequence)subs.get(3));

        //Diffie-Hellman
        SecretKey kek;
        try {
            String enc_provider = ProviderUtil.findEncryptionProvider(provider);
            IvParameterSpec spec = new IvParameterSpec(ukm.getUKM());
            KeyAgreement keyAgreement = KeyAgreement.getInstance(recipientPrivateKey.getAlgorithm(), enc_provider);
            keyAgreement.init(recipientPrivateKey, spec);
            keyAgreement.doPhase(originator.getPublicKey(), true);
            kek = keyAgreement.generateSecret(CryptoProvider.GOST_CIPHER_NAME);
        }
        catch (NoSuchAlgorithmException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (NoSuchProviderException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (InvalidKeyException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        this.recipientEncryptedKeys = new RecipientEncryptedKeys((ASNSequence)subs.get(4),
            kek, keyEncryptionAlgorithmIdentifier.getKeyWrapAlgorithm(), provider);
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        if (type != 1)
            return false;
        if (subs == null)
            return false;
        if (subs.size() != 5)
            return false;
        if ((!(subs.get(0) instanceof ASNInteger)) || (!(subs.get(1) instanceof ASNContextSpecificConstructed)) ||
                (!(subs.get(2) instanceof ASNContextSpecificConstructed)) || (!(subs.get(3) instanceof ASNSequence)) ||
                (!(subs.get(4) instanceof ASNSequence)))
            return false;
        ASNInteger tmp = (ASNInteger)subs.get(0);
        if (!((tmp.getValue()).equals(BigInteger.valueOf(3))))
            return false;
        return true;
    }

    /**
     * Метод, осуществляющий получение ключа шифрования данных.
     * @return Ключ шифрования данных алгоритма ГОСТ 28147-89.
     */
    public SecretKey getCEK() {
        return recipientEncryptedKeys.getRecipientEncryptedKeys()[0].getEncryptedKey().getCEK();
    }

    /**
     * Метод, возвращающий информацию о сертификате получателя, указанную в сообщении.
     * @return Объект класса RecipientCertInfo, содержащий требуюмую информацию.
     */
    public RecipientCertInfo getRecipientCertInfo() {
        return this.recipientEncryptedKeys.getRecipientEncryptedKeys()[0].getCertInfo();
    }
}
