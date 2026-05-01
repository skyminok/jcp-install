package cmsutil.asn1.kari;


import cmsutil.asn1.CMSCryptographyException;
import cmsutil.asn1.base.ASNCommon;
import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.base.ASNSequence;

import javax.crypto.SecretKey;
import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование списка структур RecipientEncryptedKey
 */
public class RecipientEncryptedKeys extends ASNSequence {
    /**
     * Массив структур RecipientEncryptedKey
     */
    private RecipientEncryptedKey[] encryptedKeys;

    /**
     * Конструктор, используемый при создании сообщения CMS Enveloped.
     * @param keys Массив структур RecipientEncryptedKey.
     */
    public RecipientEncryptedKeys(RecipientEncryptedKey[] keys) {
        encryptedKeys = keys;
        subs = new ArrayList<ASNCommon>(encryptedKeys.length);
        for (int i = 0; i < encryptedKeys.length; i++) {
            subs.add(encryptedKeys[i]);
        }
        encode();
    }

    /**
     * Конструктор, используемый при разборе сообщения CMS Enveloped.
     * @param sequence ASN1-структура, связанная с классом.
     * @param kek Ключ шифрования ключа.
     * @param keyWrapAlgorithm Алгоритм шифрования ключа.
     * @throws ASNDecodeException Ошибка раскодирования.
     * @throws CMSCryptographyException Ошибка расшифрования ключа.
     */
    public RecipientEncryptedKeys(ASNSequence sequence, SecretKey kek,
        Gost28147KeyWrapAlgorithm keyWrapAlgorithm, String provider)
            throws ASNDecodeException, CMSCryptographyException {
        this.encodedValue = sequence.getEncoded();
        this.realInternalLength = sequence.getRealInternalLength();
        this.realEncodedLength = sequence.getRealEncodedLength();
        this.virtualEncodedLength = sequence.getVirtualEncodedLength();
        this.virtualInternalLength = sequence.getVirtualInternalLength();
        this.subs = sequence.getSubStructures();
        if (!checkConsist())
            throw new ASNDecodeException("Corrupted RecipientEncryptedKeys structure!");
        encryptedKeys = new RecipientEncryptedKey[subs.size()];
        for (int i = 0; i < encryptedKeys.length; i++) {
            encryptedKeys[i] = new RecipientEncryptedKey((ASNSequence)subs.get(i),
                kek, keyWrapAlgorithm, provider);
        }
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        if (subs.size() < 1)
            return false;
        for (int i = 0; i < subs.size(); i++) {
            if (!(subs.get(i) instanceof ASNSequence))
                return false;
        }
        return true;
    }

    /**
     * Метол, получающий массив структур RecipientEncryptedKey
     * @return Массив структур RecipientEncryptedKey.
     */
    public RecipientEncryptedKey[] getRecipientEncryptedKeys() {
        return encryptedKeys;
    }
}
