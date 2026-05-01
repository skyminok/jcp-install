package cmsutil.asn1.kari;

import cmsutil.asn1.CMSCryptographyException;
import cmsutil.asn1.base.ASNCommon;
import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.base.ASNOctetString;
import cmsutil.asn1.base.ASNSequence;

import javax.crypto.SecretKey;
import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование структуры RecipientEncryptedKey
 */
public class RecipientEncryptedKey extends ASNSequence {
    /** Информация о сертификате получателя */
    private RecipientCertInfo certInfo;
    /** Зашифрованный ключ шифрования данных */
    private Gost28147EncryptedKey encryptedKey;

    /**
     * Конструктор, используемый при создании сообщения CMS Enveloped.
     * @param certInfo Информация о сертификате отправителя.
     * @param encryptedKey Шифрованный ключ шифрования данных.
     */
    public RecipientEncryptedKey(RecipientCertInfo certInfo, Gost28147EncryptedKey encryptedKey) {
        this.certInfo = certInfo;
        this.encryptedKey = encryptedKey;
        this.subs = new ArrayList<ASNCommon>(2);
        this.subs.add(certInfo);
        this.subs.add(encryptedKey);
        encode();
    }

    /**
     * Конструктор, используемый при разборе сообщения CMS Enveloped.
     * @param sequence ASN1-структура, связанная с классом.
     * @param kek Ключ шифрования ключа.
     * @param cekWrapAlg Параметры алгоритма шифрования ключа.
     * @throws ASNDecodeException Ошибка раскодирования.
     * @throws CMSCryptographyException Ошибка расшифрования ключа.
     */
    public RecipientEncryptedKey(ASNSequence sequence, SecretKey kek,
        Gost28147KeyWrapAlgorithm cekWrapAlg, String provider) throws
        ASNDecodeException, CMSCryptographyException {
        this.encodedValue = sequence.getEncoded();
        this.realInternalLength = sequence.getRealInternalLength();
        this.realEncodedLength = sequence.getRealEncodedLength();
        this.virtualEncodedLength = sequence.getVirtualEncodedLength();
        this.virtualInternalLength = sequence.getVirtualInternalLength();
        this.subs = sequence.getSubStructures();
        if (!checkConsist())
            throw new ASNDecodeException("Corrupted RecipientEncryptedKey structure!");
        this.certInfo = new RecipientCertInfo((ASNSequence)subs.get(0));
        this.encryptedKey = new Gost28147EncryptedKey((ASNOctetString)subs.get(1),
            kek, cekWrapAlg, provider);
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        return (subs.size() == 2) && (subs.get(0) instanceof ASNSequence) && (subs.get(1) instanceof ASNOctetString);
    }

    /**
     * Метод, осуществляющий получения информации о сертификате получателя.
     * @return Информация о сертификате получателя в виде объекта класса RecipientCertInfo.
     */
    public RecipientCertInfo getCertInfo() {
        return certInfo;
    }

    /**
     * Метод, осуществляющий получение зашифрованного ключа шифрования данных.
     * @return Зашифрованный ключ шифрования данных в виде объекта класса Gost28147EncryptedKey.
     */
    public Gost28147EncryptedKey getEncryptedKey() {
        return encryptedKey;
    }
}
