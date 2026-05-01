package cmsutil.asn1.base;

import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;

import java.io.UnsupportedEncodingException;

/**
 * Класс, используемый для ASN1-кодирования UTF-8 строк.
 */
public class ASNUTF8String extends ASNPrintableString {

    /**
     * Конструктор по значению
     * @param newValue Объект класса String, содержащий строку.
     */
    public ASNUTF8String(String newValue) throws ASNEncodeException {
        super(newValue);
    }

    /**
     * Конструктор по закодированному значению
     * @param encoded Байтовый массив, содержащий ASN1-кодированное значение.
     * @param offset Сдвиг относительно начала массива, с которого начинается ASN1-структура.
     * @throws ASNDecodeException Ошибка декодирования
     */
    public ASNUTF8String(byte[] encoded, int offset) throws ASNDecodeException {
        super(encoded, offset);
    }

    @Override
    public int getTag() {
        return ASN1UTF8String;
    }

    @Override
    protected byte[] getByteValue() throws ASNEncodeException {
        try {
            return value == null ? null : value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ASNEncodeException(e.getMessage());
        }
    }

    @Override
    protected void setValue(byte[] val) throws ASNEncodeException {
        try {
            value = new String(val, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ASNEncodeException(e.getMessage());
        }
    }

}
