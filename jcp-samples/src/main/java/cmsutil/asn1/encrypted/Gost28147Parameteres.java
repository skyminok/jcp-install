package cmsutil.asn1.encrypted;


import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.base.*;
import cmsutil.tools.OID;

import java.util.ArrayList;

/**
 * Класс, используемый для кодирования набора параметров алгоритма шифрования ГОСТ 28147-89.
 */
public class Gost28147Parameteres extends ASNSequence {

    /** Объектный идентификатор набора узлов замены. */
    private ASNObjectIdentifier cipherOID;
    /** Синхропосылка.*/
    private ASNOctetString iv;

    /**
     * Конструктор, используемый при закодировании.
     * @param asnIv Синхропосылка.
     * @param asnCipherOID Объектный идентфикатор набора узлов замены.
     */
    public Gost28147Parameteres(ASNOctetString asnIv, ASNObjectIdentifier asnCipherOID) {
        subs = new ArrayList<ASNCommon>(2);
        subs.add(asnIv);
        subs.add(asnCipherOID);
        encode();
        cipherOID = asnCipherOID;
        iv = asnIv;
    }

    /**
     * Конструктор, используемый при раскодировании
     * @param sequence ASN1-структура, связанная с классом.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    public Gost28147Parameteres(ASNSequence sequence) throws ASNDecodeException {
        this.encodedValue = sequence.getEncoded();
        this.realInternalLength = sequence.getRealInternalLength();
        this.realEncodedLength = sequence.getRealEncodedLength();
        this.virtualEncodedLength = sequence.getVirtualEncodedLength();
        this.virtualInternalLength = sequence.getVirtualInternalLength();
        this.subs = sequence.getSubStructures();
        if (!checkConsist())
            throw new ASNDecodeException("Corrupted Gost28147Parameteres structure!");
        cipherOID = (ASNObjectIdentifier)subs.get(1);
        iv = (ASNOctetString)subs.get(0);
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        return subs.size() == 2 && (subs.get(0) instanceof ASNOctetString) && (subs.get(1) instanceof ASNObjectIdentifier);
    }

    /**
     * Метод, возвращающий синхропосылку.
     * @return Байтовый массив, содержащий синхропосылку.
     */
    public byte[] getIv() {
        return (byte[])iv.getValue();
    }

    /**
     * Метод, возвращающий объектный идентификатор набора узлов замены.
     * @return Объект класса OID, содержащий объектный идентификатор набора узлов замены.
     */
    public OID getOID() {
        return (OID)cipherOID.getValue();
    }
}
