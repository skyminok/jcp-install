package cmsutil.asn1.base;


import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;

/**
 * Абстрактный класс, содержащий основные методы ASN1-кодирования
 */
public abstract class ASNCommon {
    // Константы
    public final static int ASN1Integer = 0x02;
    public final static int ASN1OctetString = 0x04;
    public final static int ASN1ObjectIdentifier = 0x06;
    public final static int ASN1Sequence = 0x30;
    public final static int ASN1ContextSpecific = 0x80;
    public final static int ASN1BitString = 0x03;
    public final static int ASN1Set = 0x31;
    public final static int ASN1IA5String = 0x16;
    public final static int ASN1UTF8String = 0xC;
    public final static int ASN1PrintableString = 0x13;
    public final static int ASN1Complicated = 0x20;
    public final static int ASN1NumericString = 0x12;
    public final static int ASN1BMPString = 0x1e;

    /** Байтовый массив, хранящий закодированное значение */
    protected byte[] encodedValue;

    /** Действительная длина байтового значения (value). Именно столько байт
     *  оперативной памяти выделено для хранения
     */
    protected long realInternalLength;
    /** Общая длина байтового значения, часть которого может быть не загружена из файла в оперативную память */
    protected long virtualInternalLength;
    /** Действительная длина закодированного значения (с тэгом и длиной)*/
    protected long realEncodedLength;
    /** Общая длина закодированного сообщения (с тэгом и длиной)*/
    protected long virtualEncodedLength;

    /**
     * Абстрактный метод, осуществляющий раскодирование ASN1-структуры.
     * @param encoded Байтовый массив, содержащий ASN1-структуру.
     * @param offset Смещение относительно начала массива, с которого начинается структура.
     * @throws ASNDecodeException Ошибка декодирования
     */
    protected abstract void decode(byte[] encoded, int offset) throws ASNDecodeException;

    /**
     * Абстрактный метод, осуществляющий закодирование данных
     */
    protected abstract void encode() throws ASNEncodeException;

    /**
     * Абстрактный метод, возвращаюший ASN1-тэг структуры.
     * @return ASN1-тэг структуры.
     */
    public abstract int getTag();

    /**
     * Метод, осуществляющий расчёт длины закодированной длины значения, содержащегося в ASN1-структуре.
     * @param encoded Байтовый массив, содержащий ASN1-структуру.
     * @param offset Смещение относительно начала массива, с которого начинается структура.
     * @return
     */
    private int getLengthLengthDec(byte[] encoded, int offset) {
        if ((encoded[offset + 1] & 0x80) == 0)
            return 1;
        else
            return (encoded[offset + 1] & 0x7f) + 1;
    }

    /**
     * Метод, осуществляющий получение длины закодированной длины значения.
     * @return Длина закодированной длины.
     */
    private int getLengthLengthEnc() {
        int res = 0;
        long tmp = virtualInternalLength;
        while (tmp != 0) {
            res++;
            tmp = tmp >>> 8;
        }
        return res;
    }

    /**
     * Метод, осуществляющий получение длины кодированного значения.
     * @param encoded Байтовый массив, содержащий ASN1-структуру.
     * @param offset Смещение относительно начала массива, с которого начинается структура.
     * @param length Массив int из одного элемента, через который возвращается длина закодированной длины
     * @return Длина кодированного значения
     */
    protected long getLength(byte[] encoded, int offset, int[] length) {
        length[0] = getLengthLengthDec(encoded, offset);
        if (length[0] == 1)
            return encoded[offset + 1];
        long res = 0;
        for (int i = 1; i < length[0]; i++) {
            res *= 256;
            res += (int)(encoded[offset + i + 1] & 0xff);
        }
        return res;
    }

    /**
     * Метод, осуществляющий кодирование длины значения.
     * @return Байтовый массив, содержащий закодированную длину значения.
     */
    protected byte[] encodeLength() {
        byte res[];
        if (virtualInternalLength < 128) {
            res = new byte[1];
            res[0] = (byte)virtualInternalLength;
        } else {
            int lenLength = getLengthLengthEnc();
            long temp = virtualInternalLength;
            res = new byte[lenLength + 1];
            for (int i = res.length - 1; i>=1; i--) {
                res[i] = (byte)temp;
                temp = temp >>> 8;
            }
            res[0] = (byte)(0x80 ^ (lenLength));
        }
        return res;
    }

    /**
     * Метод, осуществляющий получение закодированной ASN1-структуры.
     * @return Байтовый массив, содержащий закодированную ASN1-структуру.
     */
    public byte[] getEncoded() {
        byte[] res = null;
        if (encodedValue != null) {
            res = new byte[encodedValue.length];
            System.arraycopy(encodedValue, 0, res, 0, encodedValue.length);
        }
        return res;
    }

    /**
     * Метод, осуществляющий получение действительной длины байтового значения.
     * @return Действительная длина байтового значения
     */
    public long getRealInternalLength() {
        return realInternalLength;
    }

    /**
     * Метод, осуществляющий получение полной длины байтового значения.
     * @return Полная длина байтового значения.
     */
    public long getVirtualInternalLength() {return virtualInternalLength;}

    /**
     * Метод, осуществляющий получение действительной длины ASN1-структуры.
     * @return Действительная длина ASN1-структуры.
     */
    public long getRealEncodedLength() {return realEncodedLength;}

    /**
     * Метод, осуществляющий получение полной длины ASN1-структуры.
     * @return Полная длина ASN1-структуры.
     */
    public long getVirtualEncodedLength() {return virtualEncodedLength;}

}
