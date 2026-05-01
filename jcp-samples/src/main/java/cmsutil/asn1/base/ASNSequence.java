package cmsutil.asn1.base;

import cmsutil.asn1.ASNDecodeException;

import java.util.ArrayList;

/**
 * Класс, используемый для ASN1-кодирования упорядоченных последовательностей (ASN1Sequence).
 */
public class ASNSequence extends ASNConstructed {

    /**
     * Конструктор по значению
     * @param newSubs Список подструктур.
     */
    public ASNSequence(ArrayList<ASNCommon> newSubs) {
        subs = newSubs;
        encode();
    }

    /**
     * Конструктор по закодированному значению
     * @param encoded Байтовый массив, содержащий ASN1-кодированное значение.
     * @param offset Сдвиг относительно начала массива, с которого начинается ASN1-структура.
     * @throws ASNDecodeException Ошибка декодирования
     */
    public ASNSequence(byte[] encoded, int offset) throws ASNDecodeException {
        decode(encoded, offset);
    }

    /**
     * Конструктор по умолчанию
     */
    protected ASNSequence() {
        subs = null;
    }

    /**
     * Метод, возвращающий ASN1-тэг.
     * @return ASN1-тэг
     */
    @Override
    public int getTag() {
        return ASN1Sequence;
    }

    /**
     * Метод, проверяющий корректность структуры.
     * @return true.
     */
    @Override
    protected boolean checkConsist() {
        return true;
    }
}
