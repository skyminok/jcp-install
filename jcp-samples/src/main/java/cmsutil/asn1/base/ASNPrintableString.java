package cmsutil.asn1.base;

import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;

/**
 * Класс, используемый для ASN1-кодирования печатаемых строк.
 */
public class ASNPrintableString extends ASNIA5String {

    /**
     * Конструктор по значению
     * @param newValue Объект класса String, содержащий строку.
     */
    public ASNPrintableString(String newValue) throws ASNEncodeException {
        super(newValue);
    }

    /**
     * Конструктор по закодированному значению
     * @param encoded Байтовый массив, содержащий ASN1-кодированное значение.
     * @param offset Сдвиг относительно начала массива, с которого начинается ASN1-структура.
     * @throws ASNDecodeException Ошибка декодирования
     */
    public ASNPrintableString(byte[] encoded, int offset) throws ASNDecodeException {
        super(encoded, offset);
    }

    /**
     * Метод, возвращающий ASN1-тэг.
     * @return ASN1-тэг
     */
    @Override
    public int getTag() {
        return ASN1PrintableString;
    }
}
