package cmsutil.asn1.kari;

import ru.CryptoPro.JCP.JCP;
import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.base.*;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование открытого ключа отправителя и его параметров.
 */
class OriginatorPublicKey extends ASNContextSpecificConstructed {
    /**
     * Открытый ключ отправителя.
     */
    private PublicKey publicKey;

    /**
     * Конструктор, используемый при создании сообщения CMS Enveloped.
     * @param gostPublicKey Открытый ключ отправителя.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    public OriginatorPublicKey(PublicKey gostPublicKey) throws ASNDecodeException {
        this.publicKey = gostPublicKey;
        this.type = 1;
        byte[] value = publicKey.getEncoded();
        int offset = getOffset(gostPublicKey);
        byte[] internals = new byte[value.length - offset];
        System.arraycopy(value, offset, internals, 0, internals.length);
        setUpByValue(internals);
    }

    private int getOffset(PublicKey publicKey) {
        String algName = publicKey.getAlgorithm();
        return (algName.equalsIgnoreCase(JCP.GOST_EL_2012_512_NAME)) ? 3 : 2;

    }

    /**
     * Конструктор, используемый при разборе сообщения CMS Enveloped.
     * @param contextSpecificConstructed ASN1-структура, связанная с классом.
     * @throws ASNDecodeException Ошибка раскодирования.
     * @throws InvalidKeySpecException Некорректный ключ.
     */
    public OriginatorPublicKey(ASNContextSpecificConstructed contextSpecificConstructed,
        String provider) throws ASNDecodeException, InvalidKeySpecException, NoSuchProviderException,
        NoSuchAlgorithmException {
        this.subs = contextSpecificConstructed.getSubStructures();
        this.encodedValue = contextSpecificConstructed.getEncoded();
        this.type = (byte)(encodedValue[0] & 0x0f);
        this.realInternalLength = contextSpecificConstructed.getRealInternalLength();
        this.realEncodedLength = contextSpecificConstructed.getRealEncodedLength();
        this.virtualEncodedLength = contextSpecificConstructed.getVirtualEncodedLength();
        this.virtualInternalLength = contextSpecificConstructed.getVirtualInternalLength();
        if (!checkConsist())
            throw new ASNDecodeException("Structure Originator is corrupted!");
        byte[] publicKeyBlob = new byte[encodedValue.length];
        System.arraycopy(encodedValue, 1, publicKeyBlob, 1, encodedValue.length - 1);
        publicKeyBlob[0] = ASN1Sequence;
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBlob);
        KeyFactory fac = KeyFactory.getInstance(JCP.GOST_EL_DEGREE_NAME, provider);
        publicKey = fac.generatePublic(spec); // не из сертификата
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    public boolean checkConsist() {
        if (type != 1)
            return false;
        if (subs.size() != 2)
            return false;
        if ((!(subs.get(0) instanceof ASNSequence)) || (!(subs.get(1) instanceof ASNBitString)))
            return false;
        ASNSequence tmpSeq = (ASNSequence)subs.get(0);
        ArrayList<ASNCommon> subsubs = tmpSeq.getSubStructures();
        if (subsubs.size() != 2)
            return false;
        if ((!(subsubs.get(0) instanceof ASNObjectIdentifier)) || (!(subsubs.get(1) instanceof ASNSequence)))
            return false;
        tmpSeq = (ASNSequence)subsubs.get(1);
        subsubs = tmpSeq.getSubStructures();
        if (subsubs.size() != 2)
            return false;
        if ((!(subsubs.get(0) instanceof ASNObjectIdentifier)) || (!(subsubs.get(1) instanceof ASNObjectIdentifier)))
            return false;
        return true;
    }

    /**
     * Метод, возвращающий открытый ключ отправителя.
     * @return Открытый ключ отправителя.
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
}
