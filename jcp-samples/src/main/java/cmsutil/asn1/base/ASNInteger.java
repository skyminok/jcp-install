package cmsutil.asn1.base;

import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;

import java.math.BigInteger;

/**
 * Класс, используемый для ASN1-кодирования целых чисел.
 */
public class ASNInteger extends ASNPrimitive {

    /**
     * Значение целого числа
     */
    private BigInteger value = null;

    /**
     * Конструктор по значению
     * @param newValue Значение кодируемого целого
     */
    public ASNInteger(BigInteger newValue) throws ASNEncodeException {
        value = newValue;
        encode();
    }

    /**
     * Конструктор по закодированному значению
     * @param encoded Байтовый массив, содержащий ASN1-кодированное значение.
     * @param offset Сдвиг относительно начала массива, с которого начинается ASN1-структура.
     * @throws ASNDecodeException Ошибка декодирования
     */
    public ASNInteger(byte[] encoded, int offset) throws ASNDecodeException {
        decode(encoded, offset);
    }

    /**
     * Метод, возвращающий ASN1-тэг.
     * @return ASN1-тэг
     */
    @Override
    public int getTag() {
        return ASN1Integer;
    }

    /**
     * Метод, получающий значение целого
     * @return Объект класса BigInteger, содержащий целое
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
    protected byte[] getByteValue() {
        return value.toByteArray();
    }

    /**
     * Метод, осуществляющий установку значения по его байтовому представлению.
     * @param val Байтовый массив, содержащие байтовое представление значения.
     */
    @Override
    protected void setValue(byte[] val) {
        value = new BigInteger(val);
    }
}
