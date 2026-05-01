package cmsutil.asn1;

import cmsutil.asn1.base.ASNCommon;
import cmsutil.asn1.base.ASNContextSpecificConstructed;
import cmsutil.asn1.base.ASNObjectIdentifier;
import cmsutil.asn1.base.ASNSequence;
import cmsutil.asn1.encrypted.CipherProcessor;
import cmsutil.asn1.kari.RecipientCertInfo;
import cmsutil.tools.OID;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование структуры Enveloped CMS
 */
public class EnvelopedCMS extends ASNSequence {
    /** Объектный идентификатор типа данных. */
    private ASNObjectIdentifier contentType;
    /** Структура Content*/
    private Content content;

    /**
     * Конструктор, используемый при создании сообщения CMS Enveloped.
     * @param textLen Длина открытого текста.
     * @param recipientCertificate Сертификат получателя.
     * @throws ASNDecodeException Ошибка раскодирования.
     * @throws CMSCryptographyException Ошибка криптографической операции.
     */
    public EnvelopedCMS(long textLen, X509Certificate recipientCertificate, String provider)
        throws ASNDecodeException, CMSCryptographyException {
        contentType = new ASNObjectIdentifier(OID.envelopedDataOID);
        content = new Content(textLen, recipientCertificate, provider);
        subs = new ArrayList<ASNCommon>(2);
        subs.add(contentType);
        subs.add(content);
        encode();
    }

    /**
     * Конструктор, используемый при разборе сообщения CMS Enveloped.
     * @param value Байтовый массив, хранящий закодированную структуру.
     * @param recipientPrivateKey Секретный ключ получателя ГОСТ Р 34.10-2001.
     * @throws ASNDecodeException Ошибка раскодирования.
     * @throws CMSCryptographyException Ошибка криптографической операции.
     */
    public EnvelopedCMS(byte[] value, PrivateKey recipientPrivateKey, String provider)
        throws ASNDecodeException, CMSCryptographyException {
        ASNSequence sequence = new ASNSequence(value, 0);
        if (sequence.getRealEncodedLength() != value.length)
            throw new ASNDecodeException("Structure EnvelopedCMS is corrupted!");
        process(sequence, recipientPrivateKey, provider);

    }

    /**
     * Метод, осуществляющий корректную инициализацию оюъекта.
     * @param sequence ASN1-структура, связанная с классом.
     * @param recipientPrivateKey Секретный ключ получателя ГОСТ Р 34.10-2001.
     * @throws ASNDecodeException Ошибка раскодирования.
     * @throws CMSCryptographyException Ошибка криптографической операции.
     */
    private void process(ASNSequence sequence, PrivateKey recipientPrivateKey, String provider)
        throws ASNDecodeException, CMSCryptographyException {
        this.subs = sequence.getSubStructures();
        this.encodedValue = sequence.getEncoded();
        this.realInternalLength = sequence.getRealInternalLength();
        this.realEncodedLength = sequence.getRealEncodedLength();
        this.virtualEncodedLength = sequence.getVirtualEncodedLength();
        this.virtualInternalLength = sequence.getVirtualInternalLength();
        if (!checkConsist())
            throw new ASNDecodeException("Structure EnvelopedCMS is corrupted!");
        contentType = (ASNObjectIdentifier)subs.get(0);
        if (!contentType.getValue().equals(OID.envelopedDataOID))
            throw new ASNDecodeException("Unknown Content Type OID: " + contentType.getValue().toString());
        content = new Content((ASNContextSpecificConstructed)subs.get(1), recipientPrivateKey, provider);
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        return (subs != null) && (subs.size() == 2) && (subs.get(0) instanceof ASNObjectIdentifier) &&
                (subs.get(1) instanceof ASNContextSpecificConstructed);
    }

    /**
     * Метод, получающий высокоуровневый объект шифратора.
     * @return Высокоуровневый объект шифратора.
     */
    public CipherProcessor getCipherProcessor() {
        return content.getCipherProcessor();
    }

    /**
     * Метод, получающий информацию о сертификате получателя.
     * @return Информацию о сертификате получателя.
     */
    public RecipientCertInfo getRecipientCertInfo() {
        return content.getRecipientCertInfo();
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Вспомогательные методы.
// Используются при разборе CMSEnveloped, для получения длины непосредственно вспомогательных данных (без шифртекста).
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Метод, пропускающий тэг.
    private static int skipTag(FileInputStream is, long[] offset) throws IOException {
        int read;
        read = is.read();
        offset[0]++;
        if (read < 0)
            return -1;
        return 0;
    }

    //Метод, получающий длину закодированной длины.
    private static long getLength(FileInputStream is, int len) throws IOException {
        long res = 0;
        int read;
        for (int i = 0; i < len; i++) {
            res *= 256;
            read = is.read();
            if (read < 0)
                return -1;
            res += (read & 0xff);
        }
        return res;
    }

    //Метод, пропускающий длину.
    private static long skipLength(FileInputStream is, long[] offset) throws IOException {
        int read;
        long skip;
        read = is.read();
        if (read < 0)
            return -1;
        offset[0]++;
        if ((read & 0x80) != 0) {
            skip = getLength(is, read & 0x7f);
            if (skip < 0)
                return -1;
            offset[0] += read & 0x7f;
        } else {
            skip = read & 0x7f;
        }
        return skip;
    }

    /*
     * Метод, получающий смещение шифртекста относительно начала CMS-файла.
     */
    public static long getCipherTextOffset(FileInputStream is) throws IOException {
        long[] offset = new long[1];
        long skip;

        offset[0] = 0;
        //SEQUENCE
        if (skipTag(is, offset) < 0)
            return -1;
        skip = skipLength(is, offset);
        if (skip < 0)
            return -1;

        //OBJECT IDENTIFIER enveloped data
        if (skipTag(is, offset) < 0)
            return -1;
        skip = skipLength(is, offset);
        if (skip < 0)
            return -1;
        is.skip(skip);
        offset[0] += skip;

        //CONTEXT_SPECIFIC
        if (skipTag(is, offset) < 0)
            return -1;
        skip = skipLength(is, offset);
        if (skip < 0)
            return -1;

        //SEQUENCE
        if (skipTag(is, offset) < 0)
            return -1;
        skip = skipLength(is, offset);
        if (skip < 0)
            return -1;

        //INTEGER enveloped data version
        if (skipTag(is, offset) < 0)
            return -1;
        skip = skipLength(is, offset);
        if (skip < 0)
            return -1;
        is.skip(skip);
        offset[0] += skip;

        // SET recipientInfo
        if (skipTag(is, offset) < 0)
            return -1;
        skip = skipLength(is, offset);
        if (skip < 0)
            return -1;
        is.skip(skip);
        offset[0] += skip;

        //SEQUENCE
        if (skipTag(is, offset) < 0)
            return -1;
        skip = skipLength(is, offset);
        if (skip < 0)
            return -1;

        //OBJECT IDENTIFIER data
        if (skipTag(is, offset) < 0)
            return -1;
        skip = skipLength(is, offset);
        if (skip < 0)
            return -1;
        is.skip(skip);
        offset[0] += skip;

        //SEQUENCE
        if (skipTag(is, offset) < 0)
            return -1;
        skip = skipLength(is, offset);
        if (skip < 0)
            return -1;
        is.skip(skip);
        offset[0] += skip;

        //CONTEXT SPECIFIC data
        if (skipTag(is, offset) < 0)
            return -1;
        skip = skipLength(is, offset);
        if (skip < 0)
            return -1;

        return offset[0];
    }
}
