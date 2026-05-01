package cmsutil.asn1.encrypted;

import cmsutil.tools.ProviderUtil;
import ru.CryptoPro.Crypto.CryptoProvider;
import ru.CryptoPro.JCP.spec.GostCipherSpec;
import ru.CryptoPro.JCP.params.CryptParamsSpec;
import cmsutil.asn1.CMSCryptographyException;
import cmsutil.tools.OID;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;

/**
 * Класс, осуществлящий непосредственно зашифрование и расшифрование данных.
 * Класс инициализируется при создании/разборе ASN1-структуры сообщения CMS Enveloped,
 * после чего используется для шифрования/расшифрования необходимого количества данных.
 */
public class CipherProcessor {
    /** Ключ шифрования данных по алгоритму ГОСТ 28147-89*/
    private SecretKey key;
    /** Объектный идентификатор используемого набора узлов замены*/
    private ru.CryptoPro.JCP.params.OID internalCryptOID;
    /** Длина шифруемых/расшифровываемых данных*/
    private long textLength; // Так как шифруем CFB - длина при шифровании/расшифровании одна и та же.
    /** Синхропосылка.*/
    private byte iv[];
    /** Объект шифратора*/
    Cipher cipher;

    /**
     * Конструктор, используемый при создании CMS Enveloped сообщения.
     * Инициализирует шифратор на шифрование по алгоритму ГОСТ 28147-89 в режиме CFB.
     * Также сохраняет синхропосылку.
     * @param textLen Длина шифруемых данных.
     * @param gostKey Ключ шифрования данных ГОСТ 28147-89.
     * @param cipherOID Объектный идентификатор набора узлов замены.
     * @throws CMSCryptographyException Ошибка создания объекта шифратора.
     */
    public CipherProcessor(long textLen, SecretKey gostKey, OID cipherOID,
        String provider) throws CMSCryptographyException {
        this.key = gostKey;
        internalCryptOID = new ru.CryptoPro.JCP.params.OID(cipherOID.toString());
        this.textLength = textLen;
        try {
            String enc_provider = ProviderUtil.findEncryptionProvider(provider);
            cipher = Cipher.getInstance("GOST28147/CFB/NoPadding", enc_provider);
            CryptParamsSpec spec = CryptParamsSpec.getInstance(internalCryptOID);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            iv = cipher.getIV();
        }
        catch (NoSuchAlgorithmException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (NoSuchProviderException e) {
            throw new CMSCryptographyException((e.getMessage()));
        }
        catch (NoSuchPaddingException e) {
            throw new CMSCryptographyException((e.getMessage()));
        }
        catch (InvalidKeyException e) {
            throw new CMSCryptographyException((e.getMessage()));
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new CMSCryptographyException((e.getMessage()));
        }
        finally {

        }
    }

    /**
     * Конструктор, используемый при разборе CMS Enveloped сообщения.
     * Инициализирует шифратор на расшифрование по алгоритму ГОСТ 28147-89 в режиме CFB.
     * @param textLen Длина расшифруемых данных.
     * @param gostKey Ключ шифрования данных ГОСТ 28147-89.
     * @param cipherOID Объектный идентификатор набора узлов замены.
     * @param newIv Синхропосылка.
     * @throws CMSCryptographyException Ошибка создания объекта шифратора.
     */
    public CipherProcessor(long textLen, SecretKey gostKey, OID cipherOID,
        byte[] newIv, String provider) throws CMSCryptographyException
    {
        this.key = gostKey;
        internalCryptOID = new ru.CryptoPro.JCP.params.OID(cipherOID.toString());
        this.textLength = textLen;
        iv = newIv;
        try {
            String enc_provider = ProviderUtil.findEncryptionProvider(provider);
            cipher = Cipher.getInstance("GOST28147/CFB/NoPadding", enc_provider);
            CryptParamsSpec uzspec = CryptParamsSpec.getInstance(internalCryptOID);
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            GostCipherSpec spec = new GostCipherSpec(ivspec, uzspec);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
        }
        catch (NoSuchAlgorithmException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (NoSuchProviderException e) {
            throw new CMSCryptographyException((e.getMessage()));
        }
        catch (NoSuchPaddingException e) {
            throw new CMSCryptographyException((e.getMessage()));
        }catch (InvalidKeyException e) {
            throw new CMSCryptographyException((e.getMessage()));
        }
        catch (InvalidAlgorithmParameterException e) {
            throw new CMSCryptographyException((e.getMessage()));
        }
        finally {

        }
    }

    /**
     * Метод, осуществляющий обработку участка данных.
     * @param text Байтовый массив, содержащий данные.
     * @return Байтовый массив, содержащий обработанные данные.
     * @throws CMSCryptographyException Ошибка зашифрования или расшифрования.
     */
    public byte[] crypt(byte[] text) throws CMSCryptographyException {
        return cipher.update(text);
    }

    /**
     * Метод, осуществляющий обработку финального участка данных.
     * @param text Байтовый массив, содержащий данные.
     * @param num Размер участка, которые необходимо обработать.
     * @return Байтовый массив, содержащий обработанные данные.
     * @throws CMSCryptographyException Ошибка зашифрования или расшифрования.
     */
    public byte[] cryptFinal(byte[] text, int num) throws CMSCryptographyException {
        try {
            return cipher.doFinal(text, 0, num);
        }
        catch (IllegalBlockSizeException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        catch (BadPaddingException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
        finally {

        }
    }

    /**
     * Метод, возвращающий использованную синхропосылку.
     * @return Байтовый массив, содержащий синхропосылку.
     */
    public byte[] getIv() {
        return iv;
    }

    /**
     * Метод, возвращающий общую длину открытого текста/шифртекста.
     * @return Длина открытого текста/шифртекста.
     */
    public long getTextLength() {
        return textLength;
    }
}
