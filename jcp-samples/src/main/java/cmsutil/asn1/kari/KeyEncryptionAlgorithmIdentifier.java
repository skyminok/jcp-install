package cmsutil.asn1.kari;


import cmsutil.asn1.ASNEncodeException;
import cmsutil.asn1.base.ASNCommon;
import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.base.ASNObjectIdentifier;
import cmsutil.asn1.base.ASNSequence;
import cmsutil.tools.OID;

import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование информации об алгоритмах ключевого соглашения
 * и шифрования ключа.
 */
public class KeyEncryptionAlgorithmIdentifier extends ASNSequence {
    /**Объектный идентификатор алгоритма выработки общего ключа */
    private ASNObjectIdentifier asnDHOID;
    /** Набор параметров шифрования ключа шифрования данных. */
    private Gost28147KeyWrapAlgorithm keyWrapAlgorithm;

    /**
     * Конструктор, используемый при создании CMS Enveloped сообщения.
     * @param dhOID Объектный идентификатор алгоритма ВКО.
     * @param keyWrapAlgOID Объектный идентификатор алгоритма шифрования ключа.
     * @param keyWrapParamOID Объектный идентифкатор набора параметров алгоритма шифрования ключа.
     */
    public KeyEncryptionAlgorithmIdentifier(OID dhOID, OID keyWrapAlgOID,
        OID keyWrapParamOID) throws ASNEncodeException {
        asnDHOID = new ASNObjectIdentifier(dhOID);
        keyWrapAlgorithm = new Gost28147KeyWrapAlgorithm(keyWrapAlgOID, keyWrapParamOID);
        subs = new ArrayList<ASNCommon>(2);
        subs.add(asnDHOID);
        subs.add(keyWrapAlgorithm);
        encode();
    }

    /**
     * Конструктор, используемый при разборе CMS Enveloped сообщения.
     * @param sequence ASN1-структура, связанная с классом.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    public KeyEncryptionAlgorithmIdentifier(ASNSequence sequence) throws ASNDecodeException {
        this.encodedValue = sequence.getEncoded();
        this.realInternalLength = sequence.getRealInternalLength();
        this.realEncodedLength = sequence.getRealEncodedLength();
        this.virtualEncodedLength = sequence.getVirtualEncodedLength();
        this.virtualInternalLength = sequence.getVirtualInternalLength();
        this.subs = sequence.getSubStructures();
        if (!checkConsist())
            throw new ASNDecodeException("Corrupted KeyEncryptionAlgorithmIdentifier structure!");
        asnDHOID = (ASNObjectIdentifier)subs.get(0);
        keyWrapAlgorithm = new Gost28147KeyWrapAlgorithm((ASNSequence)subs.get(1));
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        if ((subs.size() != 2) || (!(subs.get(0) instanceof ASNObjectIdentifier)) || (!(subs.get(1) instanceof ASNSequence)))
            return false;
        ASNObjectIdentifier asnObjectIdentifier = (ASNObjectIdentifier) subs.get(0);
        if (!(asnObjectIdentifier.getValue().equals(OID.gostR3410ESDHOID)) &&
            !(asnObjectIdentifier.getValue().equals(OID.gostR3410ESDHOID_2012)))
            return false;
        return true;
    }

    /**
     * Метод, возвращающий параметры алгоритма шифрования ключа.
     * @return параметры алгоритма шифрования ключа.
     */
    public Gost28147KeyWrapAlgorithm getKeyWrapAlgorithm() {
        return keyWrapAlgorithm;
    }
}
