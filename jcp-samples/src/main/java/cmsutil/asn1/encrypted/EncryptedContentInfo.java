package cmsutil.asn1.encrypted;

import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;
import cmsutil.asn1.CMSCryptographyException;
import cmsutil.asn1.base.*;
import cmsutil.tools.OID;

import javax.crypto.SecretKey;
import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование шифртекста и параметров шифрования.
 */
public class EncryptedContentInfo extends ASNSequence {
    /** Параметры алгоритма шифрования*/
    private ContentEncryptionAlgorithmIdentifier encAlgId;
    /** Шифртекст*/
    private EncryptedContext encryptedContext;

    /**
     * Конструктор, используемый при закодировании. Создаёт объект шифратора, инициализирует его на зашифрование,
     * кодирует его параметры и создаёт виртуальный объект шифртекста.
     * @param textLen Длина открытого текста.
     * @param key Ключ шифрования ГОСТ 28147-89.
     * @param cipherOID Объектный идентификатор узлов замены.
     * @throws CMSCryptographyException Ошибка инициализации шифратора.
     */
    public EncryptedContentInfo(long textLen, SecretKey key, OID cipherOID, String provider)
        throws CMSCryptographyException, ASNEncodeException {
        CipherProcessor cipherProcessor = new CipherProcessor(textLen, key, cipherOID, provider);
        encryptedContext = new EncryptedContext(cipherProcessor);
        encAlgId = new ContentEncryptionAlgorithmIdentifier(
            new Gost28147Parameteres(new ASNOctetString(cipherProcessor.getIv()),
                new ASNObjectIdentifier(cipherOID)));
        subs = new ArrayList<ASNCommon>(3);
        ASNObjectIdentifier dataType = new ASNObjectIdentifier(OID.dataOID);
        subs.add(dataType);
        subs.add(encAlgId);
        subs.add(encryptedContext);
        encode();
    }

    /**
     * Конструктор, используемый при раскодировании. Раскодирует параметры шифратора, создаёт объект шифратора,
     * инициализирует его на расшифрование.
     * @param sequence ASN1-последовательность, соответствующая классу.
     * @param key Ключ шифрования ГОСТ 28147-89.
     * @throws CMSCryptographyException Ошибка инициализации шифратора.
     * @throws ASNDecodeException Ошибка раскодирования данных.
     */
    public EncryptedContentInfo(ASNSequence sequence, SecretKey key, String provider)
        throws CMSCryptographyException, ASNDecodeException {
        this.subs = sequence.getSubStructures();
        this.encodedValue = sequence.getEncoded();
        this.realInternalLength = sequence.getRealInternalLength();
        this.realEncodedLength = sequence.getRealEncodedLength();
        this.virtualEncodedLength = sequence.getVirtualEncodedLength();
        this.virtualInternalLength = sequence.getVirtualInternalLength();
        if (!checkConsist())
            throw new ASNDecodeException("EncryptedContentInfo structure corrupted!");
        encAlgId = new ContentEncryptionAlgorithmIdentifier((ASNSequence)subs.get(1));
        encryptedContext = new EncryptedContext((ASNContextSpecificPrimitive)subs.get(2),
                key, encAlgId.getGostParams().getOID(), encAlgId.getGostParams().getIv(), provider);
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        if (subs.size() != 3)
            return false;
        ASNCommon tmpOID = subs.get(0);
        if (!(tmpOID instanceof ASNObjectIdentifier))
            return false;
        if (!(((OID)(((ASNObjectIdentifier)subs.get(0)).getValue())).equals(OID.dataOID)))
            return false;
        ASNCommon tmpEncAlgId = subs.get(1);
        if (!(tmpEncAlgId instanceof ASNSequence))
            return false;
        if (!(subs.get(2) instanceof ASNContextSpecificPrimitive))
            return false;
        return true;
    }

    /**
     * Метод, осуществляющий получение высокоуровневого объекта шифратора.
     * @return Объект шифратора класса CipherProcessor.
     */
    public CipherProcessor getCipherProcessor() {
        return encryptedContext.getCipherProcessor();
    }
}
