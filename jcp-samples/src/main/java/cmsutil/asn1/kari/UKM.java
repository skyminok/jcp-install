package cmsutil.asn1.kari;


import cmsutil.asn1.ASNEncodeException;
import cmsutil.asn1.base.ASNCommon;
import cmsutil.asn1.base.ASNContextSpecificConstructed;
import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.base.ASNOctetString;

import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование пользовательского ключевого материала (UKM).
 */
public class UKM extends ASNContextSpecificConstructed {
    /** UKM */
    private ASNOctetString value;

    /**
     * Конструктор, используемый при создании сообщения CMS Enveloped.
     * @param ukm Значение UKM.
     */
    public UKM(byte[] ukm) throws ASNEncodeException {
        value = new ASNOctetString(ukm);
        subs = new ArrayList<ASNCommon>(1);
        subs.add(value);
        type = 1;
        encode();
    }

    /**
     * Конструктор, используемый при разборе сообщения CMS Enveloped.
     * @param contextSpecificConstructed ASN1-структура, связанная с классом.
     * @throws ASNDecodeException Ошибка разбора.
     */
    public UKM(ASNContextSpecificConstructed contextSpecificConstructed) throws ASNDecodeException {
        this.subs = contextSpecificConstructed.getSubStructures();
        this.encodedValue = contextSpecificConstructed.getEncoded();
        this.type = (byte)(encodedValue[0] & 0x0f);
        this.realInternalLength = contextSpecificConstructed.getRealInternalLength();
        this.realEncodedLength = contextSpecificConstructed.getRealEncodedLength();
        this.virtualEncodedLength = contextSpecificConstructed.getVirtualEncodedLength();
        this.virtualInternalLength = contextSpecificConstructed.getVirtualInternalLength();
        if (!checkConsist())
            throw new ASNDecodeException("Structure UKM is corrupted!");
        value = (ASNOctetString)subs.get(0);
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        return (subs.size() == 1) && (subs.get(0) instanceof ASNOctetString);
    }

    /**
     * Метод, осуществляющий получение значения UKM
     * @return Значение UKM  в виде байтового массива.
     */
    public byte[] getUKM() {
        return (byte[])value.getValue();
    }
}
