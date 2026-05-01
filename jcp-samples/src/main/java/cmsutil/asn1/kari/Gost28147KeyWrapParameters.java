package cmsutil.asn1.kari;


import cmsutil.asn1.ASNEncodeException;
import cmsutil.asn1.base.ASNCommon;
import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.base.ASNObjectIdentifier;
import cmsutil.asn1.base.ASNSequence;
import cmsutil.tools.OID;

import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование параметров алгоритма шифрования ключа.
 */
public class Gost28147KeyWrapParameters extends ASNSequence {
    /**
     * Объектный идентификатор набора параметров алгоритма шифрования ключа.
     */
    private ASNObjectIdentifier asnWrapOID;

    /**
     * Конструктор, используемый при создании CMS Enveloped сообщения.
     * @param wrapOID Объектный идентификатор набора узлов замены, используемого при шифровании ключа.
     */
    public Gost28147KeyWrapParameters(OID wrapOID) throws ASNEncodeException {
        asnWrapOID = new ASNObjectIdentifier(wrapOID);
        subs = new ArrayList<ASNCommon>(1);
        subs.add(asnWrapOID);
        encode();
    }

    /**
     * Конструктор, используемый при разборе CMSEnveloped сообщения.
     * @param sequence ASN1-структура, связанная с классом.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    public Gost28147KeyWrapParameters(ASNSequence sequence) throws ASNDecodeException {
        this.encodedValue = sequence.getEncoded();
        this.realInternalLength = sequence.getRealInternalLength();
        this.realEncodedLength = sequence.getRealEncodedLength();
        this.virtualEncodedLength = sequence.getVirtualEncodedLength();
        this.virtualInternalLength = sequence.getVirtualInternalLength();
        this.subs = sequence.getSubStructures();
        if (!checkConsist())
            throw new ASNDecodeException("Corrupted Gost28147KeyWrapParameters structure!");
        asnWrapOID = (ASNObjectIdentifier)subs.get(0);
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    public boolean checkConsist() {
        if ((subs.size() != 1) || (!(subs.get(0) instanceof ASNObjectIdentifier)))
            return false;
        return true;
    }

    /**
     * Метод, возвращающий объектный идентификатор параметров аалгоритма шифрования ключа.
     * @return Объектный идентификатор параметров аалгоритма шифрования ключа.
     */
    public OID getWrapOID() {
        return (OID)asnWrapOID.getValue();
    }
}
