package cmsutil.asn1.kari;


import cmsutil.asn1.ASNEncodeException;
import cmsutil.tools.ProviderUtil;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.params.CryptParamsSpec;
import ru.CryptoPro.JCP.params.OID;
import cmsutil.asn1.CMSCryptographyException;
import cmsutil.asn1.base.ASNOctetString;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Класс, осуществляющий зашифрование и расшифрование ключа ГОСТ 28147-89, используемого для шифрования данных, а также
 * кодирование и раскодирование шифрованного ключа.
 */
public class Gost28147EncryptedKey extends ASNOctetString {
    /** Ключ шифрования данных - Content encryption key*/
    private SecretKey cek;
    /** Ключ шифрования ключа - Key Encryption key*/
    private SecretKey kek;
    /** Алгоритм шифрования ключа*/
    private Gost28147KeyWrapAlgorithm wrapAlgorithm;

    /**
     * Конструктор, используемый при создании CMS Enveloped сообщения. Производит инциализацию шифратора и шифрует ключ
     * шифрования данных, после чего закодирует полученный шифрованный ключ и имитовставку.
     * @param contentEncryptionKey Ключ шифрования данных.
     * @param keyEncryptionKey Ключ шифрования ключа.
     * @param wrapAlg Алгоритм шифрования ключа.
     * @throws CMSCryptographyException Ошибка создания шифратора или шифрования ключа.
     */
    public Gost28147EncryptedKey(SecretKey contentEncryptionKey, SecretKey keyEncryptionKey,
        Gost28147KeyWrapAlgorithm wrapAlg, String provider) throws CMSCryptographyException
    {
        String wrapMode;
        wrapAlgorithm = wrapAlg;
        // TODO можно добавить PRO12_EXPORT и key-wrap-oid с диверсификацией (13.1)
        if (wrapAlgorithm.getKeyWrapOID().equals(cmsutil.tools.OID.noKeyWrapOID))
            wrapMode = "SIMPLE_EXPORT";
        else
            wrapMode = "PRO_EXPORT";
        try {
            cek = contentEncryptionKey;
            kek = keyEncryptionKey;
            String enc_provider = ProviderUtil.findEncryptionProvider(provider);
            Cipher cipher = Cipher.getInstance(CryptoProvider.GOST_CIPHER_NAME + "/" + wrapMode + "/NoPadding", enc_provider);
            CryptParamsSpec spec = CryptParamsSpec.getInstance(new OID(wrapAlgorithm.getKeyWrapParametersOID().toString()));
            cipher.init(Cipher.WRAP_MODE, kek, spec);
            value = cipher.wrap(cek); // value содержит ASN1-структура, содержащая шифрованнй ключ и имитовставку.
            encode();
        }
        catch (NoSuchPaddingException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (NoSuchProviderException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (NoSuchAlgorithmException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (InvalidKeyException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (IllegalBlockSizeException e) {
            throw new CMSCryptographyException(e.getMessage());
        } catch (ASNEncodeException e) {
            throw new CMSCryptographyException(e.getMessage());
        } finally {

        }
    }

    /**
     * Конструктор, используемый при разборе CMS Enveloped сообщения. Раскодирует шифрованный ключ шифрования данных и имитовставку,
     * инициализирует шифратор и расшифровывает ключ шифрования данных с проверкой значения имитовставки.
     * @param octetString ASN1-структура, связанная с классом.
     * @param keyEncryptionKey Ключ шифрования ключа.
     * @param keyWrapAlgorithm Алгоритм шифрования ключа.
     * @throws CMSCryptographyException Ошибка создания шифратора или расшифрования ключа.
     */
    public Gost28147EncryptedKey(ASNOctetString octetString, SecretKey keyEncryptionKey,
        Gost28147KeyWrapAlgorithm keyWrapAlgorithm, String provider) throws CMSCryptographyException {
        wrapAlgorithm = keyWrapAlgorithm;
        encodedValue = octetString.getEncoded();
        this.realInternalLength = octetString.getRealInternalLength();
        this.realEncodedLength = octetString.getRealEncodedLength();
        this.virtualEncodedLength = octetString.getVirtualEncodedLength();
        this.virtualInternalLength = octetString.getVirtualInternalLength();
        value = (byte[])octetString.getValue();
        kek = keyEncryptionKey;
        String wrapMode;
        // TODO можно добавить PRO12_EXPORT и key-wrap-oid с диверсификацией (13.1)
        if (wrapAlgorithm.getKeyWrapOID().equals(cmsutil.tools.OID.noKeyWrapOID))
            wrapMode = "SIMPLE_EXPORT";
        else
            wrapMode = "PRO_EXPORT";
        try {
            String enc_provider = ProviderUtil.findEncryptionProvider(provider);
            Cipher cipher = Cipher.getInstance(CryptoProvider.GOST_CIPHER_NAME + "/" + wrapMode + "/NoPadding", enc_provider);
            CryptParamsSpec spec = CryptParamsSpec.getInstance(new OID(wrapAlgorithm.getKeyWrapParametersOID().toString()));
            cipher.init(Cipher.UNWRAP_MODE, kek, spec);
            cek = (SecretKey)cipher.unwrap(value, null, Cipher.SECRET_KEY); // Если имитовставка неправильная, тут будет исключение.
        }
        catch (NoSuchPaddingException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (NoSuchProviderException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (NoSuchAlgorithmException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (InvalidKeyException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        finally {

        }
    }

    /**
     * Метод, получающий ключ шифрования данных.
     * @return Ключ шифрования данных.
     */
    public SecretKey getCEK() {
        return cek;
    }

    /**
     * Метод, получающий ключ шифрования ключа.
     * @return Ключ шифрования ключа.
     */
    public SecretKey getKEK() {
        return kek;
    }
}
