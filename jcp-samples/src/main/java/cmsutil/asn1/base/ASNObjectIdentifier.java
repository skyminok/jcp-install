package cmsutil.asn1.base;


import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;
import cmsutil.tools.OID;

/**
 * Класс, используемый для ASN1-кодирования объектных идентификаторов.
 */
public class ASNObjectIdentifier extends ASNPrimitive{

    /** Значение объектного идентификатора */
    private OID value;

    /**
     * Конструктор по значению
     * @param newValue Значение объектного идентификатора
     */
    public ASNObjectIdentifier(OID newValue) throws ASNEncodeException {
        this.value = newValue;
        encode();
    }

    /**
     * Конструктор по закодированному значению
     * @param encoded Байтовый массив, содержащий ASN1-кодированное значение.
     * @param offset Сдвиг относительно начала массива, с которого начинается ASN1-структура.
     * @throws ASNDecodeException Ошибка декодирования
     */
    public ASNObjectIdentifier(byte[] encoded, int offset) throws ASNDecodeException {
        decode(encoded, offset);
    }

    /**
     * Метод, возвращающий ASN1-тэг.
     * @return ASN1-тэг
     */
    @Override
    public int getTag() {
        return ASN1ObjectIdentifier;
    }

    /**
     * Метод, получающий значение строки
     * @return Объект класса OID, содержащий объектны идентификатор
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
        return value.getByteValue();
    }

    /**
     * Метод, осуществляющий установку значения по его байтовому представлению.
     * @param val Байтовый массив, содержащие байтовое представление значения.
     */
    @Override
    protected void setValue(byte[] val) {
        value = new OID(val);
    }
}
