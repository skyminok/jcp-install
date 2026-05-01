package cmsutil.asn1.kari;


import cmsutil.asn1.ASNEncodeException;
import cmsutil.asn1.base.ASNCommon;
import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.base.ASNObjectIdentifier;
import cmsutil.asn1.base.ASNSequence;
import cmsutil.tools.OID;

import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование параметров алгоритма шифрования ключа шифрования данных.
 */
public class Gost28147KeyWrapAlgorithm extends ASNSequence {
    /** Объектный идентификатор алгоритма шифрования ключа.*/
    private ASNObjectIdentifier asnKeyWrapAlgOID;
    /** Параметры шифрования ключа.*/
    private Gost28147KeyWrapParameters parameters;

    /**
     * Конструктор, используемый при создании CMS Enveloped сообщения.
     * @param keyWrapAlgOID Объектный идентификатор алгоритма шифрования ключа.
     * @param keyWrapParamOID Объектный идентификатор набора параметров шифрования ключа.
     */
    public Gost28147KeyWrapAlgorithm(OID keyWrapAlgOID, OID keyWrapParamOID)
        throws ASNEncodeException {
        this.parameters = new Gost28147KeyWrapParameters(keyWrapParamOID);
        this.asnKeyWrapAlgOID = new ASNObjectIdentifier(keyWrapAlgOID);
        this.subs = new ArrayList<ASNCommon>(2);
        subs.add(asnKeyWrapAlgOID);
        subs.add(parameters);
        encode();
    }

    /**
     * Конструктор, используемый при разборе CMS Enveloped сообщения.
     * @param sequence ASN1-структура, связанная с классом.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    public Gost28147KeyWrapAlgorithm(ASNSequence sequence) throws ASNDecodeException {
        this.encodedValue = sequence.getEncoded();
        this.realInternalLength = sequence.getRealInternalLength();
        this.realEncodedLength = sequence.getRealEncodedLength();
        this.virtualEncodedLength = sequence.getVirtualEncodedLength();
        this.virtualInternalLength = sequence.getVirtualInternalLength();
        this.subs = sequence.getSubStructures();
        if (!checkConsist())
            throw new ASNDecodeException("Corrupted Gost28147KeyWrapAlgorithm structure!");
        asnKeyWrapAlgOID = (ASNObjectIdentifier)subs.get(0);
        parameters = new Gost28147KeyWrapParameters((ASNSequence)subs.get(1));
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    public boolean checkConsist() {
        if ((subs.size() != 2) || (!(subs.get(0) instanceof ASNObjectIdentifier)) || (!(subs.get(1) instanceof ASNSequence)))
            return false;
        ASNObjectIdentifier asnObjectIdentifier = (ASNObjectIdentifier) subs.get(0);
        if ((!(asnObjectIdentifier.getValue().equals(OID.noKeyWrapOID))) &&
            (!(asnObjectIdentifier.getValue().equals(OID.cryptoProKeyWrapOID))))
            return false;
        return true;
    }

    /**
     * Метод, возвращающий объектный идентификатор алгоритма шифрования ключа.
     * @return Объектный идентификатор алгоритма шифрования ключа.
     */
    public OID getKeyWrapOID() {
        return (OID)asnKeyWrapAlgOID.getValue();
    }

    /**
     * Метод, возвращающий объектный идентификатор параметров алгоритма шифрования ключа.
     * @return Объектный идентификатор параметров алгоритма шифрования ключа.
     */
    public OID getKeyWrapParametersOID() {
        return parameters.getWrapOID();
    }
}
