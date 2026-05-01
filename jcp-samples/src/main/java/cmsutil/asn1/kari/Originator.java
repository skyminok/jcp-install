package cmsutil.asn1.kari;

import cmsutil.asn1.CMSCryptographyException;
import cmsutil.asn1.base.ASNCommon;
import cmsutil.asn1.base.ASNContextSpecificConstructed;
import cmsutil.asn1.ASNDecodeException;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

/**
 * Класс, кодирующий информацию об отправителе.
 */
public class Originator extends ASNContextSpecificConstructed {
    /** Структура, содержащая информацию об открытом ключе отправителя. */
    private OriginatorPublicKey originatorPublicKey;

    /**
     * Конструктор, используемый при создании сообщения CMS Enveloped.
     * @param publicKey Открытый ключ отправителя.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    public Originator(PublicKey publicKey) throws ASNDecodeException {
        originatorPublicKey = new OriginatorPublicKey(publicKey);
        this.type = 0;
        this.subs = new ArrayList<ASNCommon>(1);
        subs.add(originatorPublicKey);
        encode();
    }

    /**
     * Конструктор, используемый при разборе сообщения CMS Enveloped.
     * @param contextSpecificConstructed ASN1-структура, связанная с классом.
     * @throws ASNDecodeException Ошибка раскодирования.
     * @throws CMSCryptographyException Ошибка раскодирования открытого ключа.
     */
    public Originator(ASNContextSpecificConstructed contextSpecificConstructed,
        String provider) throws ASNDecodeException, CMSCryptographyException {
        this.subs = contextSpecificConstructed.getSubStructures();
        this.encodedValue = contextSpecificConstructed.getEncoded();
        this.type = (byte)(encodedValue[0] & 0x0f);
        this.realInternalLength = contextSpecificConstructed.getRealInternalLength();
        this.realEncodedLength = contextSpecificConstructed.getRealEncodedLength();
        this.virtualEncodedLength = contextSpecificConstructed.getVirtualEncodedLength();
        this.virtualInternalLength = contextSpecificConstructed.getVirtualInternalLength();
        if (!checkConsist())
            throw new ASNDecodeException("Structure Originator is corrupted!");
        try {
            originatorPublicKey = new OriginatorPublicKey((ASNContextSpecificConstructed) subs.get(0), provider);
        }
        catch (InvalidKeySpecException e) {
            throw new CMSCryptographyException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new CMSCryptographyException(e.getMessage());
        } catch (NoSuchProviderException e) {
            throw new CMSCryptographyException(e.getMessage());
        }
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        return ((subs.size() == 1) && (subs.get(0) instanceof ASNContextSpecificConstructed));
    }

    /**
     * Метод, возвращающий открытый ключ отправителя.
     * @return Открытый ключ отправителя.
     */
    public PublicKey getPublicKey() {
        return originatorPublicKey == null ? null : originatorPublicKey.getPublicKey();
    }
}
