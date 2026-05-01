package cmsutil.asn1.kari;


import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.base.ASNSequence;

import javax.security.auth.x500.X500Principal;

/**
 * Класс, осуществляющий кодирование имени УЦ, выдавшего сертификат, в формате X.500.
 */
public class RecipientIssuerX500Principal extends ASNSequence {
    /**Имя УЦ, выдавшего сертификат, в формате X.500.*/
    private X500Principal principal;

    /**
     * Конструктор, используемый при создании сообщения CMS Enveloped.
     * @param recipientPrincipal Имя УЦ, выдавшего сертификат, в формате X.500.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    public RecipientIssuerX500Principal(X500Principal recipientPrincipal) throws ASNDecodeException {
        this.principal = recipientPrincipal;
        decode(principal.getEncoded(), 0);
    }

    /**
     * Конструктор, используемый при разборе сообщения CMS Enveloped.
     * @param sequence ASN1-структура, связанная с классом.
     */
    public RecipientIssuerX500Principal(ASNSequence sequence) {
        this.subs = sequence.getSubStructures();
        this.encodedValue = sequence.getEncoded();
        this.realInternalLength = sequence.getRealInternalLength();
        this.realEncodedLength = sequence.getRealEncodedLength();
        this.virtualEncodedLength = sequence.getVirtualEncodedLength();
        this.virtualInternalLength = sequence.getVirtualInternalLength();
        principal = new X500Principal(encodedValue);
    }

    /**
     * Метод, осуществляющий получение имени УЦ, выдавшего сертификат, в формате X.500.
     * @return
     */
    public X500Principal getPrincipal() {
        return principal;
    }
}
