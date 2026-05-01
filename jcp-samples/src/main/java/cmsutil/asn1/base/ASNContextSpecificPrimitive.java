package cmsutil.asn1.base;


import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;

/**
 * Класс, используемый для ASN1-кодирования примитивных ContextSpecific структур
 */
public class ASNContextSpecificPrimitive extends ASNPrimitive {
    /** Тип структуры*/
    protected byte type;
    /** Байтовый массив, содержащий значение */
    protected byte[] value;

    /**
     * Конструктор по умолчанию
     */
    public ASNContextSpecificPrimitive() {

    }

    /**
     * Конструктор по длине значения, создающий "виртуальный объект".
     * "Виртуальный объект" отличается от обычного тем, что в нём отсутствует непосредственно значение.
     * Тем не менее длина и тэг указываются корректно.
     * @param valLen Длина предполагаемого значения
     * @param tagType Тип структуры
     */
    public ASNContextSpecificPrimitive(long valLen, int tagType) throws ASNEncodeException {
        type = (byte)tagType;
        virtualInternalLength = valLen;
        realInternalLength = 0;
        value = null;
        encode();
    }

    /**
     * Конструктор по закодированному значению
     * @param encoded Байтовый массив, содержащий ASN1-кодированное значение.
     * @param offset Сдвиг относительно начала массива, с которого начинается ASN1-структура.
     * @throws ASNDecodeException ASNDecodeException Ошибка декодирования
     */
    public ASNContextSpecificPrimitive(byte[] encoded, int offset) throws ASNDecodeException {
        type = encoded == null ? 0 : (byte)(encoded[offset] & 0x0f);
        decode(encoded, offset);
    }

    /**
     * Метод, получающий значение.
     * @return Байтовый массив, содержащий значение.
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
     * Метод, осуществляющий получение байтового представления значения
     * @return Байтовый массив, содержащий байтовое представление значения.
     */
    @Override
    protected byte[] getByteValue() {
        byte[] res = null;
        if (value != null) {
            res = new byte[value.length];
            System.arraycopy(value, 0, res, 0, value.length);
        }
        return res;
    }

    /**
     * Метод, осуществляющий установку значения по байтовому представлению.
     * @param val Байтовый массив, содержащий байтовое представление значения.
     */
    @Override
    protected void setValue(byte[] val) {
        if (val != null) {
            value = new byte[val.length];
            System.arraycopy(val, 0, value, 0, val.length);
        } else {
            value = null;
        }
    }

    /**
     * Метод, возвращающий ASN1-тэг.
     * @return ASN1-тэг
     */
    @Override
    public int getTag() {
        return ASN1ContextSpecific | type;
    }
}
