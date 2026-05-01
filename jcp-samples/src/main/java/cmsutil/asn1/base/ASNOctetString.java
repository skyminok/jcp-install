package cmsutil.asn1.base;


import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;

/**
 * Класс, используемый для ASN1-кодирования байтовых последовательностей.
 */
public class ASNOctetString extends ASNPrimitive{

    /** Значение байтовой последовательности*/
    protected byte[] value;

    /**
     * Конструктор по значению.
     * @param newValue Значение байтовой последовательности.
     */
    public ASNOctetString(byte[] newValue) throws ASNEncodeException {
        value = new byte[newValue.length];
        System.arraycopy(newValue, 0, value, 0, value.length);
        encode();
    }

    /**
     * Конструктор по закодированному значению
     * @param encoded Байтовый массив, содержащий ASN1-кодированное значение.
     * @param offset Сдвиг относительно начала массива, с которого начинается ASN1-структура.
     * @throws ASNDecodeException Ошибка декодирования
     */
    public ASNOctetString(byte[] encoded, int offset) throws ASNDecodeException {
        decode(encoded, offset);
    }

    /**
     * Конструктор по умолчанию
     */
    protected ASNOctetString() {
        value = encodedValue = null;
        realInternalLength = virtualInternalLength = 0;
    }

    /**
     * Метод, возвращающий ASN1-тэг.
     * @return ASN1-тэг
     */
    @Override
    public int getTag() {
        return ASN1OctetString;
    }

    /**
     * Метод, получающий значение строки
     * @return Байтовый массив, содержащий последовательность.
     */
    @Override
    public Object getValue() {
        byte[] res = null;
        if (value != null) {
            res = new byte[value.length];
            System.arraycopy(value, 0, res, 0, value.length);
        }
        return res;
    }

    /**
     * Метод, осуществляющий получение байтового представления значения.
     * @return Байтовый массив, содержащий байтовое представление значения.
     */
    @Override
    protected byte[] getByteValue() {
        byte res[] = new byte[value.length];
        System.arraycopy(value, 0, res, 0, value.length);
        return res;
    }

    /**
     * Метод, осуществляющий установку значения по его байтовому представлению.
     * @param val Байтовый массив, содержащие байтовое представление значения.
     */
    @Override
    protected void setValue(byte[] val) {
        value = new byte[val.length];
        System.arraycopy(val, 0, value, 0, val.length);
    }
}
