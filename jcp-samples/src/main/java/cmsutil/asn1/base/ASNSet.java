package cmsutil.asn1.base;


import cmsutil.asn1.ASNDecodeException;

import java.util.ArrayList;

/**
 * Класс, используемый для ASN1-кодирования множеств (ASN1Set).
 */
public class ASNSet extends ASNSequence {

    /**
     * Конструктор по значению
     * @param newSubs Список подструктур.
     */
    public ASNSet(ArrayList<ASNCommon> newSubs) {
        super(newSubs);
    }

    /**
     * Конструктор по закодированному значению
     * @param encoded Байтовый массив, содержащий ASN1-кодированное значение.
     * @param offset Сдвиг относительно начала массива, с которого начинается ASN1-структура.
     * @throws ASNDecodeException Ошибка декодирования
     */
    public ASNSet(byte[] encoded, int offset) throws ASNDecodeException {
        super(encoded, offset);
    }

    /**
     * Конструктор по умолчанию
     */
    public ASNSet() {

    }

    /**
     * Метод, возвращающий ASN1-тэг.
     * @return ASN1-тэг
     */
    @Override
    public int getTag() {
        return ASN1Set;
    }
}
