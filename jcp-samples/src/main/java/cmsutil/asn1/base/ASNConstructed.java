package cmsutil.asn1.base;


import cmsutil.asn1.ASNDecodeException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Класс, осуществляющий закодирование и раскодирование сложных ASN1-структур
 * (ASN1Sequence, ASN1Set и др.).
 */
public abstract class ASNConstructed extends ASNCommon {

    /**
     * Максимальное количество подструктур в структуре.
     * Может быть автоматически увеличено.
     */
    private final static int INITIAL_ARRAY_LIST_CAPACITY = 20;
    /** Список подструктур */
    protected ArrayList<ASNCommon> subs;

    /**
     * Метод, осуществляющий проверку корректности сложной структуры.
     * @return
     */
    protected abstract boolean checkConsist();

    /**
     * Метод,осуществляющий раскодирование сложной ASN1-структуры.
     * @param encoded Байтовый массив, содержащий ASN1-структуру.
     * @param offset Смещение относительно начала массива, с которого начинается структура.
     * @throws ASNDecodeException Ошибка раскодирования.
     */
    protected void decode(byte[] encoded, int offset) throws ASNDecodeException {
        int[] lenBuf = new int[1];
        int tempOffset = offset;
        if (encoded == null)
            throw new ASNDecodeException("NPE while decoding ASN value!");
        if (encoded.length - offset < 2)
            throw new ASNDecodeException("ASN1 structure too short");
        if (((encoded[offset]) & 0xff) != getTag())
            throw new ASNDecodeException("ASN1 parsing error: 0x" + Integer.toHexString(encoded[offset]) + " tag received " +
                    "while tag 0x" + Integer.toHexString(getTag()) + " expected.");
        virtualInternalLength = getLength(encoded, offset, lenBuf);
        virtualEncodedLength = 1 + lenBuf[0] + virtualInternalLength;
        subs = new ArrayList<ASNCommon>(INITIAL_ARRAY_LIST_CAPACITY);
        tempOffset += (1 + lenBuf[0]);
        ASNCommon asnCommon;
        realInternalLength = 0;
        while ((tempOffset < offset + virtualEncodedLength) && (tempOffset < encoded.length)) {
            int tag = ((int)encoded[tempOffset]) & 0xff;
            switch (tag) {
                case ASN1Integer:
                    asnCommon = new ASNInteger(encoded, tempOffset);
                    break;
                case ASN1ObjectIdentifier:
                    asnCommon = new ASNObjectIdentifier(encoded, tempOffset);
                    break;
                case ASN1OctetString:
                    asnCommon = new ASNOctetString(encoded, tempOffset);
                    break;
                case ASN1BitString:
                    asnCommon = new ASNBitString(encoded, tempOffset);
                    break;
                case ASN1IA5String:
                    asnCommon = new ASNIA5String(encoded, tempOffset);
                    break;
                case ASN1UTF8String:
                    asnCommon = new ASNUTF8String(encoded, tempOffset);
                    break;
                case ASN1PrintableString:
                    asnCommon = new ASNPrintableString(encoded, tempOffset);
                    break;
                case ASN1NumericString:
                    asnCommon = new ASN1NumericString(encoded, tempOffset);
                    break;
                case ASN1BMPString:
                    asnCommon = new ASN1BMPString(encoded, tempOffset);
                    break;
                case ASN1Sequence:
                    asnCommon = new ASNSequence(encoded, tempOffset);
                    break;
                case ASN1Set:
                    asnCommon = new ASNSet(encoded, tempOffset);
                    break;
                default:
                    if ((tag & ASN1ContextSpecific) != 0) {
                        if ((tag & ASN1Complicated) != 0) {
                            asnCommon = new ASNContextSpecificConstructed(encoded, tempOffset);
                        } else {
                            asnCommon = new ASNContextSpecificPrimitive(encoded, tempOffset);
                        }
                        break;
                    }
                    throw new ASNDecodeException("Unknown ASN tag: " + Integer.toHexString(tag));
            }
            subs.add(asnCommon);
            realInternalLength += asnCommon.getRealEncodedLength();
            tempOffset += asnCommon.getRealEncodedLength();
        }
        realEncodedLength = 1 + lenBuf[0] + realInternalLength;
        this.encodedValue = new byte[(int)realEncodedLength];
        System.arraycopy(encoded, offset, encodedValue, 0, (int)realEncodedLength);
    }

    /**
     * Метод, осуществляющий закодирование сложной структуры.
     */
    @Override
    protected void encode() {
        Iterator<ASNCommon> iterator = subs.iterator();
        int totalRealLength = 0, offset;
        long totalVirtualLength = 0;
        while (iterator.hasNext()) {
            ASNCommon asnCommon = iterator.next();
            totalRealLength += asnCommon.getRealEncodedLength();
            totalVirtualLength += asnCommon.getVirtualEncodedLength();
        }
        realInternalLength = totalRealLength;
        virtualInternalLength = totalVirtualLength;
        byte[] encodedLength = encodeLength();
        realEncodedLength = 1 + encodedLength.length + realInternalLength;
        virtualEncodedLength = 1 + encodedLength.length + virtualInternalLength;
        encodedValue = new byte[1 + encodedLength.length + (int)realInternalLength];
        encodedValue[0] = (byte)getTag();
        System.arraycopy(encodedLength, 0, encodedValue, 1, encodedLength.length);
        offset = 1 + encodedLength.length;
        iterator = subs.iterator();
        while (iterator.hasNext()) {
            byte[] encoded = iterator.next().getEncoded();
            System.arraycopy(encoded, 0, encodedValue, offset, encoded.length);
            offset += encoded.length;
        }
    }

    /**
     * Метод, осуществляющий получение списка подструктур.
     * @return Список подструктур
     */
    public ArrayList<ASNCommon> getSubStructures() {
        return subs;
    }

}
