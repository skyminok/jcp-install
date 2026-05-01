package cmsutil.asn1;

import cmsutil.asn1.base.ASNCommon;
import cmsutil.asn1.base.ASNContextSpecificConstructed;
import cmsutil.asn1.base.ASNSequence;
import cmsutil.asn1.encrypted.CipherProcessor;
import cmsutil.asn1.kari.RecipientCertInfo;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование содержимого CMS-контейнера.
 */
public class Content extends ASNContextSpecificConstructed {
    /**
     * Структура EnvelopedData
     */
    private EnvelopedData envelopedData;

    /**
     * Конструктор, используемый при создании сообщения CMS Enveloped.
     * @param textLen Длина открытого текста.
     * @param recipientCertificate Сертификат открытого ключа получателя.
     * @throws CMSCryptographyException Ошибка криптографической операции.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    public Content(long textLen, X509Certificate recipientCertificate,
        String provider) throws CMSCryptographyException, ASNDecodeException
    {
        type = 0;
        envelopedData = new EnvelopedData(textLen, recipientCertificate, provider);
        subs = new ArrayList<ASNCommon>(1);
        subs.add(envelopedData);
        encode();
    }

    /**
     * Конструктор, используемый при разборе сообщения CMS Enveloped.
     * @param contextSpecificConstructed ASN1-структура, связанная с классом.
     * @param recipientPrivateKey Секретный ключ получателя ГОСТ Р 34.10-2001.
     * @throws ASNDecodeException Ошибка раскодирования.
     * @throws CMSCryptographyException Ошибка криптографической операции.
     */
    public Content(ASNContextSpecificConstructed contextSpecificConstructed,
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
            throw new ASNDecodeException("Structure Content is corrupted!");
        envelopedData = new EnvelopedData((ASNSequence)subs.get(0), recipientPrivateKey, provider);
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        if (type != 0)
            return false;
        if ((subs == null) || (subs.size() != 1))
            return false;
        return subs.get(0) instanceof ASNSequence;
    }

    /**
     * Метод, получающий высокоуровневый объект шифратора.
     * @return Высокоуровневый объект шифратора.
     */
    public CipherProcessor getCipherProcessor() {
        return envelopedData.getCipherProcessor();
    }

    /**
     * Метод, получающий информацию о сертификате получателя.
     * @return Информацию о сертификате получателя.
     */
    public RecipientCertInfo getRecipientCertInfo() {
        return envelopedData.getRecipientCertInfo();
    }
}
