package cmsutil.asn1.base;

import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;

/**
 * Класс, осуществляющий закодирование и раскодирование примитивных ASN1-типов.
 */
public abstract class ASNPrimitive extends ASNCommon {
    /**
     * Абстрактный метод, получающий объект, связанный со значением примитвного типа.
     * @return Объект, связанный со значением примитивного типа.
     */
    public abstract Object getValue();

    /**
     * Абстрактный метод, получающий байтовое представление значения.
     * @return Байтовый массив, содержащий байтовое представление значения.
     */
    protected abstract byte[] getByteValue() throws ASNEncodeException;

    /**
     * Абстрактный метод, устанавливающий байтовое представление значения.
     * @param val Байтовый массив, содержащий байтовое представление значения.
     */
    protected abstract void setValue(byte[] val) throws ASNEncodeException;

    /**
     * Метод, осуществляющий раскодирование примитивного ASN1-типа.
     * @param encoded Байтовый массив, содержащий ASN1-структуру.
     * @param offset Смещение относительно начала массива, с которого начинается структура.
     * @throws ASNDecodeException
     */
    protected void decode(byte[] encoded, int offset) throws ASNDecodeException {
        int buf[] = new int[1];
        byte[] val;
        if (encoded == null)
            throw new ASNDecodeException("NPE while decoding ASN value!");
        if (encoded.length - offset < 2)
            throw new ASNDecodeException("ASN1 structure too short");
        if (((int)(encoded[offset]) & 0xff) != getTag())
            throw new ASNDecodeException("ASN1 parsing error: 0x" + Integer.toHexString(encoded[offset]) + " tag received " +
            "while tag 0x" + Integer.toHexString(getTag()) + " expected.");
        virtualInternalLength = getLength(encoded, offset, buf);
        virtualEncodedLength = 1 + (int)virtualInternalLength + buf[0];;
        if (encoded.length - offset - 1 - buf[0] == 0) { // "Несуществующий (виртуальный) объект". Де-факто: шифртекст, который в оперативную память не загружен, но будет закодирован.
            setValue(null);
            realInternalLength = 0;
            realEncodedLength = 1 + buf[0];
            encodedValue = new byte[(int)realEncodedLength];
            System.arraycopy(encoded, offset, encodedValue, 0, encodedValue.length);
            return;
        }
        realInternalLength = virtualInternalLength;
        realEncodedLength = virtualEncodedLength;
        val = new byte[(int)realInternalLength];
        System.arraycopy(encoded, offset + 1 + buf[0], val, 0, (int)realInternalLength);
        setValue(val);
        encodedValue = new byte[(int)realEncodedLength];
        System.arraycopy(encoded, offset, encodedValue, 0, encodedValue.length);
    }


    /**
     * Метод, осуществляющий закодирование примитивного ASN1-типа.
     */
    @Override
    protected void encode() throws ASNEncodeException {
        byte[] value = getByteValue();
        if (value != null) { // Если true, то объект не "виртуальный". Для "виртуального" все установлено в конструкторе!
            virtualInternalLength = realInternalLength = value.length;
        }
        byte[] encodedLength = encodeLength();
        realEncodedLength = 1 + encodedLength.length + realInternalLength;
        virtualEncodedLength = 1 + encodedLength.length + virtualInternalLength;
        encodedValue = new byte[(int)realEncodedLength];
        encodedValue[0] = (byte)getTag();
        System.arraycopy(encodedLength, 0, encodedValue, 1, encodedLength.length );
        if (value != null)
            System.arraycopy(value, 0, encodedValue, 1 + encodedLength.length, value.length);
    }

}
