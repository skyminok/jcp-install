/**
 * $RCSfileASN1NumericString.java,v $
 * version $Revision: 36379 $
 * created 14.06.2017 15:23 by afevma
 * last modified $Date: 2012-05-30 12:19:27 +0400 (Ср, 30 май 2012) $ by $Author: afevma $
 * <br>
 * Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
 */
package cmsutil.asn1.base;

import cmsutil.asn1.ASNDecodeException;
import cmsutil.asn1.ASNEncodeException;

/**
 * Класс, используемый для ASN1-кодирования numeric строк
 *
 * @author Copyright 2004-2017 Crypto-Pro. All rights reserved.
 * @version 2.5
 */
public class ASN1NumericString extends ASNPrimitive {

    /** Значение строки*/
    protected byte[] value;

    public ASN1NumericString(String string, boolean validate)
        throws ASNEncodeException {

        if (validate && !isNumericString(string)) {
            throw new ASNEncodeException("string contains illegal characters");
        }

        this.value = toByteArray(string);
        encode();

    }

    public ASN1NumericString(byte[] encoded, int offset) throws ASNDecodeException {
        decode(encoded, offset);
    }

    public static byte[] toByteArray(String string) {

        byte[] bytes = new byte[string.length()];
        for (int i = 0; i != bytes.length; i++) {

            char ch = string.charAt(i);
            bytes[i] = (byte)ch;

        }

        return bytes;

    }

    /**
     * Return true if the string can be represented as a
     * NumericString ('0'..'9', ' ')
     *
     * @param str string to validate.
     * @return true if numeric, false otherwise.
     */
    public static boolean isNumericString(String str) {

        for (int i = str.length() - 1; i >= 0; i--) {
            char ch = str.charAt(i);

            if (ch > 0x007f) {
                return false;
            }

            if (('0' <= ch && ch <= '9') || ch == ' ') {
                continue;
            }

            return false;

        }

        return true;

    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    protected byte[] getByteValue() throws ASNEncodeException {
        return value;
    }

    @Override
    protected void setValue(byte[] val) throws ASNEncodeException {
        this.value = new byte[val.length];
        System.arraycopy(val, 0, value, 0, val.length);
    }

    @Override
    public int getTag() {
        return ASN1NumericString;
    }

}
