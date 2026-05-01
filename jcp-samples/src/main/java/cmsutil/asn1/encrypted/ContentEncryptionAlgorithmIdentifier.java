package cmsutil.asn1.encrypted;


import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;
import cmsutil.asn1.base.*;
import cmsutil.tools.OID;

import java.util.ArrayList;

/**
 * Класс, обеспечивающий кодирование набора параметров шифрования
 * данных по алгоритму ГОСТ 28147-89
 */
public class ContentEncryptionAlgorithmIdentifier extends ASNSequence {

    /**
     * Набор параметров шифрования ГОСТ 28147-89
     */
    private final Gost28147Parameteres gostParams;

    /**
     * Конструктор, используемый при закодировании
     * @param paramSet Набор параметров шифрования
     */
    public ContentEncryptionAlgorithmIdentifier(Gost28147Parameteres paramSet)
        throws ASNEncodeException {
        gostParams = paramSet;
        subs = new ArrayList<ASNCommon>(2);
        ASNObjectIdentifier gostAlgOID = new ASNObjectIdentifier(OID.gost28147OID);
        subs.add(gostAlgOID);
        subs.add(gostParams);
        encode();
    }

    /**
     * Набор, используемый при раскодировании
     * @param sequence ASN1-структура, соответствующая классу.
     * @throws ASNDecodeException Ошибка декодирования.
     */
    public ContentEncryptionAlgorithmIdentifier(ASNSequence sequence) throws ASNDecodeException {
        this.encodedValue = sequence.getEncoded();
        this.realInternalLength = sequence.getRealInternalLength();
        this.realEncodedLength = sequence.getRealEncodedLength();
        this.virtualEncodedLength = sequence.getVirtualEncodedLength();
        this.virtualInternalLength = sequence.getVirtualInternalLength();
        subs = sequence.getSubStructures();
        if (!checkConsist())
            throw new ASNDecodeException("ContentEncryptionAlgorithmIdentifier structure is corrupted!");
        gostParams = new Gost28147Parameteres((ASNSequence)subs.get(1));
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        boolean res = subs.size() == 2 && (subs.get(1) instanceof ASNSequence) && (subs.get(0) instanceof ASNObjectIdentifier);
        OID tmpOID = (OID)(((ASNObjectIdentifier)subs.get(0)).getValue());
        return (res && tmpOID.equals(OID.gost28147OID));
    }

    /**
     * Метод, осуществляющий получение набора параметров
     * @return Объект класса Gost28147Parameteres, содержащий параметры шифрования.
     */
    public Gost28147Parameteres getGostParams() {
        return gostParams;
    }
}
