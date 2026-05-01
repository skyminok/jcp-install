package cmsutil.asn1.base;

import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;

/**
 * Класс, используемый для ASN1-кодирования IA5 строк.
 */
public class ASNIA5String extends ASNPrimitive {
    /** Значение строки*/
    protected String value;

    /**
     * Конструктор по значению.
     * @param newValue Значение строки.
     */
    public ASNIA5String(String newValue) throws ASNEncodeException {
        value = newValue;
        encode();
    }

    /**
     * Конструктор по закодированному значению
     * @param encoded Байтовый массив, содержащий ASN1-кодированное значение.
     * @param offset Сдвиг относительно начала массива, с которого начинается ASN1-структура.
     * @throws ASNDecodeException Ошибка декодирования
     */
    public ASNIA5String(byte[] encoded, int offset) throws ASNDecodeException {
        decode(encoded, offset);
    }

    /**
     * Метод, получающий значение строки
     * @return Объект класса String, содержащий строку
     */
    @Override
    public Object getValue() {
        return value;
    }

    /**
     * Метод, осуществляющий получение байтового представления значения.
     * @return Байтовый массив, содержащий байтовое представление значения.
     */
    @Override
    protected byte[] getByteValue() throws ASNEncodeException {
        return value == null ? null : value.getBytes();
    }

    /**
     * Метод, осуществляющий установку значения по его байтовому представлению.
     * @param val Байтовый массив, содержащие байтовое представление значения.
     */
    @Override
    protected void setValue(byte[] val) throws ASNEncodeException {
        value = new String(val);
    }

    /**
     * Метод, возвращающий ASN1-тэг.
     * @return ASN1-тэг
     */
    @Override
    public int getTag() {
        return ASN1IA5String;
    }
}
