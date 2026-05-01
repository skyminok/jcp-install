package cmsutil.asn1.base;

import cmsutil.asn1.ASNDecodeException;
import java.util.ArrayList;

/**
 * Класс, используемый для ASN1-кодирования сложных ContextSpecific структур
 */
public class ASNContextSpecificConstructed extends ASNConstructed {
    /** Тип структуры*/
    protected int type;

    /**
     * Конструктор по значению
     * @param newSubs Список подструктур
     */
    public ASNContextSpecificConstructed(ArrayList<ASNCommon> newSubs) {
        subs = newSubs;
        encode();
    }

    /**
     * Конструктор по закодированному значению
     * @param encoded Байтовый массив, содержащий ASN1-кодированное значение.
     * @param offset Сдвиг относительно начала массива, с которого начинается ASN1-структура.
     * @throws ASNDecodeException Ошибка декодирования
     */
    public ASNContextSpecificConstructed(byte[] encoded, int offset) throws ASNDecodeException {
        type = encoded == null ? 0 : (encoded[offset] & 0xff);
        decode(encoded, offset);
    }

    /**
     * Конструктор по уиолчанию.
     */
    public ASNContextSpecificConstructed() {

    }

    /**
     * Метод, осуществляющий инициализацию объекта класса по закодированному ASN1-значению
     * @param value Байтовый массив, содержащий ASN1-кодированно значение.
     * @throws ASNDecodeException
     */
    protected void setUpByValue(byte[] value) throws ASNDecodeException {
        virtualInternalLength = realInternalLength = value.length;
        byte[] encodedLength = encodeLength();
        encodedValue = new byte[1 + encodedLength.length + value.length];
        encodedValue[0] = (byte)getTag();
        System.arraycopy(encodedLength, 0, encodedValue, 1, encodedLength.length );
        System.arraycopy(value, 0, encodedValue, 1 + encodedLength.length, value.length);
        decode(encodedValue, 0);
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true
     */
    @Override
    protected boolean checkConsist() {
        return true;
    }

    /**
     * Метод, возвращающий ASN1-тэг.
     * @return ASN1-тэг
     */
    @Override
    public int getTag() {
        return ASN1Complicated | ASN1ContextSpecific | type;
    }

}
