package cmsutil.asn1.base;

import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;
import cmsutil.tools.BitString;

/**
 * Класс, используемый для ASN1-кодирования битовых строк
 */
public class ASNBitString extends ASNPrimitive {

    /** Значение битовой строки*/
    private BitString value;

    /**
     * Конструктор по значению
     * @param val Значение битовой строки
     */
    public ASNBitString(BitString val) throws ASNEncodeException {
        value = val;
        encode();
    }

    /**
     * Конструктор по ASN1-представлению
     * @param encoded Байтовый массив, содержащий ASN1-представление
     * @param offset Смещение относительно начала, с которого начинается закодированная структура
     * @throws ASNDecodeException
     */
    public ASNBitString(byte[] encoded, int offset) throws ASNDecodeException {
        decode(encoded, offset);
    }

    /**
     * Метод, осуществляющий получение значения битовой строки
     * @return Объект класса BitString, содержащий значение.
     */
    @Override
    public Object getValue() {
        return value;
    }

    /**
     * Метод, осуществляющий получение значения битовой строки в виде массива байтов
     * @return Массив байтов, содержащих значение битовой строки
     */
    @Override
    protected byte[] getByteValue() {
        return value.getBytes();
    }

    /**
     * Метод, осуществляющий установку значения битовой строки
     * @param val Битовая строка в виде массива байт.
     */
    @Override
    protected void setValue(byte[] val) {
        value = new BitString(val);
    }

    /**
     * Метод, возвращающий ASN1 тэг структуры BitString
     * @return ASN1 тэг структуры BitString
     */
    @Override
    public int getTag() {
        return ASN1BitString;
    }
}
