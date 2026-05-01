package cmsutil.asn1.encrypted;

import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;
import cmsutil.asn1.CMSCryptographyException;
import cmsutil.asn1.base.ASNContextSpecificPrimitive;
import cmsutil.tools.OID;

import javax.crypto.SecretKey;

/**
 * Класс, осуществляющий кодирование "виртуального" объекта шифртекста.
 */
public class EncryptedContext extends ASNContextSpecificPrimitive {

    /**
     * Объект высокоуровневого шифратора.
     */
    private CipherProcessor cipherProcessor;

    /**
     * Конструктор, используемый при закодировании
     * @param newCipherProcessor Объект высокоуровневого шифратора.
     */
    public EncryptedContext(CipherProcessor newCipherProcessor) throws ASNEncodeException {
        super(newCipherProcessor.getTextLength(), 0);
        cipherProcessor = newCipherProcessor;
    }

    /**
     * Конструктор, используемый при раскодировании, осуществляет инициализацию объекта высокоуровневого шифратора.
     * @param contextSpecific ASN1-структура, связанная с классом.
     * @param gostKey Ключ шифрования ГОСТ 28147-89.
     * @param cipherOID Объектный идентификатор используемых узлов замены.
     * @param iv Синхропосылка.
     * @throws CMSCryptographyException Ошибка инициализации шифратора.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    public EncryptedContext(ASNContextSpecificPrimitive contextSpecific,
        SecretKey gostKey, OID cipherOID, byte[] iv, String provider)
        throws CMSCryptographyException, ASNDecodeException {
        super(contextSpecific.getEncoded(), 0);
        cipherProcessor = new CipherProcessor(contextSpecific.getVirtualInternalLength(),
            gostKey, cipherOID, iv, provider);
    }

    /**
     * Метод, осуществляющий получение высокоуровневого шифратора.
     * @return Объект высокоуровневого шифратора.
     */
    public CipherProcessor getCipherProcessor() {
        return cipherProcessor;
    }
}
