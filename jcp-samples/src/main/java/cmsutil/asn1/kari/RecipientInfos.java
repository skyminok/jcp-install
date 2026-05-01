package cmsutil.asn1.kari;

import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.CMSCryptographyException;
import cmsutil.asn1.base.*;
import cmsutil.tools.OID;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * Класс, кодирующий набор структур RecipientInfos
 */
public class RecipientInfos extends ASNSet {

    /** Структура KeyAgreeRecipientInfo*/
    private KARI kari;
    private final static OID wrapAlgOID = OID.noKeyWrapOID;

    /**
     * Конструктор, используемый при создании сообщения CMS Enveloped.
     * @param cek Ключ шифрования ключа
     * @param recipentCert Сертификат получателя.
     * @throws ASNDecodeException Ошибка раскодирования.
     * @throws CMSCryptographyException Ошибка криптографической операции.
     */
    public RecipientInfos(SecretKey cek, X509Certificate recipentCert, String provider)
            throws ASNDecodeException, CMSCryptographyException
    {
        kari = new KARI(cek, wrapAlgOID, recipentCert, provider);
        subs = new ArrayList<ASNCommon>(1);
        subs.add(kari);
        encode();
    }

    /**
     * Конструктор, используемый при разборе сообщения CMS Enveloped.
     * @param set ASN1-структура, свзянная с классом.
     * @param recipientPrivateKey Секретный ключ получателя ГОСТ Р 34.10-2001
     * @throws ASNDecodeException Ошибка раскодирования.
     * @throws CMSCryptographyException Ошибка криптографической операции.
     */
    public RecipientInfos(ASNSet set, PrivateKey recipientPrivateKey, String provider)
        throws ASNDecodeException, CMSCryptographyException {
        this.subs = set.getSubStructures();
        this.encodedValue = set.getEncoded();
        this.realInternalLength = set.getRealInternalLength();
        this.realEncodedLength = set.getRealEncodedLength();
        this.virtualEncodedLength = set.getVirtualEncodedLength();
        this.virtualInternalLength = set.getVirtualInternalLength();
        if (!checkConsist())
            throw new ASNDecodeException("Structure RecipientInfos is corrupted!");
        this.kari = new KARI((ASNContextSpecificConstructed)subs.get(0), recipientPrivateKey, provider);
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        return (subs != null) && (subs.size() > 0) && (subs.get(0) instanceof ASNContextSpecificConstructed);
    }

    /**
     * Метод, осуществляющий получение ключа шифрования данных.
     * @return Ключ шифрования данных ГОСТ 28147-89.
     */
    public SecretKey getCEK() {
        return kari.getCEK();
    }

    /**
     * Метод, возвращающий информацию о сертификате получателя, указанную в сообщении.
     * @return Объект класса RecipientCertInfo, содержащий требуюмую информацию.
     */
    public RecipientCertInfo getRecipientCertInfo() {
        return kari.getRecipientCertInfo();
    }
}
