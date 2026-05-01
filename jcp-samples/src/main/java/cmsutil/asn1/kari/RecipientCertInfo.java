package cmsutil.asn1.kari;

import cmsutil.asn1.base.ASNCommon;
import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.base.ASNInteger;
import cmsutil.asn1.base.ASNSequence;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Класс, осуществляющий кодирование информации о сертификате открытого ключа получателя.
 */
public class RecipientCertInfo extends ASNSequence {
    /** Имя УЦ, выдавшего сертификат, в формате X.500 */
    private RecipientIssuerX500Principal issuer;
    /** Серийный номер сертификата */
    private ASNInteger serialNumber;

    /**
     * Конструктор, используемый при создании сообщения CMS Enveloped.
     * @param issuerPrincipal Имя УЦ, выдавшего сертификат, в формате X.500
     * @param certSN Серийный номер сертификата.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    public RecipientCertInfo(X500Principal issuerPrincipal, BigInteger certSN) throws ASNDecodeException {
        this.issuer = new RecipientIssuerX500Principal(issuerPrincipal);
        this.serialNumber = new ASNInteger(certSN);
        this.subs = new ArrayList<ASNCommon>(2);
        subs.add(issuer);
        subs.add(serialNumber);
        encode();
    }

    /**
     * Конструктор, используемый при разборе сообщения CMS Enveloped.
     * @param sequence ASN1-структура, свзанная с классом.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    public RecipientCertInfo(ASNSequence sequence) throws ASNDecodeException {
        this.subs = sequence.getSubStructures();
        this.encodedValue = sequence.getEncoded();
        this.realInternalLength = sequence.getRealInternalLength();
        this.realEncodedLength = sequence.getRealEncodedLength();
        this.virtualEncodedLength = sequence.getVirtualEncodedLength();
        this.virtualInternalLength = sequence.getVirtualInternalLength();
        if (!checkConsist())
            throw new ASNDecodeException("Structure RecipientCertInfo is corrupted!");
        issuer = new RecipientIssuerX500Principal((ASNSequence)subs.get(0));
        serialNumber = (ASNInteger)subs.get(1);
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true, тогда и только тогда, когда количество подструктур и их содержимое корректно.
     */
    @Override
    protected boolean checkConsist() {
        return (subs.size() == 2) && (subs.get(0) instanceof ASNSequence) && (subs.get(1) instanceof ASNInteger);
    }

    /**
     * Метод, возвращающий серийный номер сертификата.
     * @return Серийный номер сертификата.
     */
    public BigInteger getSerial() {
        return (BigInteger)serialNumber.getValue();
    }

    /**
     * Мето, возвращающий имя УЦ, выдавшего сертификат, в формате X.500.
     * @return Имя УЦ, выдавшего сертификат, в формате X.500.
     */
    public X500Principal getRecipientIssuerPrincipal() {
        return issuer.getPrincipal();
    }
}
